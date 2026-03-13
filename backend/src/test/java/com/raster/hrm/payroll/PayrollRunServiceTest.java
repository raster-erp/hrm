package com.raster.hrm.payroll;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employeesalary.entity.EmployeeSalaryDetail;
import com.raster.hrm.employeesalary.repository.EmployeeSalaryDetailRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.payroll.dto.PayrollRunRequest;
import com.raster.hrm.payroll.entity.AdjustmentType;
import com.raster.hrm.payroll.entity.PayrollAdjustment;
import com.raster.hrm.payroll.entity.PayrollDetail;
import com.raster.hrm.payroll.entity.PayrollRun;
import com.raster.hrm.payroll.entity.PayrollRunStatus;
import com.raster.hrm.payroll.repository.PayrollAdjustmentRepository;
import com.raster.hrm.payroll.repository.PayrollDetailRepository;
import com.raster.hrm.payroll.repository.PayrollRunRepository;
import com.raster.hrm.payroll.service.PayrollRunService;
import com.raster.hrm.salarycomponent.entity.SalaryComponent;
import com.raster.hrm.salarycomponent.entity.SalaryComponentType;
import com.raster.hrm.salarycomponent.entity.SalaryComputationType;
import com.raster.hrm.salarystructure.entity.SalaryStructure;
import com.raster.hrm.salarystructure.entity.SalaryStructureComponent;
import com.raster.hrm.salarystructure.repository.SalaryStructureComponentRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollRunServiceTest {

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private PayrollDetailRepository payrollDetailRepository;

    @Mock
    private PayrollAdjustmentRepository payrollAdjustmentRepository;

    @Mock
    private EmployeeSalaryDetailRepository employeeSalaryDetailRepository;

    @Mock
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PayrollRunService payrollRunService;

    private PayrollRun createPayrollRun(Long id, PayrollRunStatus status) {
        var run = new PayrollRun();
        run.setId(id);
        run.setPeriodYear(2024);
        run.setPeriodMonth(6);
        run.setRunDate(LocalDate.of(2024, 6, 30));
        run.setStatus(status);
        run.setTotalGross(BigDecimal.ZERO);
        run.setTotalDeductions(BigDecimal.ZERO);
        run.setTotalNet(BigDecimal.ZERO);
        run.setEmployeeCount(0);
        run.setNotes("Test run");
        run.setCreatedAt(LocalDateTime.of(2024, 6, 30, 10, 0));
        run.setUpdatedAt(LocalDateTime.of(2024, 6, 30, 10, 0));
        return run;
    }

    private Employee createEmployee(Long id) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode("EMP00" + id);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        return employee;
    }

    private SalaryStructure createSalaryStructure(Long id) {
        var structure = new SalaryStructure();
        structure.setId(id);
        structure.setCode("STD");
        structure.setName("Standard");
        structure.setComponents(new ArrayList<>());
        structure.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        structure.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        return structure;
    }

    private SalaryStructureComponent createStructureComponent(SalaryStructure structure,
                                                               String componentName,
                                                               SalaryComponentType type,
                                                               SalaryComputationType computationType,
                                                               BigDecimal percentage,
                                                               BigDecimal fixedAmount,
                                                               int sortOrder) {
        var salaryComponent = new SalaryComponent();
        salaryComponent.setId((long) sortOrder);
        salaryComponent.setCode(componentName.toUpperCase().replace(" ", "_"));
        salaryComponent.setName(componentName);
        salaryComponent.setType(type);
        salaryComponent.setComputationType(computationType);

        var structureComp = new SalaryStructureComponent();
        structureComp.setId((long) sortOrder);
        structureComp.setSalaryStructure(structure);
        structureComp.setSalaryComponent(salaryComponent);
        structureComp.setComputationType(computationType);
        structureComp.setPercentageValue(percentage);
        structureComp.setFixedAmount(fixedAmount);
        structureComp.setSortOrder(sortOrder);
        return structureComp;
    }

    private EmployeeSalaryDetail createSalaryDetail(Long id, Employee employee, SalaryStructure structure) {
        var detail = new EmployeeSalaryDetail();
        detail.setId(id);
        detail.setEmployee(employee);
        detail.setSalaryStructure(structure);
        detail.setBasicSalary(new BigDecimal("50000.00"));
        detail.setCtc(new BigDecimal("1200000.00"));
        detail.setEffectiveDate(LocalDate.of(2024, 1, 1));
        detail.setActive(true);
        detail.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        detail.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        return detail;
    }

    private PayrollDetail createPayrollDetail(Long id, PayrollRun run, Employee employee, SalaryStructure structure) {
        var detail = new PayrollDetail();
        detail.setId(id);
        detail.setPayrollRun(run);
        detail.setEmployee(employee);
        detail.setSalaryStructure(structure);
        detail.setBasicSalary(new BigDecimal("50000.00"));
        detail.setGrossSalary(new BigDecimal("75000.00"));
        detail.setTotalDeductions(new BigDecimal("6000.00"));
        detail.setNetSalary(new BigDecimal("69000.00"));
        detail.setComponentBreakup("[]");
        detail.setDaysPayable(30);
        detail.setLopDays(0);
        detail.setCreatedAt(LocalDateTime.of(2024, 6, 30, 10, 0));
        detail.setUpdatedAt(LocalDateTime.of(2024, 6, 30, 10, 0));
        return detail;
    }

    // --- getAll tests ---

    @Test
    void getAll_shouldReturnPageOfPayrollRuns() {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        var page = new PageImpl<>(List.of(run), PageRequest.of(0, 20), 1);
        when(payrollRunRepository.findAllByOrderByPeriodYearDescPeriodMonthDesc(any())).thenReturn(page);

        var result = payrollRunService.getAll(PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals("DRAFT", result.getContent().get(0).status());
    }

    // --- getById tests ---

    @Test
    void getById_shouldReturnPayrollRun() {
        var run = createPayrollRun(1L, PayrollRunStatus.COMPUTED);
        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));

        var result = payrollRunService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("COMPUTED", result.status());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(payrollRunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> payrollRunService.getById(99L));
    }

    // --- initialize tests ---

    @Test
    void initialize_shouldCreateDraftRun() {
        var request = new PayrollRunRequest(2024, 6, "June payroll");
        when(payrollRunRepository.findByPeriodYearAndPeriodMonth(2024, 6)).thenReturn(Optional.empty());
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(invocation -> {
            var run = invocation.getArgument(0, PayrollRun.class);
            run.setId(1L);
            run.setCreatedAt(LocalDateTime.now());
            run.setUpdatedAt(LocalDateTime.now());
            return run;
        });

        var result = payrollRunService.initialize(request);

        assertNotNull(result);
        assertEquals(2024, result.periodYear());
        assertEquals(6, result.periodMonth());
        assertEquals("DRAFT", result.status());
        assertEquals("June payroll", result.notes());
        verify(payrollRunRepository).save(any(PayrollRun.class));
    }

    @Test
    void initialize_shouldRejectDuplicatePeriod() {
        var request = new PayrollRunRequest(2024, 6, "June payroll");
        when(payrollRunRepository.findByPeriodYearAndPeriodMonth(2024, 6))
                .thenReturn(Optional.of(createPayrollRun(1L, PayrollRunStatus.DRAFT)));

        assertThrows(BadRequestException.class, () -> payrollRunService.initialize(request));
    }

    // --- computePayroll tests ---

    @Test
    void computePayroll_shouldComputeWithZeroActiveEmployees() throws JsonProcessingException {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeSalaryDetailRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

        var result = payrollRunService.computePayroll(1L);

        assertEquals("COMPUTED", result.status());
        assertEquals(0, result.employeeCount());
        assertEquals(BigDecimal.ZERO.setScale(2), result.totalGross());
    }

    @Test
    void computePayroll_shouldComputeForSingleEmployee() throws JsonProcessingException {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var salaryDetail = createSalaryDetail(1L, employee, structure);

        var basicComp = createStructureComponent(structure, "Basic", SalaryComponentType.EARNING,
                SalaryComputationType.FIXED, null, new BigDecimal("50000.00"), 1);
        var hraComp = createStructureComponent(structure, "HRA", SalaryComponentType.EARNING,
                SalaryComputationType.PERCENTAGE_OF_BASIC, new BigDecimal("50.00"), null, 2);
        var pfComp = createStructureComponent(structure, "PF", SalaryComponentType.DEDUCTION,
                SalaryComputationType.PERCENTAGE_OF_BASIC, new BigDecimal("12.00"), null, 3);

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeSalaryDetailRepository.findByActiveTrue()).thenReturn(List.of(salaryDetail));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderBySortOrder(1L))
                .thenReturn(List.of(basicComp, hraComp, pfComp));
        when(payrollAdjustmentRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Collections.emptyList());
        when(payrollDetailRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Optional.empty());
        when(payrollDetailRepository.save(any(PayrollDetail.class))).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        var result = payrollRunService.computePayroll(1L);

        assertEquals("COMPUTED", result.status());
        assertEquals(1, result.employeeCount());
        // gross = 50000 (fixed basic) + 25000 (50% HRA) = 75000
        assertEquals(new BigDecimal("75000.00"), result.totalGross());
        // deductions = 6000 (12% PF)
        assertEquals(new BigDecimal("6000.00"), result.totalDeductions());
        // net = 75000 - 6000 = 69000
        assertEquals(new BigDecimal("69000.00"), result.totalNet());
    }

    @Test
    void computePayroll_shouldApplyAdjustments() throws JsonProcessingException {
        var run = createPayrollRun(1L, PayrollRunStatus.COMPUTED);
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var salaryDetail = createSalaryDetail(1L, employee, structure);

        var basicComp = createStructureComponent(structure, "Basic", SalaryComponentType.EARNING,
                SalaryComputationType.FIXED, null, new BigDecimal("50000.00"), 1);

        var additionAdj = new PayrollAdjustment();
        additionAdj.setId(1L);
        additionAdj.setPayrollRun(run);
        additionAdj.setEmployee(employee);
        additionAdj.setAdjustmentType(AdjustmentType.ADDITION);
        additionAdj.setComponentName("Bonus");
        additionAdj.setAmount(new BigDecimal("5000.00"));

        var deductionAdj = new PayrollAdjustment();
        deductionAdj.setId(2L);
        deductionAdj.setPayrollRun(run);
        deductionAdj.setEmployee(employee);
        deductionAdj.setAdjustmentType(AdjustmentType.DEDUCTION);
        deductionAdj.setComponentName("Advance Recovery");
        deductionAdj.setAmount(new BigDecimal("2000.00"));

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeSalaryDetailRepository.findByActiveTrue()).thenReturn(List.of(salaryDetail));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderBySortOrder(1L))
                .thenReturn(List.of(basicComp));
        when(payrollAdjustmentRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(List.of(additionAdj, deductionAdj));
        when(payrollDetailRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Optional.empty());
        when(payrollDetailRepository.save(any(PayrollDetail.class))).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        var result = payrollRunService.computePayroll(1L);

        // gross = 50000 + 5000 (addition) = 55000
        assertEquals(new BigDecimal("55000.00"), result.totalGross());
        // deductions = 2000 (deduction adj)
        assertEquals(new BigDecimal("2000.00"), result.totalDeductions());
        // net = 55000 - 2000 = 53000
        assertEquals(new BigDecimal("53000.00"), result.totalNet());
    }

    @Test
    void computePayroll_shouldComputeMultipleEmployees() throws JsonProcessingException {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        var emp1 = createEmployee(1L);
        var emp2 = createEmployee(2L);
        var structure = createSalaryStructure(1L);
        var sd1 = createSalaryDetail(1L, emp1, structure);
        var sd2 = createSalaryDetail(2L, emp2, structure);

        var basicComp = createStructureComponent(structure, "Basic", SalaryComponentType.EARNING,
                SalaryComputationType.FIXED, null, new BigDecimal("50000.00"), 1);

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeSalaryDetailRepository.findByActiveTrue()).thenReturn(List.of(sd1, sd2));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderBySortOrder(1L))
                .thenReturn(List.of(basicComp));
        when(payrollAdjustmentRepository.findByPayrollRunIdAndEmployeeId(eq(1L), anyLong()))
                .thenReturn(Collections.emptyList());
        when(payrollDetailRepository.findByPayrollRunIdAndEmployeeId(eq(1L), anyLong()))
                .thenReturn(Optional.empty());
        when(payrollDetailRepository.save(any(PayrollDetail.class))).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        var result = payrollRunService.computePayroll(1L);

        assertEquals(2, result.employeeCount());
        assertEquals(new BigDecimal("100000.00"), result.totalGross());
        assertEquals(new BigDecimal("100000.00"), result.totalNet());
    }

    @Test
    void computePayroll_shouldRejectInvalidStatus() {
        var run = createPayrollRun(1L, PayrollRunStatus.FINALIZED);
        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));

        assertThrows(BadRequestException.class, () -> payrollRunService.computePayroll(1L));
    }

    @Test
    void computePayroll_shouldThrowWhenNotFound() {
        when(payrollRunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> payrollRunService.computePayroll(99L));
    }

    @Test
    void computePayroll_shouldHandleSlabBasedComputation() throws JsonProcessingException {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var salaryDetail = createSalaryDetail(1L, employee, structure);

        var slabComp = createStructureComponent(structure, "Professional Tax", SalaryComponentType.DEDUCTION,
                SalaryComputationType.SLAB_BASED, null, new BigDecimal("200.00"), 1);

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeSalaryDetailRepository.findByActiveTrue()).thenReturn(List.of(salaryDetail));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderBySortOrder(1L))
                .thenReturn(List.of(slabComp));
        when(payrollAdjustmentRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Collections.emptyList());
        when(payrollDetailRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Optional.empty());
        when(payrollDetailRepository.save(any(PayrollDetail.class))).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        var result = payrollRunService.computePayroll(1L);

        assertEquals(new BigDecimal("200.00"), result.totalDeductions());
    }

    @Test
    void computePayroll_shouldHandleNullFixedAmount() throws JsonProcessingException {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var salaryDetail = createSalaryDetail(1L, employee, structure);

        var comp = createStructureComponent(structure, "Misc", SalaryComponentType.EARNING,
                SalaryComputationType.FIXED, null, null, 1);

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeSalaryDetailRepository.findByActiveTrue()).thenReturn(List.of(salaryDetail));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderBySortOrder(1L))
                .thenReturn(List.of(comp));
        when(payrollAdjustmentRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Collections.emptyList());
        when(payrollDetailRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Optional.empty());
        when(payrollDetailRepository.save(any(PayrollDetail.class))).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        var result = payrollRunService.computePayroll(1L);

        assertEquals(BigDecimal.ZERO.setScale(2), result.totalGross());
    }

    @Test
    void computePayroll_shouldHandleJsonSerializationError() throws JsonProcessingException {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var salaryDetail = createSalaryDetail(1L, employee, structure);

        var basicComp = createStructureComponent(structure, "Basic", SalaryComponentType.EARNING,
                SalaryComputationType.FIXED, null, new BigDecimal("50000.00"), 1);

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeSalaryDetailRepository.findByActiveTrue()).thenReturn(List.of(salaryDetail));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderBySortOrder(1L))
                .thenReturn(List.of(basicComp));
        when(payrollAdjustmentRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Collections.emptyList());
        when(payrollDetailRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Optional.empty());
        when(payrollDetailRepository.save(any(PayrollDetail.class))).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("error") {});

        var result = payrollRunService.computePayroll(1L);

        // Should still compute successfully, just with empty breakup
        assertEquals("COMPUTED", result.status());
        assertEquals(1, result.employeeCount());
    }

    @Test
    void computePayroll_shouldUpdateExistingDetail() throws JsonProcessingException {
        var run = createPayrollRun(1L, PayrollRunStatus.COMPUTED);
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var salaryDetail = createSalaryDetail(1L, employee, structure);
        var existingDetail = createPayrollDetail(10L, run, employee, structure);

        var basicComp = createStructureComponent(structure, "Basic", SalaryComponentType.EARNING,
                SalaryComputationType.FIXED, null, new BigDecimal("50000.00"), 1);

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeSalaryDetailRepository.findByActiveTrue()).thenReturn(List.of(salaryDetail));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderBySortOrder(1L))
                .thenReturn(List.of(basicComp));
        when(payrollAdjustmentRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Collections.emptyList());
        when(payrollDetailRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Optional.of(existingDetail));
        when(payrollDetailRepository.save(any(PayrollDetail.class))).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        var result = payrollRunService.computePayroll(1L);

        assertEquals("COMPUTED", result.status());
        verify(payrollDetailRepository).save(any(PayrollDetail.class));
    }

    // --- getDetails tests ---

    @Test
    void getDetails_shouldReturnPageOfDetails() {
        var run = createPayrollRun(1L, PayrollRunStatus.COMPUTED);
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var detail = createPayrollDetail(1L, run, employee, structure);
        var page = new PageImpl<>(List.of(detail), PageRequest.of(0, 20), 1);

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollDetailRepository.findByPayrollRunId(eq(1L), any())).thenReturn(page);

        var result = payrollRunService.getDetails(1L, PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).employeeName());
    }

    @Test
    void getDetails_shouldThrowWhenRunNotFound() {
        when(payrollRunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> payrollRunService.getDetails(99L, PageRequest.of(0, 20)));
    }

    // --- getDetailByEmployee tests ---

    @Test
    void getDetailByEmployee_shouldReturnDetail() {
        var run = createPayrollRun(1L, PayrollRunStatus.COMPUTED);
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var detail = createPayrollDetail(1L, run, employee, structure);

        when(payrollDetailRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Optional.of(detail));

        var result = payrollRunService.getDetailByEmployee(1L, 1L);

        assertNotNull(result);
        assertEquals("EMP001", result.employeeCode());
    }

    @Test
    void getDetailByEmployee_shouldThrowWhenNotFound() {
        when(payrollDetailRepository.findByPayrollRunIdAndEmployeeId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> payrollRunService.getDetailByEmployee(1L, 99L));
    }

    // --- verify tests ---

    @Test
    void verify_shouldTransitionFromComputedToVerified() {
        var run = createPayrollRun(1L, PayrollRunStatus.COMPUTED);
        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));

        var result = payrollRunService.verify(1L);

        assertEquals("VERIFIED", result.status());
    }

    @Test
    void verify_shouldRejectNonComputedStatus() {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));

        assertThrows(BadRequestException.class, () -> payrollRunService.verify(1L));
    }

    @Test
    void verify_shouldThrowWhenNotFound() {
        when(payrollRunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> payrollRunService.verify(99L));
    }

    // --- finalize tests ---

    @Test
    void finalizeRun_shouldTransitionFromVerifiedToFinalized() {
        var run = createPayrollRun(1L, PayrollRunStatus.VERIFIED);
        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));

        var result = payrollRunService.finalizeRun(1L);

        assertEquals("FINALIZED", result.status());
    }

    @Test
    void finalizeRun_shouldRejectNonVerifiedStatus() {
        var run = createPayrollRun(1L, PayrollRunStatus.COMPUTED);
        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));

        assertThrows(BadRequestException.class, () -> payrollRunService.finalizeRun(1L));
    }

    @Test
    void finalizeRun_shouldThrowWhenNotFound() {
        when(payrollRunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> payrollRunService.finalizeRun(99L));
    }

    // --- reverse tests ---

    @Test
    void reverse_shouldTransitionFromComputedToReversed() {
        var run = createPayrollRun(1L, PayrollRunStatus.COMPUTED);
        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));

        var result = payrollRunService.reverse(1L);

        assertEquals("REVERSED", result.status());
    }

    @Test
    void reverse_shouldTransitionFromVerifiedToReversed() {
        var run = createPayrollRun(1L, PayrollRunStatus.VERIFIED);
        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));

        var result = payrollRunService.reverse(1L);

        assertEquals("REVERSED", result.status());
    }

    @Test
    void reverse_shouldRejectFinalizedStatus() {
        var run = createPayrollRun(1L, PayrollRunStatus.FINALIZED);
        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));

        assertThrows(BadRequestException.class, () -> payrollRunService.reverse(1L));
    }

    @Test
    void reverse_shouldRejectDraftStatus() {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));

        assertThrows(BadRequestException.class, () -> payrollRunService.reverse(1L));
    }

    @Test
    void reverse_shouldThrowWhenNotFound() {
        when(payrollRunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> payrollRunService.reverse(99L));
    }

    // --- computePayroll with null percentage ---

    @Test
    void computePayroll_shouldHandleNullPercentageValue() throws JsonProcessingException {
        var run = createPayrollRun(1L, PayrollRunStatus.DRAFT);
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var salaryDetail = createSalaryDetail(1L, employee, structure);

        var comp = createStructureComponent(structure, "HRA", SalaryComponentType.EARNING,
                SalaryComputationType.PERCENTAGE_OF_BASIC, null, null, 1);

        when(payrollRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(i -> i.getArgument(0));
        when(employeeSalaryDetailRepository.findByActiveTrue()).thenReturn(List.of(salaryDetail));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderBySortOrder(1L))
                .thenReturn(List.of(comp));
        when(payrollAdjustmentRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Collections.emptyList());
        when(payrollDetailRepository.findByPayrollRunIdAndEmployeeId(1L, 1L))
                .thenReturn(Optional.empty());
        when(payrollDetailRepository.save(any(PayrollDetail.class))).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        var result = payrollRunService.computePayroll(1L);

        assertEquals(new BigDecimal("0.00"), result.totalGross());
    }
}
