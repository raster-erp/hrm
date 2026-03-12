package com.raster.hrm.leavebalance;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leavebalance.dto.BalanceAdjustmentRequest;
import com.raster.hrm.leavebalance.dto.YearEndProcessingRequest;
import com.raster.hrm.leavebalance.entity.LeaveBalance;
import com.raster.hrm.leavebalance.entity.LeaveTransaction;
import com.raster.hrm.leavebalance.entity.ReferenceType;
import com.raster.hrm.leavebalance.entity.TransactionType;
import com.raster.hrm.leavebalance.repository.LeaveBalanceRepository;
import com.raster.hrm.leavebalance.repository.LeaveTransactionRepository;
import com.raster.hrm.leavebalance.service.LeaveBalanceService;
import com.raster.hrm.leavepolicy.entity.LeavePolicy;
import com.raster.hrm.leavepolicy.repository.LeavePolicyRepository;
import com.raster.hrm.leavepolicyassignment.entity.AssignmentType;
import com.raster.hrm.leavepolicyassignment.entity.LeavePolicyAssignment;
import com.raster.hrm.leavepolicyassignment.repository.LeavePolicyAssignmentRepository;
import com.raster.hrm.leavetype.entity.LeaveType;
import com.raster.hrm.leavetype.repository.LeaveTypeRepository;
import com.raster.hrm.department.entity.Department;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveBalanceServiceTest {

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private LeaveTransactionRepository leaveTransactionRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private LeavePolicyRepository leavePolicyRepository;

    @Mock
    private LeavePolicyAssignmentRepository leavePolicyAssignmentRepository;

    @InjectMocks
    private LeaveBalanceService leaveBalanceService;

    private Employee createEmployee() {
        var employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        var department = new Department();
        department.setId(10L);
        employee.setDepartment(department);
        return employee;
    }

    private LeaveType createLeaveType() {
        var leaveType = new LeaveType();
        leaveType.setId(1L);
        leaveType.setName("Casual Leave");
        leaveType.setCode("CL");
        return leaveType;
    }

    private LeaveBalance createLeaveBalance(Long id, Employee employee, LeaveType leaveType, int year) {
        var balance = new LeaveBalance();
        balance.setId(id);
        balance.setEmployee(employee);
        balance.setLeaveType(leaveType);
        balance.setYear(year);
        balance.setCredited(new BigDecimal("12.00"));
        balance.setUsed(new BigDecimal("3.00"));
        balance.setPending(new BigDecimal("2.00"));
        balance.setAvailable(new BigDecimal("7.00"));
        balance.setCarryForwarded(BigDecimal.ZERO);
        balance.setCreatedAt(LocalDateTime.now());
        balance.setUpdatedAt(LocalDateTime.now());
        return balance;
    }

    @Test
    void getBalancesByEmployee_shouldReturnBalances() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var balance = createLeaveBalance(1L, employee, leaveType, 2025);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployeeIdAndYear(1L, 2025)).thenReturn(List.of(balance));

        var result = leaveBalanceService.getBalancesByEmployee(1L, 2025);

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).employeeName());
        assertEquals("Casual Leave", result.get(0).leaveTypeName());
        assertEquals(new BigDecimal("12.00"), result.get(0).credited());
        assertEquals(new BigDecimal("7.00"), result.get(0).available());
    }

    @Test
    void getBalancesByEmployee_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveBalanceService.getBalancesByEmployee(999L, 2025));
    }

    @Test
    void getBalance_shouldReturnBalance() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var balance = createLeaveBalance(1L, employee, leaveType, 2025);
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, 2025))
                .thenReturn(Optional.of(balance));

        var result = leaveBalanceService.getBalance(1L, 1L, 2025);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(2025, result.year());
        assertEquals("EMP001", result.employeeCode());
    }

    @Test
    void getBalance_shouldThrowWhenNotFound() {
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, 2025))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveBalanceService.getBalance(1L, 1L, 2025));
    }

    @Test
    void getTransactions_shouldReturnAllForEmployee() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var txn = new LeaveTransaction();
        txn.setId(1L);
        txn.setEmployee(employee);
        txn.setLeaveType(leaveType);
        txn.setTransactionType(TransactionType.CREDIT);
        txn.setAmount(new BigDecimal("12.00"));
        txn.setBalanceAfter(new BigDecimal("12.00"));
        txn.setCreatedAt(LocalDateTime.now());
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(List.of(txn), pageable, 1);
        when(leaveTransactionRepository.findByEmployeeId(1L, pageable)).thenReturn(page);

        var result = leaveBalanceService.getTransactions(1L, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("CREDIT", result.getContent().get(0).transactionType());
    }

    @Test
    void getTransactions_shouldFilterByLeaveType() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var txn = new LeaveTransaction();
        txn.setId(1L);
        txn.setEmployee(employee);
        txn.setLeaveType(leaveType);
        txn.setTransactionType(TransactionType.DEBIT);
        txn.setAmount(new BigDecimal("-3.00"));
        txn.setBalanceAfter(new BigDecimal("9.00"));
        txn.setCreatedAt(LocalDateTime.now());
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(List.of(txn), pageable, 1);
        when(leaveTransactionRepository.findByEmployeeIdAndLeaveTypeId(1L, 1L, pageable)).thenReturn(page);

        var result = leaveBalanceService.getTransactions(1L, 1L, null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTransactions_shouldFilterByTransactionType() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var txn = new LeaveTransaction();
        txn.setId(1L);
        txn.setEmployee(employee);
        txn.setLeaveType(leaveType);
        txn.setTransactionType(TransactionType.ADJUSTMENT);
        txn.setAmount(new BigDecimal("2.00"));
        txn.setBalanceAfter(new BigDecimal("14.00"));
        txn.setCreatedAt(LocalDateTime.now());
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(List.of(txn), pageable, 1);
        when(leaveTransactionRepository.findByEmployeeIdAndTransactionType(1L, TransactionType.ADJUSTMENT, pageable))
                .thenReturn(page);

        var result = leaveBalanceService.getTransactions(1L, null, TransactionType.ADJUSTMENT, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTransactions_shouldFilterByBothLeaveTypeAndTransactionType() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var txn = new LeaveTransaction();
        txn.setId(1L);
        txn.setEmployee(employee);
        txn.setLeaveType(leaveType);
        txn.setTransactionType(TransactionType.DEBIT);
        txn.setAmount(new BigDecimal("-3.00"));
        txn.setBalanceAfter(new BigDecimal("9.00"));
        txn.setCreatedAt(LocalDateTime.now());
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(List.of(txn), pageable, 1);
        when(leaveTransactionRepository.findByEmployeeIdAndLeaveTypeIdAndTransactionType(1L, 1L, TransactionType.DEBIT, pageable))
                .thenReturn(page);

        var result = leaveBalanceService.getTransactions(1L, 1L, TransactionType.DEBIT, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void adjustBalance_shouldAdjustAndRecordTransaction() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var balance = createLeaveBalance(1L, employee, leaveType, 2025);
        var request = new BalanceAdjustmentRequest(1L, 1L, 2025, new BigDecimal("5.00"), "Bonus leave", "HR Admin");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, 2025))
                .thenReturn(Optional.of(balance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveTransactionRepository.save(any(LeaveTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = leaveBalanceService.adjustBalance(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("17.00"), result.credited());
        verify(leaveBalanceRepository).save(any(LeaveBalance.class));
        verify(leaveTransactionRepository).save(any(LeaveTransaction.class));
    }

    @Test
    void adjustBalance_shouldThrowWhenEmployeeNotFound() {
        var request = new BalanceAdjustmentRequest(999L, 1L, 2025, new BigDecimal("5.00"), null, null);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveBalanceService.adjustBalance(request));
    }

    @Test
    void adjustBalance_shouldThrowWhenLeaveTypeNotFound() {
        var employee = createEmployee();
        var request = new BalanceAdjustmentRequest(1L, 999L, 2025, new BigDecimal("5.00"), null, null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveBalanceService.adjustBalance(request));
    }

    @Test
    void adjustBalance_shouldCreateBalanceIfNotExists() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var request = new BalanceAdjustmentRequest(1L, 1L, 2025, new BigDecimal("10.00"), "Initial credit", "Admin");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, 2025))
                .thenReturn(Optional.empty());
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> {
            LeaveBalance b = inv.getArgument(0);
            b.setId(1L);
            b.setCreatedAt(LocalDateTime.now());
            b.setUpdatedAt(LocalDateTime.now());
            return b;
        });
        when(leaveTransactionRepository.save(any(LeaveTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = leaveBalanceService.adjustBalance(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result.credited());
    }

    @Test
    void processYearEnd_shouldThrowWhenNoBalancesFound() {
        var request = new YearEndProcessingRequest(2025, "Admin");
        when(leaveBalanceRepository.findByYear(2025)).thenReturn(List.of());

        assertThrows(BadRequestException.class,
                () -> leaveBalanceService.processYearEnd(request));
    }

    @Test
    void processYearEnd_shouldCarryForwardAndLapse() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var balance = new LeaveBalance();
        balance.setId(1L);
        balance.setEmployee(employee);
        balance.setLeaveType(leaveType);
        balance.setYear(2025);
        balance.setCredited(new BigDecimal("12.00"));
        balance.setUsed(new BigDecimal("5.00"));
        balance.setPending(BigDecimal.ZERO);
        balance.setAvailable(new BigDecimal("7.00"));
        balance.setCarryForwarded(BigDecimal.ZERO);
        balance.setCreatedAt(LocalDateTime.now());
        balance.setUpdatedAt(LocalDateTime.now());

        var policy = new LeavePolicy();
        policy.setId(1L);
        policy.setActive(true);
        policy.setCarryForwardLimit(new BigDecimal("5.00"));

        var assignment = new LeavePolicyAssignment();
        assignment.setAssignmentType(AssignmentType.DEPARTMENT);
        assignment.setDepartmentId(10L);
        assignment.setActive(true);

        var request = new YearEndProcessingRequest(2025, "Admin");

        when(leaveBalanceRepository.findByYear(2025)).thenReturn(List.of(balance));
        when(leavePolicyRepository.findByLeaveTypeId(1L)).thenReturn(List.of(policy));
        when(leavePolicyAssignmentRepository.findByLeavePolicyId(1L)).thenReturn(List.of(assignment));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, 2026))
                .thenReturn(Optional.empty());
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> {
            LeaveBalance b = inv.getArgument(0);
            if (b.getId() == null) {
                b.setId(2L);
                b.setCreatedAt(LocalDateTime.now());
                b.setUpdatedAt(LocalDateTime.now());
            }
            return b;
        });
        when(leaveTransactionRepository.save(any(LeaveTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = leaveBalanceService.processYearEnd(request);

        assertNotNull(result);
        assertEquals(2025, result.processedYear());
        assertEquals(2026, result.nextYear());
        assertEquals(1, result.employeesProcessed());
        assertEquals(1, result.balancesCreated());
        assertEquals(new BigDecimal("5.00"), result.totalCarryForwarded());
        assertEquals(new BigDecimal("2.00"), result.totalLapsed());
    }

    @Test
    void recordLeaveSubmission_shouldUpdatePendingBalance() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var balance = createLeaveBalance(1L, employee, leaveType, java.time.LocalDate.now().getYear());

        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(eq(1L), eq(1L), any(Integer.class)))
                .thenReturn(Optional.of(balance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveTransactionRepository.save(any(LeaveTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        leaveBalanceService.recordLeaveSubmission(employee, leaveType, new BigDecimal("3.00"), 100L);

        verify(leaveBalanceRepository).save(any(LeaveBalance.class));
        verify(leaveTransactionRepository).save(any(LeaveTransaction.class));
    }

    @Test
    void recordLeaveApproval_shouldUpdateUsedBalance() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var balance = createLeaveBalance(1L, employee, leaveType, java.time.LocalDate.now().getYear());

        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(eq(1L), eq(1L), any(Integer.class)))
                .thenReturn(Optional.of(balance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveTransactionRepository.save(any(LeaveTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        leaveBalanceService.recordLeaveApproval(employee, leaveType, new BigDecimal("2.00"), 100L);

        verify(leaveBalanceRepository).save(any(LeaveBalance.class));
        verify(leaveTransactionRepository).save(any(LeaveTransaction.class));
    }

    @Test
    void recordLeaveCancellation_shouldRestoreBalanceForApproved() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var balance = createLeaveBalance(1L, employee, leaveType, java.time.LocalDate.now().getYear());

        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(eq(1L), eq(1L), any(Integer.class)))
                .thenReturn(Optional.of(balance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveTransactionRepository.save(any(LeaveTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        leaveBalanceService.recordLeaveCancellation(employee, leaveType, new BigDecimal("3.00"), 100L, true);

        verify(leaveBalanceRepository).save(any(LeaveBalance.class));
        verify(leaveTransactionRepository).save(any(LeaveTransaction.class));
    }

    @Test
    void recordLeaveCancellation_shouldRestoreBalanceForPending() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var balance = createLeaveBalance(1L, employee, leaveType, java.time.LocalDate.now().getYear());

        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(eq(1L), eq(1L), any(Integer.class)))
                .thenReturn(Optional.of(balance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveTransactionRepository.save(any(LeaveTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        leaveBalanceService.recordLeaveCancellation(employee, leaveType, new BigDecimal("2.00"), 100L, false);

        verify(leaveBalanceRepository).save(any(LeaveBalance.class));
        verify(leaveTransactionRepository).save(any(LeaveTransaction.class));
    }
}
