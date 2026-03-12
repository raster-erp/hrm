package com.raster.hrm.leaveencashment.service;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leavebalance.entity.LeaveBalance;
import com.raster.hrm.leavebalance.repository.LeaveBalanceRepository;
import com.raster.hrm.leavebalance.service.LeaveBalanceService;
import com.raster.hrm.leaveencashment.dto.EncashmentEligibilityResponse;
import com.raster.hrm.leaveencashment.dto.LeaveEncashmentApprovalRequest;
import com.raster.hrm.leaveencashment.dto.LeaveEncashmentRequest;
import com.raster.hrm.leaveencashment.dto.LeaveEncashmentResponse;
import com.raster.hrm.leaveencashment.entity.EncashmentStatus;
import com.raster.hrm.leaveencashment.entity.LeaveEncashment;
import com.raster.hrm.leaveencashment.repository.LeaveEncashmentRepository;
import com.raster.hrm.leavetype.entity.LeaveType;
import com.raster.hrm.leavetype.repository.LeaveTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LeaveEncashmentService {

    private static final Logger log = LoggerFactory.getLogger(LeaveEncashmentService.class);
    private static final BigDecimal DAYS_IN_MONTH = new BigDecimal("30");

    private final LeaveEncashmentRepository leaveEncashmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveBalanceService leaveBalanceService;

    public LeaveEncashmentService(LeaveEncashmentRepository leaveEncashmentRepository,
                                  EmployeeRepository employeeRepository,
                                  LeaveTypeRepository leaveTypeRepository,
                                  LeaveBalanceRepository leaveBalanceRepository,
                                  LeaveBalanceService leaveBalanceService) {
        this.leaveEncashmentRepository = leaveEncashmentRepository;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveBalanceService = leaveBalanceService;
    }

    // ── Eligibility Check ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public EncashmentEligibilityResponse checkEligibility(Long employeeId, Long leaveTypeId, int year) {
        log.debug("Checking encashment eligibility for employee {} type {} year {}", employeeId, leaveTypeId, year);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", leaveTypeId));

        String employeeName = employee.getFirstName() + " " + employee.getLastName();

        if (!leaveType.isEncashable()) {
            return new EncashmentEligibilityResponse(
                    employeeId, employeeName, leaveTypeId, leaveType.getName(), year,
                    false, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    "Leave type '" + leaveType.getName() + "' is not eligible for encashment");
        }

        if (employee.getBasicSalary() == null || employee.getBasicSalary().compareTo(BigDecimal.ZERO) <= 0) {
            return new EncashmentEligibilityResponse(
                    employeeId, employeeName, leaveTypeId, leaveType.getName(), year,
                    false, BigDecimal.ZERO, leaveType.getMinEncashmentBalance(), BigDecimal.ZERO, BigDecimal.ZERO,
                    "Employee does not have basic salary configured");
        }

        BigDecimal availableBalance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .map(LeaveBalance::getAvailable)
                .orElse(BigDecimal.ZERO);

        BigDecimal minRequired = leaveType.getMinEncashmentBalance();
        BigDecimal perDaySalary = employee.getBasicSalary().divide(DAYS_IN_MONTH, 2, RoundingMode.HALF_UP);

        if (availableBalance.compareTo(minRequired) < 0) {
            return new EncashmentEligibilityResponse(
                    employeeId, employeeName, leaveTypeId, leaveType.getName(), year,
                    false, availableBalance, minRequired, BigDecimal.ZERO, perDaySalary,
                    "Insufficient leave balance. Available: " + availableBalance + ", minimum required: " + minRequired);
        }

        BigDecimal maxEncashableDays = availableBalance.subtract(minRequired);

        return new EncashmentEligibilityResponse(
                employeeId, employeeName, leaveTypeId, leaveType.getName(), year,
                true, availableBalance, minRequired, maxEncashableDays, perDaySalary,
                "Eligible for encashment");
    }

    // ── Create Encashment Request ───────────────────────────────────────

    public LeaveEncashmentResponse createRequest(LeaveEncashmentRequest request) {
        log.info("Creating encashment request for employee {} type {} days {}",
                request.employeeId(), request.leaveTypeId(), request.numberOfDays());

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        LeaveType leaveType = leaveTypeRepository.findById(request.leaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", request.leaveTypeId()));

        if (!leaveType.isEncashable()) {
            throw new BadRequestException("Leave type '" + leaveType.getName() + "' is not eligible for encashment");
        }

        if (employee.getBasicSalary() == null || employee.getBasicSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Employee does not have basic salary configured");
        }

        int year = LocalDate.now().getYear();

        // Check for existing PENDING or APPROVED encashment
        leaveEncashmentRepository.findByEmployeeIdAndLeaveTypeIdAndYearAndStatusIn(
                request.employeeId(), request.leaveTypeId(), year,
                List.of(EncashmentStatus.PENDING, EncashmentStatus.APPROVED)
        ).ifPresent(existing -> {
            throw new BadRequestException("An encashment request already exists for this employee, leave type, and year with status: " + existing.getStatus());
        });

        BigDecimal availableBalance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(request.employeeId(), request.leaveTypeId(), year)
                .map(LeaveBalance::getAvailable)
                .orElse(BigDecimal.ZERO);

        BigDecimal minRequired = leaveType.getMinEncashmentBalance();
        BigDecimal maxEncashableDays = availableBalance.subtract(minRequired);

        if (maxEncashableDays.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Insufficient leave balance for encashment. Available: "
                    + availableBalance + ", minimum required balance: " + minRequired);
        }

        if (request.numberOfDays().compareTo(maxEncashableDays) > 0) {
            throw new BadRequestException("Requested days (" + request.numberOfDays()
                    + ") exceeds maximum encashable days (" + maxEncashableDays + ")");
        }

        BigDecimal perDaySalary = employee.getBasicSalary().divide(DAYS_IN_MONTH, 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = perDaySalary.multiply(request.numberOfDays()).setScale(2, RoundingMode.HALF_UP);

        var encashment = new LeaveEncashment();
        encashment.setEmployee(employee);
        encashment.setLeaveType(leaveType);
        encashment.setYear(year);
        encashment.setNumberOfDays(request.numberOfDays());
        encashment.setPerDaySalary(perDaySalary);
        encashment.setTotalAmount(totalAmount);
        encashment.setStatus(EncashmentStatus.PENDING);
        encashment.setRemarks(request.remarks());

        LeaveEncashment saved = leaveEncashmentRepository.save(encashment);
        log.info("Encashment request created with id {} for employee {}", saved.getId(), request.employeeId());
        return toResponse(saved);
    }

    // ── Approve / Reject ────────────────────────────────────────────────

    public LeaveEncashmentResponse approve(Long id, LeaveEncashmentApprovalRequest request) {
        log.info("Processing encashment approval for id {} with status {}", id, request.status());

        LeaveEncashment encashment = leaveEncashmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveEncashment", "id", id));

        if (encashment.getStatus() != EncashmentStatus.PENDING) {
            throw new BadRequestException("Only PENDING encashments can be approved or rejected. Current status: " + encashment.getStatus());
        }

        if (request.status() != EncashmentStatus.APPROVED && request.status() != EncashmentStatus.REJECTED) {
            throw new BadRequestException("Approval status must be either APPROVED or REJECTED");
        }

        encashment.setStatus(request.status());
        encashment.setApprovedBy(request.approvedBy());
        encashment.setApprovedAt(LocalDateTime.now());

        if (request.remarks() != null) {
            encashment.setRemarks(request.remarks());
        }

        if (request.status() == EncashmentStatus.APPROVED) {
            leaveBalanceService.recordEncashment(
                    encashment.getEmployee(),
                    encashment.getLeaveType(),
                    encashment.getNumberOfDays(),
                    encashment.getId());
        }

        LeaveEncashment saved = leaveEncashmentRepository.save(encashment);
        log.info("Encashment {} {} by {}", id, request.status(), request.approvedBy());
        return toResponse(saved);
    }

    // ── Mark as Paid ────────────────────────────────────────────────────

    public LeaveEncashmentResponse markAsPaid(Long id, String approvedBy) {
        log.info("Marking encashment {} as paid", id);

        LeaveEncashment encashment = leaveEncashmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveEncashment", "id", id));

        if (encashment.getStatus() != EncashmentStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED encashments can be marked as PAID. Current status: " + encashment.getStatus());
        }

        encashment.setStatus(EncashmentStatus.PAID);
        encashment.setApprovedBy(approvedBy);
        encashment.setApprovedAt(LocalDateTime.now());

        LeaveEncashment saved = leaveEncashmentRepository.save(encashment);
        log.info("Encashment {} marked as PAID", id);
        return toResponse(saved);
    }

    // ── Queries ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LeaveEncashmentResponse getById(Long id) {
        log.debug("Fetching encashment by id {}", id);
        return leaveEncashmentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveEncashment", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<LeaveEncashmentResponse> getByEmployee(Long employeeId, Pageable pageable) {
        log.debug("Fetching encashments for employee {}", employeeId);
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        return leaveEncashmentRepository.findByEmployeeId(employeeId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LeaveEncashmentResponse> getByStatus(EncashmentStatus status, Pageable pageable) {
        log.debug("Fetching encashments by status {}", status);
        return leaveEncashmentRepository.findByStatus(status, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LeaveEncashmentResponse> getAll(Pageable pageable) {
        log.debug("Fetching all encashments");
        return leaveEncashmentRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // ── Private helpers ─────────────────────────────────────────────────

    private LeaveEncashmentResponse toResponse(LeaveEncashment encashment) {
        Employee employee = encashment.getEmployee();
        LeaveType leaveType = encashment.getLeaveType();
        return new LeaveEncashmentResponse(
                encashment.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                leaveType.getId(),
                leaveType.getName(),
                encashment.getYear(),
                encashment.getNumberOfDays(),
                encashment.getPerDaySalary(),
                encashment.getTotalAmount(),
                encashment.getStatus().name(),
                encashment.getApprovedBy(),
                encashment.getApprovedAt(),
                encashment.getRemarks(),
                encashment.getCreatedAt(),
                encashment.getUpdatedAt()
        );
    }
}
