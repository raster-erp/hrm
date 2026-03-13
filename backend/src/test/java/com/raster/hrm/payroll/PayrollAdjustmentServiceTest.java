package com.raster.hrm.payroll;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.payroll.dto.PayrollAdjustmentRequest;
import com.raster.hrm.payroll.entity.AdjustmentType;
import com.raster.hrm.payroll.entity.PayrollAdjustment;
import com.raster.hrm.payroll.entity.PayrollRun;
import com.raster.hrm.payroll.entity.PayrollRunStatus;
import com.raster.hrm.payroll.repository.PayrollAdjustmentRepository;
import com.raster.hrm.payroll.repository.PayrollRunRepository;
import com.raster.hrm.payroll.service.PayrollAdjustmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollAdjustmentServiceTest {

    @Mock
    private PayrollAdjustmentRepository payrollAdjustmentRepository;

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private PayrollAdjustmentService payrollAdjustmentService;

    private PayrollRun createPayrollRun(Long id, PayrollRunStatus status) {
        var run = new PayrollRun();
        run.setId(id);
        run.setPeriodYear(2024);
        run.setPeriodMonth(6);
        run.setRunDate(LocalDate.of(2024, 6, 30));
        run.setStatus(status);
        run.setCreatedAt(LocalDateTime.of(2024, 6, 30, 10, 0));
        run.setUpdatedAt(LocalDateTime.of(2024, 6, 30, 10, 0));
        return run;
    }

    private Employee createEmployee(Long id) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        return employee;
    }

    private PayrollAdjustment createAdjustment(Long id, PayrollRun run, Employee employee) {
        var adj = new PayrollAdjustment();
        adj.setId(id);
        adj.setPayrollRun(run);
        adj.setEmployee(employee);
        adj.setAdjustmentType(AdjustmentType.ADDITION);
        adj.setComponentName("Bonus");
        adj.setAmount(new BigDecimal("5000.00"));
        adj.setReason("Performance bonus");
        adj.setCreatedAt(LocalDateTime.of(2024, 6, 30, 10, 0));
        return adj;
    }

    // --- getByRunId tests ---

    @Test
    void getByRunId_shouldReturnAdjustments() {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        var employee = createEmployee(1L);
        var adj = createAdjustment(1L, run, employee);

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollAdjustmentRepository.findByPayrollRunId(1L)).thenReturn(List.of(adj));

        var result = payrollAdjustmentService.getByRunId(1L);

        assertEquals(1, result.size());
        assertEquals("Bonus", result.get(0).componentName());
        assertEquals("ADDITION", result.get(0).adjustmentType());
    }

    @Test
    void getByRunId_shouldReturnEmptyListWhenNoAdjustments() {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollAdjustmentRepository.findByPayrollRunId(1L)).thenReturn(List.of());

        var result = payrollAdjustmentService.getByRunId(1L);

        assertEquals(0, result.size());
    }

    @Test
    void getByRunId_shouldThrowWhenRunNotFound() {
        when(payrollRunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> payrollAdjustmentService.getByRunId(99L));
    }

    // --- create tests ---

    @Test
    void create_shouldCreateAdjustmentInDraftStatus() {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        var employee = createEmployee(1L);
        var request = new PayrollAdjustmentRequest(1L, 1L, "ADDITION", "Bonus",
                new BigDecimal("5000.00"), "Performance bonus");

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollAdjustmentRepository.save(any(PayrollAdjustment.class))).thenAnswer(invocation -> {
            var adj = invocation.getArgument(0, PayrollAdjustment.class);
            adj.setId(1L);
            adj.setCreatedAt(LocalDateTime.now());
            return adj;
        });

        var result = payrollAdjustmentService.create(request);

        assertNotNull(result);
        assertEquals("Bonus", result.componentName());
        assertEquals("ADDITION", result.adjustmentType());
        assertEquals(new BigDecimal("5000.00"), result.amount());
    }

    @Test
    void create_shouldCreateAdjustmentInComputedStatus() {
        var run = createPayrollRun(1L, PayrollRunStatus.COMPUTED);
        var employee = createEmployee(1L);
        var request = new PayrollAdjustmentRequest(1L, 1L, "DEDUCTION", "Advance",
                new BigDecimal("2000.00"), "Advance recovery");

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollAdjustmentRepository.save(any(PayrollAdjustment.class))).thenAnswer(invocation -> {
            var adj = invocation.getArgument(0, PayrollAdjustment.class);
            adj.setId(1L);
            adj.setCreatedAt(LocalDateTime.now());
            return adj;
        });

        var result = payrollAdjustmentService.create(request);

        assertNotNull(result);
        assertEquals("DEDUCTION", result.adjustmentType());
    }

    @Test
    void create_shouldRejectWhenRunIsFinalized() {
        var run = createPayrollRun(1L, PayrollRunStatus.FINALIZED);
        var request = new PayrollAdjustmentRequest(1L, 1L, "ADDITION", "Bonus",
                new BigDecimal("5000.00"), null);

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));

        assertThrows(BadRequestException.class, () -> payrollAdjustmentService.create(request));
    }

    @Test
    void create_shouldRejectWhenRunIsVerified() {
        var run = createPayrollRun(1L, PayrollRunStatus.VERIFIED);
        var request = new PayrollAdjustmentRequest(1L, 1L, "ADDITION", "Bonus",
                new BigDecimal("5000.00"), null);

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));

        assertThrows(BadRequestException.class, () -> payrollAdjustmentService.create(request));
    }

    @Test
    void create_shouldThrowWhenRunNotFound() {
        var request = new PayrollAdjustmentRequest(99L, 1L, "ADDITION", "Bonus",
                new BigDecimal("5000.00"), null);

        when(payrollRunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> payrollAdjustmentService.create(request));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        var request = new PayrollAdjustmentRequest(1L, 99L, "ADDITION", "Bonus",
                new BigDecimal("5000.00"), null);

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> payrollAdjustmentService.create(request));
    }

    // --- delete tests ---

    @Test
    void delete_shouldDeleteInDraftStatus() {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        var employee = createEmployee(1L);
        var adj = createAdjustment(1L, run, employee);

        when(payrollAdjustmentRepository.findById(1L)).thenReturn(Optional.of(adj));

        payrollAdjustmentService.delete(1L);

        verify(payrollAdjustmentRepository).delete(adj);
    }

    @Test
    void delete_shouldDeleteInComputedStatus() {
        var run = createPayrollRun(1L, PayrollRunStatus.COMPUTED);
        var employee = createEmployee(1L);
        var adj = createAdjustment(1L, run, employee);

        when(payrollAdjustmentRepository.findById(1L)).thenReturn(Optional.of(adj));

        payrollAdjustmentService.delete(1L);

        verify(payrollAdjustmentRepository).delete(adj);
    }

    @Test
    void delete_shouldRejectWhenRunIsFinalized() {
        var run = createPayrollRun(1L, PayrollRunStatus.FINALIZED);
        var employee = createEmployee(1L);
        var adj = createAdjustment(1L, run, employee);

        when(payrollAdjustmentRepository.findById(1L)).thenReturn(Optional.of(adj));

        assertThrows(BadRequestException.class, () -> payrollAdjustmentService.delete(1L));
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(payrollAdjustmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> payrollAdjustmentService.delete(99L));
    }
}
