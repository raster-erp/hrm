package com.raster.hrm.leaveencashment;

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
import com.raster.hrm.leaveencashment.service.LeaveEncashmentService;
import com.raster.hrm.leavetype.entity.LeaveType;
import com.raster.hrm.leavetype.repository.LeaveTypeRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveEncashmentServiceTest {

    @Mock
    private LeaveEncashmentRepository leaveEncashmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private LeaveBalanceService leaveBalanceService;

    @InjectMocks
    private LeaveEncashmentService leaveEncashmentService;

    private Employee createEmployee() {
        var employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setBasicSalary(new BigDecimal("30000.00"));
        return employee;
    }

    private LeaveType createLeaveType() {
        var leaveType = new LeaveType();
        leaveType.setId(1L);
        leaveType.setCode("CL");
        leaveType.setName("Casual Leave");
        leaveType.setEncashable(true);
        leaveType.setMinEncashmentBalance(new BigDecimal("5.00"));
        return leaveType;
    }

    private LeaveBalance createLeaveBalance() {
        var balance = new LeaveBalance();
        balance.setId(1L);
        balance.setEmployee(createEmployee());
        balance.setLeaveType(createLeaveType());
        balance.setYear(java.time.LocalDate.now().getYear());
        balance.setCredited(new BigDecimal("20.00"));
        balance.setUsed(new BigDecimal("3.00"));
        balance.setPending(new BigDecimal("2.00"));
        balance.setAvailable(new BigDecimal("15.00"));
        balance.setCarryForwarded(BigDecimal.ZERO);
        balance.setEncashed(BigDecimal.ZERO);
        return balance;
    }

    private LeaveEncashment createEncashment(EncashmentStatus status) {
        var encashment = new LeaveEncashment();
        encashment.setId(1L);
        encashment.setEmployee(createEmployee());
        encashment.setLeaveType(createLeaveType());
        encashment.setYear(java.time.LocalDate.now().getYear());
        encashment.setNumberOfDays(new BigDecimal("5.00"));
        encashment.setPerDaySalary(new BigDecimal("1000.00"));
        encashment.setTotalAmount(new BigDecimal("5000.00"));
        encashment.setStatus(status);
        encashment.setCreatedAt(LocalDateTime.now());
        encashment.setUpdatedAt(LocalDateTime.now());
        return encashment;
    }

    // ── Eligibility Check Tests ──────────────────────────────────────────

    @Test
    void checkEligibility_shouldReturnEligible() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var balance = createLeaveBalance();
        int year = java.time.LocalDate.now().getYear();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, year))
                .thenReturn(Optional.of(balance));

        EncashmentEligibilityResponse response = leaveEncashmentService.checkEligibility(1L, 1L, year);

        assertTrue(response.eligible());
        assertEquals(new BigDecimal("15.00"), response.availableBalance());
        assertEquals(new BigDecimal("10.00"), response.maxEncashableDays());
        assertEquals(new BigDecimal("1000.00"), response.perDaySalary());
    }

    @Test
    void checkEligibility_shouldReturnNotEligible_whenNotEncashable() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        leaveType.setEncashable(false);
        int year = java.time.LocalDate.now().getYear();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        EncashmentEligibilityResponse response = leaveEncashmentService.checkEligibility(1L, 1L, year);

        assertFalse(response.eligible());
        assertTrue(response.reason().contains("not eligible"));
    }

    @Test
    void checkEligibility_shouldReturnNotEligible_whenNoSalary() {
        var employee = createEmployee();
        employee.setBasicSalary(BigDecimal.ZERO);
        var leaveType = createLeaveType();
        int year = java.time.LocalDate.now().getYear();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        EncashmentEligibilityResponse response = leaveEncashmentService.checkEligibility(1L, 1L, year);

        assertFalse(response.eligible());
        assertTrue(response.reason().contains("basic salary"));
    }

    @Test
    void checkEligibility_shouldReturnNotEligible_whenInsufficientBalance() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        leaveType.setMinEncashmentBalance(new BigDecimal("20.00"));
        var balance = createLeaveBalance();
        balance.setAvailable(new BigDecimal("10.00"));
        int year = java.time.LocalDate.now().getYear();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, year))
                .thenReturn(Optional.of(balance));

        EncashmentEligibilityResponse response = leaveEncashmentService.checkEligibility(1L, 1L, year);

        assertFalse(response.eligible());
        assertTrue(response.reason().contains("Insufficient"));
    }

    @Test
    void checkEligibility_shouldThrow_whenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveEncashmentService.checkEligibility(999L, 1L, 2025));
    }

    @Test
    void checkEligibility_shouldThrow_whenLeaveTypeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(createEmployee()));
        when(leaveTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveEncashmentService.checkEligibility(1L, 999L, 2025));
    }

    // ── Create Request Tests ─────────────────────────────────────────────

    @Test
    void createRequest_shouldCreateEncashment() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var balance = createLeaveBalance();
        int year = java.time.LocalDate.now().getYear();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveEncashmentRepository.findByEmployeeIdAndLeaveTypeIdAndYearAndStatusIn(
                eq(1L), eq(1L), eq(year), any())).thenReturn(Optional.empty());
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, year))
                .thenReturn(Optional.of(balance));

        var saved = createEncashment(EncashmentStatus.PENDING);
        when(leaveEncashmentRepository.save(any(LeaveEncashment.class))).thenReturn(saved);

        var request = new LeaveEncashmentRequest(1L, 1L, java.time.LocalDate.now().getYear(), new BigDecimal("5.00"), "Test");
        LeaveEncashmentResponse response = leaveEncashmentService.createRequest(request);

        assertNotNull(response);
        assertEquals("PENDING", response.status());
        verify(leaveEncashmentRepository).save(any(LeaveEncashment.class));
    }

    @Test
    void createRequest_shouldThrow_whenNotEncashable() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        leaveType.setEncashable(false);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        var request = new LeaveEncashmentRequest(1L, 1L, java.time.LocalDate.now().getYear(), new BigDecimal("5.00"), null);
        assertThrows(BadRequestException.class, () -> leaveEncashmentService.createRequest(request));
    }

    @Test
    void createRequest_shouldThrow_whenNoSalary() {
        var employee = createEmployee();
        employee.setBasicSalary(BigDecimal.ZERO);
        var leaveType = createLeaveType();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        var request = new LeaveEncashmentRequest(1L, 1L, java.time.LocalDate.now().getYear(), new BigDecimal("5.00"), null);
        assertThrows(BadRequestException.class, () -> leaveEncashmentService.createRequest(request));
    }

    @Test
    void createRequest_shouldThrow_whenExistingPendingEncashment() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        int year = java.time.LocalDate.now().getYear();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveEncashmentRepository.findByEmployeeIdAndLeaveTypeIdAndYearAndStatusIn(
                eq(1L), eq(1L), eq(year), any()))
                .thenReturn(Optional.of(createEncashment(EncashmentStatus.PENDING)));

        var request = new LeaveEncashmentRequest(1L, 1L, java.time.LocalDate.now().getYear(), new BigDecimal("5.00"), null);
        assertThrows(BadRequestException.class, () -> leaveEncashmentService.createRequest(request));
    }

    @Test
    void createRequest_shouldThrow_whenExceedsMaxEncashableDays() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var balance = createLeaveBalance();
        int year = java.time.LocalDate.now().getYear();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveEncashmentRepository.findByEmployeeIdAndLeaveTypeIdAndYearAndStatusIn(
                eq(1L), eq(1L), eq(year), any())).thenReturn(Optional.empty());
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, year))
                .thenReturn(Optional.of(balance));

        var request = new LeaveEncashmentRequest(1L, 1L, java.time.LocalDate.now().getYear(), new BigDecimal("20.00"), null);
        assertThrows(BadRequestException.class, () -> leaveEncashmentService.createRequest(request));
    }

    @Test
    void createRequest_shouldThrow_whenInsufficientBalance() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var balance = createLeaveBalance();
        balance.setAvailable(new BigDecimal("3.00"));
        int year = java.time.LocalDate.now().getYear();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveEncashmentRepository.findByEmployeeIdAndLeaveTypeIdAndYearAndStatusIn(
                eq(1L), eq(1L), eq(year), any())).thenReturn(Optional.empty());
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, year))
                .thenReturn(Optional.of(balance));

        var request = new LeaveEncashmentRequest(1L, 1L, java.time.LocalDate.now().getYear(), new BigDecimal("5.00"), null);
        assertThrows(BadRequestException.class, () -> leaveEncashmentService.createRequest(request));
    }

    // ── Approve / Reject Tests ───────────────────────────────────────────

    @Test
    void approve_shouldApproveAndUpdateBalance() {
        var encashment = createEncashment(EncashmentStatus.PENDING);
        when(leaveEncashmentRepository.findById(1L)).thenReturn(Optional.of(encashment));
        when(leaveEncashmentRepository.save(any(LeaveEncashment.class))).thenReturn(encashment);

        var request = new LeaveEncashmentApprovalRequest(EncashmentStatus.APPROVED, "Admin", "Approved");
        LeaveEncashmentResponse response = leaveEncashmentService.approve(1L, request);

        assertNotNull(response);
        verify(leaveBalanceService).recordEncashment(
                encashment.getEmployee(), encashment.getLeaveType(),
                encashment.getNumberOfDays(), encashment.getId());
    }

    @Test
    void approve_shouldRejectWithoutUpdatingBalance() {
        var encashment = createEncashment(EncashmentStatus.PENDING);
        when(leaveEncashmentRepository.findById(1L)).thenReturn(Optional.of(encashment));
        when(leaveEncashmentRepository.save(any(LeaveEncashment.class))).thenReturn(encashment);

        var request = new LeaveEncashmentApprovalRequest(EncashmentStatus.REJECTED, "Admin", "Not eligible");
        LeaveEncashmentResponse response = leaveEncashmentService.approve(1L, request);

        assertNotNull(response);
        verify(leaveBalanceService, never()).recordEncashment(any(), any(), any(), any());
    }

    @Test
    void approve_shouldThrow_whenNotPending() {
        var encashment = createEncashment(EncashmentStatus.APPROVED);
        when(leaveEncashmentRepository.findById(1L)).thenReturn(Optional.of(encashment));

        var request = new LeaveEncashmentApprovalRequest(EncashmentStatus.APPROVED, "Admin", null);
        assertThrows(BadRequestException.class, () -> leaveEncashmentService.approve(1L, request));
    }

    @Test
    void approve_shouldThrow_whenInvalidStatus() {
        var encashment = createEncashment(EncashmentStatus.PENDING);
        when(leaveEncashmentRepository.findById(1L)).thenReturn(Optional.of(encashment));

        var request = new LeaveEncashmentApprovalRequest(EncashmentStatus.PAID, "Admin", null);
        assertThrows(BadRequestException.class, () -> leaveEncashmentService.approve(1L, request));
    }

    @Test
    void approve_shouldThrow_whenNotFound() {
        when(leaveEncashmentRepository.findById(999L)).thenReturn(Optional.empty());

        var request = new LeaveEncashmentApprovalRequest(EncashmentStatus.APPROVED, "Admin", null);
        assertThrows(ResourceNotFoundException.class, () -> leaveEncashmentService.approve(999L, request));
    }

    // ── Mark as Paid Tests ───────────────────────────────────────────────

    @Test
    void markAsPaid_shouldMarkApprovedAsPaid() {
        var encashment = createEncashment(EncashmentStatus.APPROVED);
        when(leaveEncashmentRepository.findById(1L)).thenReturn(Optional.of(encashment));
        when(leaveEncashmentRepository.save(any(LeaveEncashment.class))).thenReturn(encashment);

        LeaveEncashmentResponse response = leaveEncashmentService.markAsPaid(1L, "Finance");

        assertNotNull(response);
        verify(leaveEncashmentRepository).save(any(LeaveEncashment.class));
    }

    @Test
    void markAsPaid_shouldThrow_whenNotApproved() {
        var encashment = createEncashment(EncashmentStatus.PENDING);
        when(leaveEncashmentRepository.findById(1L)).thenReturn(Optional.of(encashment));

        assertThrows(BadRequestException.class, () -> leaveEncashmentService.markAsPaid(1L, "Finance"));
    }

    @Test
    void markAsPaid_shouldThrow_whenNotFound() {
        when(leaveEncashmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leaveEncashmentService.markAsPaid(999L, "Finance"));
    }

    // ── Query Tests ──────────────────────────────────────────────────────

    @Test
    void getById_shouldReturnEncashment() {
        var encashment = createEncashment(EncashmentStatus.PENDING);
        when(leaveEncashmentRepository.findById(1L)).thenReturn(Optional.of(encashment));

        LeaveEncashmentResponse response = leaveEncashmentService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(leaveEncashmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leaveEncashmentService.getById(999L));
    }

    @Test
    void getByEmployee_shouldReturnPage() {
        var pageable = PageRequest.of(0, 10);
        var encashment = createEncashment(EncashmentStatus.PENDING);
        var page = new PageImpl<>(List.of(encashment));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(createEmployee()));
        when(leaveEncashmentRepository.findByEmployeeId(1L, pageable)).thenReturn(page);

        var result = leaveEncashmentService.getByEmployee(1L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getByEmployee_shouldThrow_whenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveEncashmentService.getByEmployee(999L, PageRequest.of(0, 10)));
    }

    @Test
    void getByStatus_shouldReturnPage() {
        var pageable = PageRequest.of(0, 10);
        var encashment = createEncashment(EncashmentStatus.PENDING);
        var page = new PageImpl<>(List.of(encashment));

        when(leaveEncashmentRepository.findByStatus(EncashmentStatus.PENDING, pageable)).thenReturn(page);

        var result = leaveEncashmentService.getByStatus(EncashmentStatus.PENDING, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAll_shouldReturnPage() {
        var pageable = PageRequest.of(0, 10);
        var encashment = createEncashment(EncashmentStatus.PENDING);
        var page = new PageImpl<>(List.of(encashment));

        when(leaveEncashmentRepository.findAll(pageable)).thenReturn(page);

        var result = leaveEncashmentService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void checkEligibility_shouldReturnNotEligible_whenNullSalary() {
        var employee = createEmployee();
        employee.setBasicSalary(null);
        var leaveType = createLeaveType();
        int year = java.time.LocalDate.now().getYear();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        EncashmentEligibilityResponse response = leaveEncashmentService.checkEligibility(1L, 1L, year);

        assertFalse(response.eligible());
        assertTrue(response.reason().contains("basic salary"));
    }

    @Test
    void checkEligibility_shouldReturnEligibleWithZeroMaxDays_whenNoBalanceRecord() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        leaveType.setMinEncashmentBalance(BigDecimal.ZERO);
        int year = java.time.LocalDate.now().getYear();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, year))
                .thenReturn(Optional.empty());

        EncashmentEligibilityResponse response = leaveEncashmentService.checkEligibility(1L, 1L, year);

        assertTrue(response.eligible());
        assertEquals(BigDecimal.ZERO, response.availableBalance());
        assertEquals(BigDecimal.ZERO, response.maxEncashableDays());
    }

    @Test
    void createRequest_shouldThrow_whenNullSalary() {
        var employee = createEmployee();
        employee.setBasicSalary(null);
        var leaveType = createLeaveType();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        var request = new LeaveEncashmentRequest(1L, 1L, java.time.LocalDate.now().getYear(), new BigDecimal("5.00"), null);
        assertThrows(BadRequestException.class, () -> leaveEncashmentService.createRequest(request));
    }

    @Test
    void approve_shouldSetRemarksOnApproval() {
        var encashment = createEncashment(EncashmentStatus.PENDING);
        when(leaveEncashmentRepository.findById(1L)).thenReturn(Optional.of(encashment));
        when(leaveEncashmentRepository.save(any(LeaveEncashment.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new LeaveEncashmentApprovalRequest(EncashmentStatus.APPROVED, "Admin", "Good to go");
        LeaveEncashmentResponse response = leaveEncashmentService.approve(1L, request);

        assertNotNull(response);
        assertEquals("Good to go", response.remarks());
    }

    @Test
    void approve_shouldKeepOriginalRemarks_whenNullRemarks() {
        var encashment = createEncashment(EncashmentStatus.PENDING);
        encashment.setRemarks("Original remark");
        when(leaveEncashmentRepository.findById(1L)).thenReturn(Optional.of(encashment));
        when(leaveEncashmentRepository.save(any(LeaveEncashment.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new LeaveEncashmentApprovalRequest(EncashmentStatus.REJECTED, "Admin", null);
        LeaveEncashmentResponse response = leaveEncashmentService.approve(1L, request);

        assertNotNull(response);
        assertEquals("Original remark", response.remarks());
    }

    @Test
    void createRequest_shouldThrow_whenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        var request = new LeaveEncashmentRequest(999L, 1L, java.time.LocalDate.now().getYear(), new BigDecimal("5.00"), null);
        assertThrows(ResourceNotFoundException.class, () -> leaveEncashmentService.createRequest(request));
    }

    @Test
    void createRequest_shouldThrow_whenLeaveTypeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(createEmployee()));
        when(leaveTypeRepository.findById(999L)).thenReturn(Optional.empty());

        var request = new LeaveEncashmentRequest(1L, 999L, java.time.LocalDate.now().getYear(), new BigDecimal("5.00"), null);
        assertThrows(ResourceNotFoundException.class, () -> leaveEncashmentService.createRequest(request));
    }
}
