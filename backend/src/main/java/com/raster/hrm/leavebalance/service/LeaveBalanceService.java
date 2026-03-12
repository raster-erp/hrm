package com.raster.hrm.leavebalance.service;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leavebalance.dto.BalanceAdjustmentRequest;
import com.raster.hrm.leavebalance.dto.LeaveBalanceResponse;
import com.raster.hrm.leavebalance.dto.LeaveTransactionResponse;
import com.raster.hrm.leavebalance.dto.YearEndProcessingRequest;
import com.raster.hrm.leavebalance.dto.YearEndSummaryResponse;
import com.raster.hrm.leavebalance.entity.LeaveBalance;
import com.raster.hrm.leavebalance.entity.LeaveTransaction;
import com.raster.hrm.leavebalance.entity.ReferenceType;
import com.raster.hrm.leavebalance.entity.TransactionType;
import com.raster.hrm.leavebalance.repository.LeaveBalanceRepository;
import com.raster.hrm.leavebalance.repository.LeaveTransactionRepository;
import com.raster.hrm.leavepolicy.entity.LeavePolicy;
import com.raster.hrm.leavepolicy.repository.LeavePolicyRepository;
import com.raster.hrm.leavepolicyassignment.entity.AssignmentType;
import com.raster.hrm.leavepolicyassignment.entity.LeavePolicyAssignment;
import com.raster.hrm.leavepolicyassignment.repository.LeavePolicyAssignmentRepository;
import com.raster.hrm.leavetype.entity.LeaveType;
import com.raster.hrm.leavetype.repository.LeaveTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class LeaveBalanceService {

    private static final Logger log = LoggerFactory.getLogger(LeaveBalanceService.class);

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTransactionRepository leaveTransactionRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeavePolicyAssignmentRepository leavePolicyAssignmentRepository;

    public LeaveBalanceService(LeaveBalanceRepository leaveBalanceRepository,
                               LeaveTransactionRepository leaveTransactionRepository,
                               EmployeeRepository employeeRepository,
                               LeaveTypeRepository leaveTypeRepository,
                               LeavePolicyRepository leavePolicyRepository,
                               LeavePolicyAssignmentRepository leavePolicyAssignmentRepository) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveTransactionRepository = leaveTransactionRepository;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leavePolicyRepository = leavePolicyRepository;
        this.leavePolicyAssignmentRepository = leavePolicyAssignmentRepository;
    }

    // ── Balance Inquiry ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getBalancesByEmployee(Long employeeId, int year) {
        log.debug("Fetching leave balances for employee {} in year {}", employeeId, year);
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year)
                .stream()
                .map(this::toBalanceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaveBalanceResponse getBalance(Long employeeId, Long leaveTypeId, int year) {
        log.debug("Fetching leave balance for employee {} type {} year {}", employeeId, leaveTypeId, year);
        return leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .map(this::toBalanceResponse)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveBalance",
                        "employeeId/leaveTypeId/year", employeeId + "/" + leaveTypeId + "/" + year));
    }

    // ── Transaction History ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<LeaveTransactionResponse> getTransactions(Long employeeId, Long leaveTypeId,
                                                          TransactionType transactionType, Pageable pageable) {
        log.debug("Fetching transactions for employee {} type {} txnType {}", employeeId, leaveTypeId, transactionType);

        if (leaveTypeId != null && transactionType != null) {
            return leaveTransactionRepository
                    .findByEmployeeIdAndLeaveTypeIdAndTransactionType(employeeId, leaveTypeId, transactionType, pageable)
                    .map(this::toTransactionResponse);
        } else if (leaveTypeId != null) {
            return leaveTransactionRepository
                    .findByEmployeeIdAndLeaveTypeId(employeeId, leaveTypeId, pageable)
                    .map(this::toTransactionResponse);
        } else if (transactionType != null) {
            return leaveTransactionRepository
                    .findByEmployeeIdAndTransactionType(employeeId, transactionType, pageable)
                    .map(this::toTransactionResponse);
        } else {
            return leaveTransactionRepository
                    .findByEmployeeId(employeeId, pageable)
                    .map(this::toTransactionResponse);
        }
    }

    // ── Balance Adjustment (HR manual correction) ───────────────────────

    public LeaveBalanceResponse adjustBalance(BalanceAdjustmentRequest request) {
        log.info("Adjusting balance for employee {} type {} year {} by {}",
                request.employeeId(), request.leaveTypeId(), request.year(), request.amount());

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        LeaveType leaveType = leaveTypeRepository.findById(request.leaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", request.leaveTypeId()));

        LeaveBalance balance = getOrCreateBalance(employee, leaveType, request.year());

        balance.setCredited(balance.getCredited().add(request.amount()));
        balance.setAvailable(balance.getCredited().add(balance.getCarryForwarded())
                .subtract(balance.getUsed()).subtract(balance.getPending()).subtract(balance.getEncashed()));

        LeaveBalance saved = leaveBalanceRepository.save(balance);

        recordTransaction(employee, leaveType, TransactionType.ADJUSTMENT,
                request.amount(), saved.getAvailable(),
                ReferenceType.MANUAL, null,
                request.description() != null ? request.description() : "Manual balance adjustment",
                request.adjustedBy());

        log.info("Balance adjusted for employee {} type {} year {}", request.employeeId(), request.leaveTypeId(), request.year());
        return toBalanceResponse(saved);
    }

    // ── Year-End Processing ─────────────────────────────────────────────

    public YearEndSummaryResponse processYearEnd(YearEndProcessingRequest request) {
        int processedYear = request.year();
        int nextYear = processedYear + 1;
        log.info("Starting year-end processing for year {}", processedYear);

        List<LeaveBalance> balances = leaveBalanceRepository.findByYear(processedYear);

        if (balances.isEmpty()) {
            throw new BadRequestException("No leave balances found for year " + processedYear);
        }

        Set<Long> processedEmployees = new HashSet<>();
        int balancesCreated = 0;
        BigDecimal totalCarryForwarded = BigDecimal.ZERO;
        BigDecimal totalLapsed = BigDecimal.ZERO;

        for (LeaveBalance balance : balances) {
            Employee employee = balance.getEmployee();
            LeaveType leaveType = balance.getLeaveType();
            processedEmployees.add(employee.getId());

            BigDecimal remaining = balance.getAvailable();
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal carryForwardLimit = getCarryForwardLimit(employee, leaveType);
            BigDecimal carryForward = remaining.min(carryForwardLimit);
            BigDecimal lapsed = remaining.subtract(carryForward);

            if (lapsed.compareTo(BigDecimal.ZERO) > 0) {
                recordTransaction(employee, leaveType, TransactionType.LAPSE,
                        lapsed.negate(), balance.getAvailable().subtract(lapsed),
                        ReferenceType.YEAR_END, null,
                        "Year-end lapse for " + processedYear,
                        request.processedBy());
                totalLapsed = totalLapsed.add(lapsed);
            }

            if (carryForward.compareTo(BigDecimal.ZERO) > 0) {
                LeaveBalance nextYearBalance = getOrCreateBalance(employee, leaveType, nextYear);
                nextYearBalance.setCarryForwarded(carryForward);
                nextYearBalance.setAvailable(nextYearBalance.getCredited().add(carryForward)
                        .subtract(nextYearBalance.getUsed()).subtract(nextYearBalance.getPending()).subtract(nextYearBalance.getEncashed()));
                leaveBalanceRepository.save(nextYearBalance);
                balancesCreated++;

                recordTransaction(employee, leaveType, TransactionType.CARRY_FORWARD,
                        carryForward, nextYearBalance.getAvailable(),
                        ReferenceType.YEAR_END, null,
                        "Carry forward from " + processedYear + " to " + nextYear,
                        request.processedBy());
                totalCarryForwarded = totalCarryForwarded.add(carryForward);
            }
        }

        log.info("Year-end processing completed for year {}. Employees: {}, Balances created: {}, Carry-forwarded: {}, Lapsed: {}",
                processedYear, processedEmployees.size(), balancesCreated, totalCarryForwarded, totalLapsed);

        return new YearEndSummaryResponse(
                processedYear, nextYear,
                processedEmployees.size(), balancesCreated,
                totalCarryForwarded, totalLapsed
        );
    }

    // ── Called by LeaveApplicationService on approval/cancellation ───────

    public void recordLeaveApproval(Employee employee, LeaveType leaveType, BigDecimal days, Long applicationId) {
        int year = java.time.LocalDate.now().getYear();
        LeaveBalance balance = getOrCreateBalance(employee, leaveType, year);

        balance.setPending(balance.getPending().subtract(days));
        balance.setUsed(balance.getUsed().add(days));
        balance.setAvailable(balance.getCredited().add(balance.getCarryForwarded())
                .subtract(balance.getUsed()).subtract(balance.getPending()).subtract(balance.getEncashed()));

        leaveBalanceRepository.save(balance);

        recordTransaction(employee, leaveType, TransactionType.DEBIT,
                days.negate(), balance.getAvailable(),
                ReferenceType.LEAVE_APPLICATION, applicationId,
                "Leave approved - " + days + " day(s)", null);
    }

    public void recordLeaveSubmission(Employee employee, LeaveType leaveType, BigDecimal days, Long applicationId) {
        int year = java.time.LocalDate.now().getYear();
        LeaveBalance balance = getOrCreateBalance(employee, leaveType, year);

        balance.setPending(balance.getPending().add(days));
        balance.setAvailable(balance.getCredited().add(balance.getCarryForwarded())
                .subtract(balance.getUsed()).subtract(balance.getPending()).subtract(balance.getEncashed()));

        leaveBalanceRepository.save(balance);

        recordTransaction(employee, leaveType, TransactionType.PENDING_DEBIT,
                days.negate(), balance.getAvailable(),
                ReferenceType.LEAVE_APPLICATION, applicationId,
                "Leave submitted - " + days + " day(s) pending", null);
    }

    public void recordLeaveCancellation(Employee employee, LeaveType leaveType, BigDecimal days,
                                        Long applicationId, boolean wasApproved) {
        int year = java.time.LocalDate.now().getYear();
        LeaveBalance balance = getOrCreateBalance(employee, leaveType, year);

        if (wasApproved) {
            balance.setUsed(balance.getUsed().subtract(days));
        } else {
            balance.setPending(balance.getPending().subtract(days));
        }
        balance.setAvailable(balance.getCredited().add(balance.getCarryForwarded())
                .subtract(balance.getUsed()).subtract(balance.getPending()).subtract(balance.getEncashed()));

        leaveBalanceRepository.save(balance);

        recordTransaction(employee, leaveType, TransactionType.PENDING_REVERSAL,
                days, balance.getAvailable(),
                ReferenceType.LEAVE_APPLICATION, applicationId,
                "Leave cancelled - " + days + " day(s) restored", null);
    }

    // ── Called by LeaveEncashmentService on encashment approval ──────────

    public void recordEncashment(Employee employee, LeaveType leaveType, BigDecimal days, Long encashmentId) {
        int year = java.time.LocalDate.now().getYear();
        LeaveBalance balance = getOrCreateBalance(employee, leaveType, year);

        balance.setEncashed(balance.getEncashed().add(days));
        balance.setAvailable(balance.getCredited().add(balance.getCarryForwarded())
                .subtract(balance.getUsed()).subtract(balance.getPending()).subtract(balance.getEncashed()));
        leaveBalanceRepository.save(balance);

        recordTransaction(employee, leaveType, TransactionType.ENCASHMENT,
                days.negate(), balance.getAvailable(),
                ReferenceType.ENCASHMENT, encashmentId,
                "Leave encashment - " + days + " day(s)", null);
    }

    // ── Private helpers ─────────────────────────────────────────────────

    private LeaveBalance getOrCreateBalance(Employee employee, LeaveType leaveType, int year) {
        return leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employee.getId(), leaveType.getId(), year)
                .orElseGet(() -> {
                    var balance = new LeaveBalance();
                    balance.setEmployee(employee);
                    balance.setLeaveType(leaveType);
                    balance.setYear(year);
                    return leaveBalanceRepository.save(balance);
                });
    }

    private void recordTransaction(Employee employee, LeaveType leaveType, TransactionType type,
                                   BigDecimal amount, BigDecimal balanceAfter,
                                   ReferenceType refType, Long refId,
                                   String description, String createdBy) {
        var txn = new LeaveTransaction();
        txn.setEmployee(employee);
        txn.setLeaveType(leaveType);
        txn.setTransactionType(type);
        txn.setAmount(amount);
        txn.setBalanceAfter(balanceAfter);
        txn.setReferenceType(refType);
        txn.setReferenceId(refId);
        txn.setDescription(description);
        txn.setCreatedBy(createdBy);
        leaveTransactionRepository.save(txn);
    }

    private BigDecimal getCarryForwardLimit(Employee employee, LeaveType leaveType) {
        List<LeavePolicy> policies = leavePolicyRepository.findByLeaveTypeId(leaveType.getId());
        if (policies.isEmpty()) {
            return BigDecimal.ZERO;
        }

        for (LeavePolicy policy : policies) {
            if (!policy.isActive()) {
                continue;
            }
            List<LeavePolicyAssignment> assignments = leavePolicyAssignmentRepository.findByLeavePolicyId(policy.getId());
            for (LeavePolicyAssignment assignment : assignments) {
                if (!assignment.isActive()) {
                    continue;
                }
                if (matchesAssignment(employee, assignment)) {
                    BigDecimal limit = policy.getCarryForwardLimit();
                    return limit != null ? limit : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private boolean matchesAssignment(Employee employee, LeavePolicyAssignment assignment) {
        if (assignment.getAssignmentType() == AssignmentType.INDIVIDUAL) {
            return employee.getId().equals(assignment.getEmployeeId());
        } else if (assignment.getAssignmentType() == AssignmentType.DEPARTMENT) {
            return employee.getDepartment() != null
                    && employee.getDepartment().getId().equals(assignment.getDepartmentId());
        } else if (assignment.getAssignmentType() == AssignmentType.DESIGNATION) {
            return employee.getDesignation() != null
                    && employee.getDesignation().getId().equals(assignment.getDesignationId());
        }
        return false;
    }

    private LeaveBalanceResponse toBalanceResponse(LeaveBalance balance) {
        Employee employee = balance.getEmployee();
        LeaveType leaveType = balance.getLeaveType();
        return new LeaveBalanceResponse(
                balance.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                leaveType.getId(),
                leaveType.getName(),
                leaveType.getCode(),
                balance.getYear(),
                balance.getCredited(),
                balance.getUsed(),
                balance.getPending(),
                balance.getAvailable(),
                balance.getCarryForwarded(),
                balance.getEncashed(),
                balance.getCreatedAt(),
                balance.getUpdatedAt()
        );
    }

    private LeaveTransactionResponse toTransactionResponse(LeaveTransaction txn) {
        Employee employee = txn.getEmployee();
        LeaveType leaveType = txn.getLeaveType();
        return new LeaveTransactionResponse(
                txn.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                leaveType.getId(),
                leaveType.getName(),
                txn.getTransactionType().name(),
                txn.getAmount(),
                txn.getBalanceAfter(),
                txn.getReferenceType() != null ? txn.getReferenceType().name() : null,
                txn.getReferenceId(),
                txn.getDescription(),
                txn.getCreatedBy(),
                txn.getCreatedAt()
        );
    }
}
