package com.raster.hrm.separation.service;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.entity.EmploymentStatus;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.separation.dto.SeparationRequest;
import com.raster.hrm.separation.dto.SeparationResponse;
import com.raster.hrm.separation.entity.Separation;
import com.raster.hrm.separation.entity.SeparationStatus;
import com.raster.hrm.separation.entity.SeparationType;
import com.raster.hrm.separation.repository.SeparationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SeparationService {

    private static final Logger log = LoggerFactory.getLogger(SeparationService.class);

    private final SeparationRepository separationRepository;
    private final EmployeeRepository employeeRepository;

    public SeparationService(SeparationRepository separationRepository,
                             EmployeeRepository employeeRepository) {
        this.separationRepository = separationRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<SeparationResponse> getAll(Pageable pageable) {
        return separationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public SeparationResponse getById(Long id) {
        var separation = separationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Separation", "id", id));
        return mapToResponse(separation);
    }

    @Transactional(readOnly = true)
    public List<SeparationResponse> getByEmployeeId(Long employeeId) {
        return separationRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeparationResponse> getPendingSeparations() {
        return separationRepository.findByStatus(SeparationStatus.PENDING).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SeparationResponse create(SeparationRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var separation = new Separation();
        separation.setEmployee(employee);
        try {
            separation.setSeparationType(SeparationType.valueOf(request.separationType()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid separation type: " + request.separationType());
        }
        separation.setReason(request.reason());
        separation.setNoticeDate(request.noticeDate());
        separation.setLastWorkingDay(request.lastWorkingDay());
        separation.setStatus(SeparationStatus.PENDING);

        var saved = separationRepository.save(separation);
        log.info("Created separation with id: {} for employee id: {}", saved.getId(), employee.getId());
        return mapToResponse(saved);
    }

    public SeparationResponse approve(Long id, Long approvedById) {
        var separation = separationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Separation", "id", id));

        if (separation.getStatus() != SeparationStatus.PENDING) {
            throw new BadRequestException("Separation can only be approved when in PENDING status");
        }

        var approver = employeeRepository.findById(approvedById)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", approvedById));

        separation.setStatus(SeparationStatus.APPROVED);
        separation.setApprovedBy(approver);
        separation.setApprovedAt(LocalDateTime.now());

        var saved = separationRepository.save(separation);
        log.info("Approved separation with id: {} by employee id: {}", saved.getId(), approvedById);
        return mapToResponse(saved);
    }

    public SeparationResponse reject(Long id, Long approvedById) {
        var separation = separationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Separation", "id", id));

        if (separation.getStatus() != SeparationStatus.PENDING) {
            throw new BadRequestException("Separation can only be rejected when in PENDING status");
        }

        var approver = employeeRepository.findById(approvedById)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", approvedById));

        separation.setStatus(SeparationStatus.REJECTED);
        separation.setApprovedBy(approver);
        separation.setApprovedAt(LocalDateTime.now());

        var saved = separationRepository.save(separation);
        log.info("Rejected separation with id: {} by employee id: {}", saved.getId(), approvedById);
        return mapToResponse(saved);
    }

    public SeparationResponse finalizeSeparation(Long id) {
        var separation = separationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Separation", "id", id));

        if (separation.getStatus() != SeparationStatus.APPROVED) {
            throw new BadRequestException("Separation must be in APPROVED status before finalization");
        }

        separation.setStatus(SeparationStatus.FINALIZED);

        var employee = separation.getEmployee();
        employee.setEmploymentStatus(EmploymentStatus.INACTIVE);
        employeeRepository.save(employee);

        var saved = separationRepository.save(separation);
        log.info("Finalized separation with id: {} for employee id: {}", saved.getId(), employee.getId());
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var separation = separationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Separation", "id", id));

        if (separation.getStatus() != SeparationStatus.PENDING) {
            throw new BadRequestException("Only separations in PENDING status can be deleted");
        }

        separationRepository.delete(separation);
        log.info("Deleted separation with id: {}", id);
    }

    private SeparationResponse mapToResponse(Separation separation) {
        return new SeparationResponse(
                separation.getId(),
                separation.getEmployee().getId(),
                separation.getEmployee().getEmployeeCode(),
                separation.getEmployee().getFirstName() + " " + separation.getEmployee().getLastName(),
                separation.getSeparationType().name(),
                separation.getReason(),
                separation.getNoticeDate(),
                separation.getLastWorkingDay(),
                separation.getStatus().name(),
                separation.getApprovedBy() != null ? separation.getApprovedBy().getId() : null,
                separation.getApprovedBy() != null
                        ? separation.getApprovedBy().getFirstName() + " " + separation.getApprovedBy().getLastName()
                        : null,
                separation.getApprovedAt(),
                separation.getCreatedAt(),
                separation.getUpdatedAt()
        );
    }
}
