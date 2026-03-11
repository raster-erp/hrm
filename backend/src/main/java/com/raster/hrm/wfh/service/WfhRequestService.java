package com.raster.hrm.wfh.service;

import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.wfh.dto.WfhApprovalRequest;
import com.raster.hrm.wfh.dto.WfhDashboardResponse;
import com.raster.hrm.wfh.dto.WfhRequestCreateRequest;
import com.raster.hrm.wfh.dto.WfhRequestResponse;
import com.raster.hrm.wfh.entity.WfhRequest;
import com.raster.hrm.wfh.entity.WfhStatus;
import com.raster.hrm.wfh.repository.WfhActivityLogRepository;
import com.raster.hrm.wfh.repository.WfhRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WfhRequestService {

    private static final Logger log = LoggerFactory.getLogger(WfhRequestService.class);

    private final WfhRequestRepository wfhRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final WfhActivityLogRepository wfhActivityLogRepository;

    public WfhRequestService(WfhRequestRepository wfhRequestRepository,
                              EmployeeRepository employeeRepository,
                              WfhActivityLogRepository wfhActivityLogRepository) {
        this.wfhRequestRepository = wfhRequestRepository;
        this.employeeRepository = employeeRepository;
        this.wfhActivityLogRepository = wfhActivityLogRepository;
    }

    @Transactional(readOnly = true)
    public Page<WfhRequestResponse> getAll(Pageable pageable) {
        return wfhRequestRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public WfhRequestResponse getById(Long id) {
        var request = wfhRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WfhRequest", "id", id));
        return mapToResponse(request);
    }

    @Transactional(readOnly = true)
    public Page<WfhRequestResponse> getByEmployeeId(Long employeeId, Pageable pageable) {
        return wfhRequestRepository.findByEmployeeId(employeeId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<WfhRequestResponse> getByStatus(WfhStatus status, Pageable pageable) {
        return wfhRequestRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<WfhRequestResponse> getByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return wfhRequestRepository.findByDateRange(startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    public WfhRequestResponse create(WfhRequestCreateRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var wfhRequest = new WfhRequest();
        wfhRequest.setEmployee(employee);
        wfhRequest.setRequestDate(request.requestDate());
        wfhRequest.setReason(request.reason());
        wfhRequest.setRemarks(request.remarks());

        var saved = wfhRequestRepository.save(wfhRequest);
        log.info("Created WFH request with id: {} for employee: {}", saved.getId(), employee.getEmployeeCode());
        return mapToResponse(saved);
    }

    public WfhRequestResponse update(Long id, WfhRequestCreateRequest request) {
        var wfhRequest = wfhRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WfhRequest", "id", id));

        if (wfhRequest.getStatus() != WfhStatus.PENDING) {
            throw new BadRequestException("Cannot update WFH request with status: " + wfhRequest.getStatus());
        }

        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        wfhRequest.setEmployee(employee);
        wfhRequest.setRequestDate(request.requestDate());
        wfhRequest.setReason(request.reason());
        wfhRequest.setRemarks(request.remarks());

        var saved = wfhRequestRepository.save(wfhRequest);
        log.info("Updated WFH request with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public WfhRequestResponse approve(Long id, WfhApprovalRequest request) {
        var wfhRequest = wfhRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WfhRequest", "id", id));

        if (wfhRequest.getStatus() != WfhStatus.PENDING) {
            throw new BadRequestException("Cannot change status of WFH request with status: " + wfhRequest.getStatus());
        }

        var newStatus = WfhStatus.valueOf(request.status());
        if (newStatus != WfhStatus.APPROVED && newStatus != WfhStatus.REJECTED) {
            throw new BadRequestException("Status must be APPROVED or REJECTED");
        }

        wfhRequest.setStatus(newStatus);
        wfhRequest.setApprovedBy(request.approvedBy());
        wfhRequest.setApprovedAt(LocalDateTime.now());
        if (request.remarks() != null) {
            wfhRequest.setRemarks(request.remarks());
        }

        var saved = wfhRequestRepository.save(wfhRequest);
        log.info("Updated WFH request id: {} status to {}", saved.getId(), newStatus);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var wfhRequest = wfhRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WfhRequest", "id", id));
        wfhRequestRepository.delete(wfhRequest);
        log.info("Deleted WFH request with id: {}", id);
    }

    @Transactional(readOnly = true)
    public List<WfhDashboardResponse> getDashboard(LocalDate startDate, LocalDate endDate) {
        var requests = wfhRequestRepository.findByDateRange(startDate, endDate, Pageable.unpaged()).getContent();

        return requests.stream()
                .collect(Collectors.groupingBy(r -> r.getEmployee().getId()))
                .entrySet().stream()
                .map(entry -> {
                    var empRequests = entry.getValue();
                    var employee = empRequests.get(0).getEmployee();

                    int totalRequests = empRequests.size();

                    int approvedRequests = (int) empRequests.stream()
                            .filter(r -> r.getStatus() == WfhStatus.APPROVED)
                            .count();

                    int pendingRequests = (int) empRequests.stream()
                            .filter(r -> r.getStatus() == WfhStatus.PENDING)
                            .count();

                    int rejectedRequests = (int) empRequests.stream()
                            .filter(r -> r.getStatus() == WfhStatus.REJECTED)
                            .count();

                    var todayLogs = wfhActivityLogRepository
                            .findByEmployeeIdAndDate(employee.getId(), LocalDate.now());
                    boolean checkedInToday = !todayLogs.isEmpty();

                    return new WfhDashboardResponse(
                            employee.getId(),
                            employee.getEmployeeCode(),
                            employee.getFirstName() + " " + employee.getLastName(),
                            totalRequests,
                            approvedRequests,
                            pendingRequests,
                            rejectedRequests,
                            checkedInToday
                    );
                })
                .toList();
    }

    private WfhRequestResponse mapToResponse(WfhRequest request) {
        var employee = request.getEmployee();
        return new WfhRequestResponse(
                request.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                request.getRequestDate(),
                request.getReason(),
                request.getStatus().name(),
                request.getApprovedBy(),
                request.getApprovedAt(),
                request.getRemarks(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
