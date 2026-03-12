package com.raster.hrm.compoff;

import com.raster.hrm.compoff.dto.CompOffApprovalRequest;
import com.raster.hrm.compoff.dto.CompOffBalanceResponse;
import com.raster.hrm.compoff.dto.CompOffCreditRequest;
import com.raster.hrm.compoff.dto.CompOffCreditResponse;
import com.raster.hrm.compoff.entity.CompOffCredit;
import com.raster.hrm.compoff.entity.CompOffStatus;
import com.raster.hrm.compoff.repository.CompOffCreditRepository;
import com.raster.hrm.compoff.service.CompOffCreditService;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class CompOffCreditServiceTest {

    @Mock
    private CompOffCreditRepository compOffCreditRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CompOffCreditService compOffCreditService;

    private Employee createEmployee() {
        var employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        return employee;
    }

    private CompOffCredit createCredit(CompOffStatus status) {
        var credit = new CompOffCredit();
        credit.setId(1L);
        credit.setEmployee(createEmployee());
        credit.setWorkedDate(LocalDate.of(2025, 6, 15));
        credit.setReason("Worked on weekend for release");
        credit.setCreditDate(LocalDate.of(2025, 6, 16));
        credit.setExpiryDate(LocalDate.of(2025, 9, 13));
        credit.setHoursWorked(new BigDecimal("8.00"));
        credit.setStatus(status);
        credit.setCreatedAt(LocalDateTime.now());
        credit.setUpdatedAt(LocalDateTime.now());
        return credit;
    }

    // ── Create Request Tests ────────────────────────────────────────────

    @Test
    void createRequest_shouldCreateCompOff() {
        var employee = createEmployee();
        var request = new CompOffCreditRequest(
                1L, LocalDate.of(2025, 6, 15), "Worked on weekend", new BigDecimal("8.00"), "Test");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(compOffCreditRepository.findByEmployeeIdAndWorkedDateAndStatusIn(
                eq(1L), eq(LocalDate.of(2025, 6, 15)), any())).thenReturn(Optional.empty());
        when(compOffCreditRepository.save(any(CompOffCredit.class))).thenAnswer(invocation -> {
            CompOffCredit saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CompOffCreditResponse response = compOffCreditService.createRequest(request);

        assertNotNull(response);
        assertEquals(1L, response.employeeId());
        assertEquals("PENDING", response.status());
        assertEquals(LocalDate.of(2025, 6, 15), response.workedDate());
        verify(compOffCreditRepository).save(any(CompOffCredit.class));
    }

    @Test
    void createRequest_shouldThrow_whenEmployeeNotFound() {
        var request = new CompOffCreditRequest(
                999L, LocalDate.of(2025, 6, 15), "Worked on weekend", null, null);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> compOffCreditService.createRequest(request));
    }

    @Test
    void createRequest_shouldThrow_whenDuplicateExists() {
        var employee = createEmployee();
        var request = new CompOffCreditRequest(
                1L, LocalDate.of(2025, 6, 15), "Worked on weekend", null, null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(compOffCreditRepository.findByEmployeeIdAndWorkedDateAndStatusIn(
                eq(1L), eq(LocalDate.of(2025, 6, 15)), any()))
                .thenReturn(Optional.of(createCredit(CompOffStatus.PENDING)));

        assertThrows(BadRequestException.class, () -> compOffCreditService.createRequest(request));
        verify(compOffCreditRepository, never()).save(any());
    }

    @Test
    void createRequest_shouldSetExpiryDate90DaysFromWorkedDate() {
        var employee = createEmployee();
        var workedDate = LocalDate.of(2025, 6, 15);
        var request = new CompOffCreditRequest(1L, workedDate, "Worked on weekend", null, null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(compOffCreditRepository.findByEmployeeIdAndWorkedDateAndStatusIn(
                eq(1L), eq(workedDate), any())).thenReturn(Optional.empty());
        when(compOffCreditRepository.save(any(CompOffCredit.class))).thenAnswer(invocation -> {
            CompOffCredit saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CompOffCreditResponse response = compOffCreditService.createRequest(request);

        assertEquals(workedDate.plusDays(90), response.expiryDate());
    }

    @Test
    void createRequest_shouldSetCreditDateToToday() {
        var employee = createEmployee();
        var request = new CompOffCreditRequest(
                1L, LocalDate.of(2025, 6, 15), "Worked on weekend", null, null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(compOffCreditRepository.findByEmployeeIdAndWorkedDateAndStatusIn(
                eq(1L), any(), any())).thenReturn(Optional.empty());
        when(compOffCreditRepository.save(any(CompOffCredit.class))).thenAnswer(invocation -> {
            CompOffCredit saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CompOffCreditResponse response = compOffCreditService.createRequest(request);

        assertEquals(LocalDate.now(), response.creditDate());
    }

    // ── Approve / Reject Tests ──────────────────────────────────────────

    @Test
    void approve_shouldApproveCompOff() {
        var credit = createCredit(CompOffStatus.PENDING);
        var request = new CompOffApprovalRequest(CompOffStatus.APPROVED, "Admin", "Approved");

        when(compOffCreditRepository.findById(1L)).thenReturn(Optional.of(credit));
        when(compOffCreditRepository.save(any(CompOffCredit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompOffCreditResponse response = compOffCreditService.approve(1L, request);

        assertEquals("APPROVED", response.status());
        assertEquals("Admin", response.approvedBy());
        assertNotNull(response.approvedAt());
    }

    @Test
    void approve_shouldRejectCompOff() {
        var credit = createCredit(CompOffStatus.PENDING);
        var request = new CompOffApprovalRequest(CompOffStatus.REJECTED, "Admin", "Rejected - not valid");

        when(compOffCreditRepository.findById(1L)).thenReturn(Optional.of(credit));
        when(compOffCreditRepository.save(any(CompOffCredit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompOffCreditResponse response = compOffCreditService.approve(1L, request);

        assertEquals("REJECTED", response.status());
        assertEquals("Rejected - not valid", response.remarks());
    }

    @Test
    void approve_shouldThrow_whenNotPending() {
        var credit = createCredit(CompOffStatus.APPROVED);
        var request = new CompOffApprovalRequest(CompOffStatus.APPROVED, "Admin", null);

        when(compOffCreditRepository.findById(1L)).thenReturn(Optional.of(credit));

        assertThrows(BadRequestException.class, () -> compOffCreditService.approve(1L, request));
        verify(compOffCreditRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrow_whenInvalidStatus() {
        var credit = createCredit(CompOffStatus.PENDING);
        var request = new CompOffApprovalRequest(CompOffStatus.USED, "Admin", null);

        when(compOffCreditRepository.findById(1L)).thenReturn(Optional.of(credit));

        assertThrows(BadRequestException.class, () -> compOffCreditService.approve(1L, request));
        verify(compOffCreditRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrow_whenNotFound() {
        var request = new CompOffApprovalRequest(CompOffStatus.APPROVED, "Admin", null);
        when(compOffCreditRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> compOffCreditService.approve(999L, request));
    }

    @Test
    void approve_shouldSetRemarksOnApproval() {
        var credit = createCredit(CompOffStatus.PENDING);
        var request = new CompOffApprovalRequest(CompOffStatus.APPROVED, "Admin", "Good work");

        when(compOffCreditRepository.findById(1L)).thenReturn(Optional.of(credit));
        when(compOffCreditRepository.save(any(CompOffCredit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompOffCreditResponse response = compOffCreditService.approve(1L, request);

        assertEquals("Good work", response.remarks());
    }

    @Test
    void approve_shouldKeepOriginalRemarks_whenNullRemarks() {
        var credit = createCredit(CompOffStatus.PENDING);
        credit.setRemarks("Original remark");
        var request = new CompOffApprovalRequest(CompOffStatus.APPROVED, "Admin", null);

        when(compOffCreditRepository.findById(1L)).thenReturn(Optional.of(credit));
        when(compOffCreditRepository.save(any(CompOffCredit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompOffCreditResponse response = compOffCreditService.approve(1L, request);

        assertEquals("Original remark", response.remarks());
    }

    // ── Balance Tests ───────────────────────────────────────────────────

    @Test
    void getBalance_shouldReturnBalance() {
        var employee = createEmployee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(compOffCreditRepository.countByEmployeeIdAndStatus(1L, CompOffStatus.APPROVED)).thenReturn(3L);
        when(compOffCreditRepository.countByEmployeeIdAndStatus(1L, CompOffStatus.PENDING)).thenReturn(1L);
        when(compOffCreditRepository.countByEmployeeIdAndStatus(1L, CompOffStatus.USED)).thenReturn(2L);
        when(compOffCreditRepository.countByEmployeeIdAndStatus(1L, CompOffStatus.EXPIRED)).thenReturn(1L);
        when(compOffCreditRepository.countByEmployeeIdAndStatus(1L, CompOffStatus.REJECTED)).thenReturn(0L);

        CompOffBalanceResponse balance = compOffCreditService.getBalance(1L);

        assertEquals(1L, balance.employeeId());
        assertEquals("John Doe", balance.employeeName());
        assertEquals(7, balance.totalCredits());
        assertEquals(3, balance.approved());
        assertEquals(1, balance.pending());
        assertEquals(2, balance.used());
        assertEquals(1, balance.expired());
        assertEquals(3, balance.availableForUse());
    }

    @Test
    void getBalance_shouldThrow_whenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> compOffCreditService.getBalance(999L));
    }

    // ── Expire Credits Tests ────────────────────────────────────────────

    @Test
    void expireCredits_shouldExpireApprovedPastExpiry() {
        var credit1 = createCredit(CompOffStatus.APPROVED);
        credit1.setExpiryDate(LocalDate.now().minusDays(1));
        var credit2 = createCredit(CompOffStatus.APPROVED);
        credit2.setId(2L);
        credit2.setExpiryDate(LocalDate.now().minusDays(5));

        when(compOffCreditRepository.findByStatusAndExpiryDateBefore(CompOffStatus.APPROVED, LocalDate.now()))
                .thenReturn(List.of(credit1, credit2));
        when(compOffCreditRepository.saveAll(any())).thenReturn(List.of(credit1, credit2));

        int expired = compOffCreditService.expireCredits();

        assertEquals(2, expired);
        assertEquals(CompOffStatus.EXPIRED, credit1.getStatus());
        assertEquals(CompOffStatus.EXPIRED, credit2.getStatus());
        verify(compOffCreditRepository).saveAll(any());
    }

    @Test
    void expireCredits_shouldReturnZero_whenNoneExpired() {
        when(compOffCreditRepository.findByStatusAndExpiryDateBefore(CompOffStatus.APPROVED, LocalDate.now()))
                .thenReturn(List.of());

        int expired = compOffCreditService.expireCredits();

        assertEquals(0, expired);
    }

    // ── Query Tests ─────────────────────────────────────────────────────

    @Test
    void getById_shouldReturnCompOff() {
        var credit = createCredit(CompOffStatus.PENDING);
        when(compOffCreditRepository.findById(1L)).thenReturn(Optional.of(credit));

        CompOffCreditResponse response = compOffCreditService.getById(1L);

        assertEquals(1L, response.id());
        assertEquals("EMP001", response.employeeCode());
        assertEquals("John Doe", response.employeeName());
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(compOffCreditRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> compOffCreditService.getById(999L));
    }

    @Test
    void getByEmployee_shouldReturnPage() {
        var employee = createEmployee();
        var credit = createCredit(CompOffStatus.PENDING);
        var page = new PageImpl<>(List.of(credit));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(compOffCreditRepository.findByEmployeeId(eq(1L), any())).thenReturn(page);

        var result = compOffCreditService.getByEmployee(1L, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getByEmployee_shouldThrow_whenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> compOffCreditService.getByEmployee(999L, PageRequest.of(0, 10)));
    }

    @Test
    void getByStatus_shouldReturnPage() {
        var credit = createCredit(CompOffStatus.PENDING);
        var page = new PageImpl<>(List.of(credit));

        when(compOffCreditRepository.findByStatus(eq(CompOffStatus.PENDING), any())).thenReturn(page);

        var result = compOffCreditService.getByStatus(CompOffStatus.PENDING, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAll_shouldReturnPage() {
        var credit = createCredit(CompOffStatus.PENDING);
        var page = new PageImpl<>(List.of(credit));

        when(compOffCreditRepository.findAll(any(PageRequest.class))).thenReturn(page);

        var result = compOffCreditService.getAll(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }
}
