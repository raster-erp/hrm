package com.raster.hrm.leaveapplication.service;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leaveapplication.dto.LeaveApplicationRequest;
import com.raster.hrm.leaveapplication.dto.LeaveApplicationResponse;
import com.raster.hrm.leaveapplication.dto.LeaveApprovalRequest;
import com.raster.hrm.leaveapplication.entity.LeaveApplication;
import com.raster.hrm.leaveapplication.entity.LeaveApplicationStatus;
import com.raster.hrm.leaveapplication.entity.LeaveApprovalLog;
import com.raster.hrm.leaveapplication.repository.LeaveApplicationRepository;
import com.raster.hrm.leaveapplication.repository.LeaveApprovalLogRepository;
import com.raster.hrm.leavetype.entity.LeaveType;
import com.raster.hrm.leavetype.repository.LeaveTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class LeaveApplicationService {

    private static final Logger log = LoggerFactory.getLogger(LeaveApplicationService.class);

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveApprovalLogRepository leaveApprovalLogRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveApplicationNotificationService notificationService;

    public LeaveApplicationService(LeaveApplicationRepository leaveApplicationRepository,
                                   LeaveApprovalLogRepository leaveApprovalLogRepository,
                                   EmployeeRepository employeeRepository,
                                   LeaveTypeRepository leaveTypeRepository,
                                   LeaveApplicationNotificationService notificationService) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveApprovalLogRepository = leaveApprovalLogRepository;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Page<LeaveApplicationResponse> getAll(Pageable pageable) {
        log.debug("Fetching all leave applications");
        return leaveApplicationRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public LeaveApplicationResponse getById(Long id) {
        log.debug("Fetching leave application with id: {}", id);
        return leaveApplicationRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveApplication", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<LeaveApplicationResponse> getByEmployee(Long employeeId, Pageable pageable) {
        log.debug("Fetching leave applications for employee: {}", employeeId);
        return leaveApplicationRepository.findByEmployeeId(employeeId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LeaveApplicationResponse> getByStatus(LeaveApplicationStatus status, Pageable pageable) {
        log.debug("Fetching leave applications with status: {}", status);
        return leaveApplicationRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LeaveApplicationResponse> getByLeaveType(Long leaveTypeId, Pageable pageable) {
        log.debug("Fetching leave applications for leave type: {}", leaveTypeId);
        return leaveApplicationRepository.findByLeaveTypeId(leaveTypeId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LeaveApplicationResponse> getByDateRange(LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        log.debug("Fetching leave applications from {} to {}", fromDate, toDate);
        return leaveApplicationRepository
                .findByFromDateGreaterThanEqualAndToDateLessThanEqual(fromDate, toDate, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LeaveApplicationResponse> getByEmployeeAndDateRange(Long employeeId, LocalDate fromDate,
                                                                     LocalDate toDate, Pageable pageable) {
        log.debug("Fetching leave applications for employee {} from {} to {}", employeeId, fromDate, toDate);
        return leaveApplicationRepository
                .findByEmployeeIdAndFromDateGreaterThanEqualAndToDateLessThanEqual(employeeId, fromDate, toDate, pageable)
                .map(this::toResponse);
    }

    public LeaveApplicationResponse create(LeaveApplicationRequest request) {
        log.info("Creating leave application for employee: {}", request.employeeId());

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        LeaveType leaveType = leaveTypeRepository.findById(request.leaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", request.leaveTypeId()));

        if (request.fromDate().isAfter(request.toDate())) {
            throw new BadRequestException("From date must be before or equal to to date");
        }

        LeaveApplication application = new LeaveApplication();
        application.setEmployee(employee);
        application.setLeaveType(leaveType);
        application.setFromDate(request.fromDate());
        application.setToDate(request.toDate());
        application.setNumberOfDays(request.numberOfDays());
        application.setReason(request.reason());
        application.setRemarks(request.remarks());
        application.setStatus(LeaveApplicationStatus.PENDING);

        LeaveApplication saved = leaveApplicationRepository.save(application);
        log.info("Leave application created with id: {}", saved.getId());
        notificationService.notifyApplicationSubmitted(saved);
        return toResponse(saved);
    }

    public LeaveApplicationResponse update(Long id, LeaveApplicationRequest request) {
        log.info("Updating leave application with id: {}", id);

        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveApplication", "id", id));

        if (application.getStatus() != LeaveApplicationStatus.PENDING) {
            throw new BadRequestException("Only PENDING leave applications can be updated");
        }

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        LeaveType leaveType = leaveTypeRepository.findById(request.leaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", request.leaveTypeId()));

        if (request.fromDate().isAfter(request.toDate())) {
            throw new BadRequestException("From date must be before or equal to to date");
        }

        application.setEmployee(employee);
        application.setLeaveType(leaveType);
        application.setFromDate(request.fromDate());
        application.setToDate(request.toDate());
        application.setNumberOfDays(request.numberOfDays());
        application.setReason(request.reason());
        application.setRemarks(request.remarks());

        LeaveApplication saved = leaveApplicationRepository.save(application);
        log.info("Leave application updated with id: {}", saved.getId());
        return toResponse(saved);
    }

    public LeaveApplicationResponse approve(Long id, LeaveApprovalRequest request) {
        log.info("Processing approval for leave application with id: {}", id);

        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveApplication", "id", id));

        if (application.getStatus() != LeaveApplicationStatus.PENDING) {
            throw new BadRequestException("Only PENDING leave applications can be approved or rejected");
        }

        if (request.status() != LeaveApplicationStatus.APPROVED && request.status() != LeaveApplicationStatus.REJECTED) {
            throw new BadRequestException("Approval status must be APPROVED or REJECTED");
        }

        application.setStatus(request.status());
        application.setApprovedBy(request.approvedBy());
        application.setApprovedAt(LocalDateTime.now());
        application.setApprovalLevel(application.getApprovalLevel() + 1);
        if (request.remarks() != null) {
            application.setRemarks(request.remarks());
        }

        LeaveApplication saved = leaveApplicationRepository.save(application);

        // Create approval log entry
        LeaveApprovalLog logEntry = new LeaveApprovalLog();
        logEntry.setLeaveApplication(saved);
        logEntry.setApproverName(request.approvedBy());
        logEntry.setApprovalLevel(saved.getApprovalLevel());
        logEntry.setAction(request.status().name());
        logEntry.setRemarks(request.remarks());
        leaveApprovalLogRepository.save(logEntry);

        log.info("Leave application {} has been {}", id, request.status());

        if (request.status() == LeaveApplicationStatus.APPROVED) {
            notificationService.notifyApplicationApproved(saved);
        } else {
            notificationService.notifyApplicationRejected(saved);
        }

        return toResponse(saved);
    }

    public LeaveApplicationResponse cancel(Long id) {
        log.info("Cancelling leave application with id: {}", id);

        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveApplication", "id", id));

        if (application.getStatus() != LeaveApplicationStatus.PENDING
                && application.getStatus() != LeaveApplicationStatus.APPROVED) {
            throw new BadRequestException("Only PENDING or APPROVED leave applications can be cancelled");
        }

        application.setStatus(LeaveApplicationStatus.CANCELLED);

        LeaveApplication saved = leaveApplicationRepository.save(application);
        log.info("Leave application cancelled with id: {}", id);
        notificationService.notifyApplicationCancelled(saved);
        return toResponse(saved);
    }

    public void delete(Long id) {
        log.info("Deleting leave application with id: {}", id);

        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveApplication", "id", id));

        leaveApplicationRepository.delete(application);
        log.info("Leave application deleted with id: {}", id);
    }

    private LeaveApplicationResponse toResponse(LeaveApplication application) {
        Employee employee = application.getEmployee();
        LeaveType leaveType = application.getLeaveType();
        return new LeaveApplicationResponse(
                application.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                leaveType.getId(),
                leaveType.getName(),
                application.getFromDate(),
                application.getToDate(),
                application.getNumberOfDays(),
                application.getReason(),
                application.getStatus().name(),
                application.getApprovalLevel(),
                application.getRemarks(),
                application.getApprovedBy(),
                application.getApprovedAt(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
