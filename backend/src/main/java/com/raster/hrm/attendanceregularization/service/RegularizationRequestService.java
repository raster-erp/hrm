package com.raster.hrm.attendanceregularization.service;

import com.raster.hrm.attendance.entity.AttendancePunch;
import com.raster.hrm.attendance.entity.PunchDirection;
import com.raster.hrm.attendance.repository.AttendancePunchRepository;
import com.raster.hrm.attendanceregularization.dto.RegularizationApprovalRequest;
import com.raster.hrm.attendanceregularization.dto.RegularizationRequestRequest;
import com.raster.hrm.attendanceregularization.dto.RegularizationRequestResponse;
import com.raster.hrm.attendanceregularization.entity.RegularizationRequest;
import com.raster.hrm.attendanceregularization.entity.RegularizationStatus;
import com.raster.hrm.attendanceregularization.entity.RegularizationType;
import com.raster.hrm.attendanceregularization.repository.RegularizationRequestRepository;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RegularizationRequestService {

    private static final Logger log = LoggerFactory.getLogger(RegularizationRequestService.class);

    private final RegularizationRequestRepository regularizationRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendancePunchRepository attendancePunchRepository;

    public RegularizationRequestService(RegularizationRequestRepository regularizationRequestRepository,
                                         EmployeeRepository employeeRepository,
                                         AttendancePunchRepository attendancePunchRepository) {
        this.regularizationRequestRepository = regularizationRequestRepository;
        this.employeeRepository = employeeRepository;
        this.attendancePunchRepository = attendancePunchRepository;
    }

    @Transactional(readOnly = true)
    public Page<RegularizationRequestResponse> getAll(Pageable pageable) {
        return regularizationRequestRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public RegularizationRequestResponse getById(Long id) {
        var request = regularizationRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RegularizationRequest", "id", id));
        return mapToResponse(request);
    }

    @Transactional(readOnly = true)
    public Page<RegularizationRequestResponse> getByEmployeeId(Long employeeId, Pageable pageable) {
        return regularizationRequestRepository.findByEmployeeId(employeeId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<RegularizationRequestResponse> getByStatus(RegularizationStatus status, Pageable pageable) {
        return regularizationRequestRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<RegularizationRequestResponse> getByType(RegularizationType type, Pageable pageable) {
        return regularizationRequestRepository.findByType(type, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<RegularizationRequestResponse> getByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return regularizationRequestRepository.findByDateRange(startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<RegularizationRequestResponse> getByEmployeeAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return regularizationRequestRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RegularizationRequestResponse create(RegularizationRequestRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var regularizationType = RegularizationType.valueOf(request.type());

        var regularizationRequest = new RegularizationRequest();
        regularizationRequest.setEmployee(employee);
        regularizationRequest.setRequestDate(request.requestDate());
        regularizationRequest.setType(regularizationType);
        regularizationRequest.setReason(request.reason());
        regularizationRequest.setOriginalPunchIn(request.originalPunchIn());
        regularizationRequest.setOriginalPunchOut(request.originalPunchOut());
        regularizationRequest.setCorrectedPunchIn(request.correctedPunchIn());
        regularizationRequest.setCorrectedPunchOut(request.correctedPunchOut());
        regularizationRequest.setRemarks(request.remarks());

        var saved = regularizationRequestRepository.save(regularizationRequest);
        log.info("Created regularization request with id: {} for employee: {}", saved.getId(), employee.getEmployeeCode());
        return mapToResponse(saved);
    }

    public RegularizationRequestResponse update(Long id, RegularizationRequestRequest request) {
        var regularizationRequest = regularizationRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RegularizationRequest", "id", id));

        if (regularizationRequest.getStatus() != RegularizationStatus.PENDING) {
            throw new BadRequestException("Cannot update regularization request with status: " + regularizationRequest.getStatus());
        }

        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var regularizationType = RegularizationType.valueOf(request.type());

        regularizationRequest.setEmployee(employee);
        regularizationRequest.setRequestDate(request.requestDate());
        regularizationRequest.setType(regularizationType);
        regularizationRequest.setReason(request.reason());
        regularizationRequest.setOriginalPunchIn(request.originalPunchIn());
        regularizationRequest.setOriginalPunchOut(request.originalPunchOut());
        regularizationRequest.setCorrectedPunchIn(request.correctedPunchIn());
        regularizationRequest.setCorrectedPunchOut(request.correctedPunchOut());
        regularizationRequest.setRemarks(request.remarks());

        var saved = regularizationRequestRepository.save(regularizationRequest);
        log.info("Updated regularization request with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public RegularizationRequestResponse approve(Long id, RegularizationApprovalRequest request) {
        var regularizationRequest = regularizationRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RegularizationRequest", "id", id));

        if (regularizationRequest.getStatus() != RegularizationStatus.PENDING) {
            throw new BadRequestException("Cannot change status of regularization request with status: " + regularizationRequest.getStatus());
        }

        var newStatus = RegularizationStatus.valueOf(request.status());
        if (newStatus != RegularizationStatus.APPROVED && newStatus != RegularizationStatus.REJECTED) {
            throw new BadRequestException("Status must be APPROVED or REJECTED");
        }

        regularizationRequest.setStatus(newStatus);
        regularizationRequest.setApprovedBy(request.approvedBy());
        regularizationRequest.setApprovedAt(LocalDateTime.now());
        if (request.remarks() != null) {
            regularizationRequest.setRemarks(request.remarks());
        }

        var saved = regularizationRequestRepository.save(regularizationRequest);
        log.info("Updated regularization request id: {} status to {}", saved.getId(), newStatus);

        // Auto-create corrected attendance punches when approved
        if (newStatus == RegularizationStatus.APPROVED) {
            createCorrectedPunches(regularizationRequest);
        }

        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var regularizationRequest = regularizationRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RegularizationRequest", "id", id));
        regularizationRequestRepository.delete(regularizationRequest);
        log.info("Deleted regularization request with id: {}", id);
    }

    private void createCorrectedPunches(RegularizationRequest regularizationRequest) {
        var employee = regularizationRequest.getEmployee();

        var punchIn = new AttendancePunch();
        punchIn.setEmployee(employee);
        punchIn.setPunchTime(regularizationRequest.getCorrectedPunchIn());
        punchIn.setDirection(PunchDirection.IN);
        punchIn.setSource("API");
        punchIn.setNormalized(true);
        attendancePunchRepository.save(punchIn);

        var punchOut = new AttendancePunch();
        punchOut.setEmployee(employee);
        punchOut.setPunchTime(regularizationRequest.getCorrectedPunchOut());
        punchOut.setDirection(PunchDirection.OUT);
        punchOut.setSource("API");
        punchOut.setNormalized(true);
        attendancePunchRepository.save(punchOut);

        log.info("Created corrected attendance punches for regularization request id: {} employee: {}",
                regularizationRequest.getId(), employee.getEmployeeCode());
    }

    private RegularizationRequestResponse mapToResponse(RegularizationRequest request) {
        var employee = request.getEmployee();
        return new RegularizationRequestResponse(
                request.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                request.getRequestDate(),
                request.getType().name(),
                request.getReason(),
                request.getOriginalPunchIn(),
                request.getOriginalPunchOut(),
                request.getCorrectedPunchIn(),
                request.getCorrectedPunchOut(),
                request.getStatus().name(),
                request.getApprovalLevel(),
                request.getRemarks(),
                request.getApprovedBy(),
                request.getApprovedAt(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
