package com.raster.hrm.tds;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.tds.dto.TaxComputationRequest;
import com.raster.hrm.tds.entity.InvestmentDeclaration;
import com.raster.hrm.tds.entity.TaxComputation;
import com.raster.hrm.tds.entity.TaxRegime;
import com.raster.hrm.tds.entity.TaxSlab;
import com.raster.hrm.tds.repository.InvestmentDeclarationRepository;
import com.raster.hrm.tds.repository.TaxComputationRepository;
import com.raster.hrm.tds.repository.TaxSlabRepository;
import com.raster.hrm.tds.service.TaxComputationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxComputationServiceTest {

    @Mock
    private TaxComputationRepository taxComputationRepository;

    @Mock
    private TaxSlabRepository taxSlabRepository;

    @Mock
    private InvestmentDeclarationRepository investmentDeclarationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private TaxComputationService taxComputationService;

    private Employee createEmployee(Long id, BigDecimal basicSalary) {
        var employee = new Employee();
        employee.setId(id);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setBasicSalary(basicSalary);
        return employee;
    }

    private TaxSlab createTaxSlab(TaxRegime regime, String fy, BigDecimal from, BigDecimal to, BigDecimal rate) {
        var slab = new TaxSlab();
        slab.setRegime(regime);
        slab.setFinancialYear(fy);
        slab.setSlabFrom(from);
        slab.setSlabTo(to);
        slab.setRate(rate);
        slab.setActive(true);
        slab.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        slab.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return slab;
    }

    private TaxComputation createComputation(Long id, Employee employee, String fy, int month, BigDecimal tds) {
        var computation = new TaxComputation();
        computation.setId(id);
        computation.setEmployee(employee);
        computation.setFinancialYear(fy);
        computation.setMonth(month);
        computation.setGrossAnnualIncome(new BigDecimal("1200000.00"));
        computation.setTotalExemptions(BigDecimal.ZERO);
        computation.setTaxableIncome(new BigDecimal("1150000.00"));
        computation.setTotalAnnualTax(new BigDecimal("72500.00"));
        computation.setMonthlyTds(tds);
        computation.setCess(new BigDecimal("2900.00"));
        computation.setSurcharge(BigDecimal.ZERO);
        computation.setTdsDeductedTillDate(BigDecimal.ZERO);
        computation.setRemainingTds(new BigDecimal("75400.00"));
        computation.setRegime(TaxRegime.NEW);
        computation.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        computation.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return computation;
    }

    private List<TaxSlab> createNewRegimeSlabs() {
        return List.of(
                createTaxSlab(TaxRegime.NEW, "2025-26", new BigDecimal("0"), new BigDecimal("300000"), new BigDecimal("0")),
                createTaxSlab(TaxRegime.NEW, "2025-26", new BigDecimal("300000"), new BigDecimal("700000"), new BigDecimal("5")),
                createTaxSlab(TaxRegime.NEW, "2025-26", new BigDecimal("700000"), new BigDecimal("1000000"), new BigDecimal("10")),
                createTaxSlab(TaxRegime.NEW, "2025-26", new BigDecimal("1000000"), new BigDecimal("1200000"), new BigDecimal("15")),
                createTaxSlab(TaxRegime.NEW, "2025-26", new BigDecimal("1200000"), new BigDecimal("1500000"), new BigDecimal("20")),
                createTaxSlab(TaxRegime.NEW, "2025-26", new BigDecimal("1500000"), null, new BigDecimal("30"))
        );
    }

    @Test
    void computeMonthlyTds_shouldComputeCorrectly() {
        // Employee with basicSalary=100000, annual=1200000
        // After standard deduction 50000: taxableIncome = 1150000
        // Tax: 0-3L=0, 3L-7L=4L*5%=20000, 7L-10L=3L*10%=30000, 10L-11.5L=1.5L*15%=22500
        // Total tax = 72500, cess = 72500*4% = 2900, total = 75400
        // Month 1: monthlyTds = 75400/12 = 6283.33
        var employee = createEmployee(1L, new BigDecimal("100000"));
        var slabs = createNewRegimeSlabs();
        var request = new TaxComputationRequest(1L, "2025-26", 1);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(investmentDeclarationRepository.findByEmployeeIdAndFinancialYear(1L, "2025-26"))
                .thenReturn(Optional.empty());
        when(taxSlabRepository.findByRegimeAndFinancialYearAndActiveOrderBySlabFromAsc(TaxRegime.NEW, "2025-26", true))
                .thenReturn(slabs);
        when(taxComputationRepository.findByEmployeeIdAndFinancialYearOrderByMonthAsc(1L, "2025-26"))
                .thenReturn(List.of());
        when(taxComputationRepository.findByEmployeeIdAndFinancialYearAndMonth(1L, "2025-26", 1))
                .thenReturn(Optional.empty());
        when(taxComputationRepository.save(any(TaxComputation.class))).thenAnswer(invocation -> {
            TaxComputation c = invocation.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        var result = taxComputationService.computeMonthlyTds(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("1200000.00"), result.grossAnnualIncome());
        assertEquals(new BigDecimal("1150000.00"), result.taxableIncome());
        assertEquals(new BigDecimal("72500.00"), result.totalAnnualTax());
        assertEquals(new BigDecimal("2900.00"), result.cess());
        assertEquals(new BigDecimal("6283.33"), result.monthlyTds());
        assertEquals("NEW", result.regime());
        verify(taxComputationRepository).save(any(TaxComputation.class));
    }

    @Test
    void computeMonthlyTds_shouldThrowWhenEmployeeNotFound() {
        var request = new TaxComputationRequest(999L, "2025-26", 1);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taxComputationService.computeMonthlyTds(request));
    }

    @Test
    void computeMonthlyTds_shouldHandleNoDeclaration() {
        var employee = createEmployee(1L, new BigDecimal("100000"));
        var slabs = createNewRegimeSlabs();
        var request = new TaxComputationRequest(1L, "2025-26", 1);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(investmentDeclarationRepository.findByEmployeeIdAndFinancialYear(1L, "2025-26"))
                .thenReturn(Optional.empty());
        when(taxSlabRepository.findByRegimeAndFinancialYearAndActiveOrderBySlabFromAsc(TaxRegime.NEW, "2025-26", true))
                .thenReturn(slabs);
        when(taxComputationRepository.findByEmployeeIdAndFinancialYearOrderByMonthAsc(1L, "2025-26"))
                .thenReturn(List.of());
        when(taxComputationRepository.findByEmployeeIdAndFinancialYearAndMonth(1L, "2025-26", 1))
                .thenReturn(Optional.empty());
        when(taxComputationRepository.save(any(TaxComputation.class))).thenAnswer(invocation -> {
            TaxComputation c = invocation.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        var result = taxComputationService.computeMonthlyTds(request);

        assertNotNull(result);
        assertEquals("NEW", result.regime());
        assertEquals(new BigDecimal("0.00"), result.totalExemptions());
    }

    @Test
    void computeMonthlyTds_shouldUseVerifiedAmountWhenAvailable() {
        var employee = createEmployee(1L, new BigDecimal("100000"));
        var slabs = createNewRegimeSlabs();
        var request = new TaxComputationRequest(1L, "2025-26", 1);

        var declaration = new InvestmentDeclaration();
        declaration.setId(1L);
        declaration.setEmployee(employee);
        declaration.setFinancialYear("2025-26");
        declaration.setRegime(TaxRegime.NEW);
        declaration.setTotalDeclaredAmount(new BigDecimal("200000"));
        declaration.setTotalVerifiedAmount(new BigDecimal("150000"));
        declaration.setItems(new ArrayList<>());

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(investmentDeclarationRepository.findByEmployeeIdAndFinancialYear(1L, "2025-26"))
                .thenReturn(Optional.of(declaration));
        when(taxSlabRepository.findByRegimeAndFinancialYearAndActiveOrderBySlabFromAsc(TaxRegime.NEW, "2025-26", true))
                .thenReturn(slabs);
        when(taxComputationRepository.findByEmployeeIdAndFinancialYearOrderByMonthAsc(1L, "2025-26"))
                .thenReturn(List.of());
        when(taxComputationRepository.findByEmployeeIdAndFinancialYearAndMonth(1L, "2025-26", 1))
                .thenReturn(Optional.empty());
        when(taxComputationRepository.save(any(TaxComputation.class))).thenAnswer(invocation -> {
            TaxComputation c = invocation.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        var result = taxComputationService.computeMonthlyTds(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("150000.00"), result.totalExemptions());
        // taxableIncome = 1200000 - 50000 - 150000 = 1000000
        assertEquals(new BigDecimal("1000000.00"), result.taxableIncome());
    }

    @Test
    void getByEmployeeAndYear_shouldReturnComputations() {
        var employee = createEmployee(1L, new BigDecimal("100000"));
        var computations = List.of(
                createComputation(1L, employee, "2025-26", 1, new BigDecimal("6283.33")),
                createComputation(2L, employee, "2025-26", 2, new BigDecimal("6283.33"))
        );
        when(taxComputationRepository.findByEmployeeIdAndFinancialYearOrderByMonthAsc(1L, "2025-26"))
                .thenReturn(computations);

        var result = taxComputationService.getByEmployeeAndYear(1L, "2025-26");

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).month());
        assertEquals(2, result.get(1).month());
    }

    @Test
    void getByEmployeeYearMonth_shouldReturnComputation() {
        var employee = createEmployee(1L, new BigDecimal("100000"));
        var computation = createComputation(1L, employee, "2025-26", 1, new BigDecimal("6283.33"));
        when(taxComputationRepository.findByEmployeeIdAndFinancialYearAndMonth(1L, "2025-26", 1))
                .thenReturn(Optional.of(computation));

        var result = taxComputationService.getByEmployeeYearMonth(1L, "2025-26", 1);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1, result.month());
        assertEquals("John Doe", result.employeeName());
    }

    @Test
    void getByEmployeeYearMonth_shouldThrowWhenNotFound() {
        when(taxComputationRepository.findByEmployeeIdAndFinancialYearAndMonth(1L, "2025-26", 1))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taxComputationService.getByEmployeeYearMonth(1L, "2025-26", 1));
    }

    @Test
    void generateForm16Data_shouldReturnFormData() {
        var employee = createEmployee(1L, new BigDecimal("100000"));
        var computations = List.of(
                createComputation(1L, employee, "2025-26", 1, new BigDecimal("6283.33"))
        );
        var slabs = createNewRegimeSlabs();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(taxComputationRepository.findByEmployeeIdAndFinancialYearOrderByMonthAsc(1L, "2025-26"))
                .thenReturn(computations);
        when(investmentDeclarationRepository.findByEmployeeIdAndFinancialYear(1L, "2025-26"))
                .thenReturn(Optional.empty());
        when(taxSlabRepository.findByRegimeAndFinancialYearAndActiveOrderBySlabFromAsc(TaxRegime.NEW, "2025-26", true))
                .thenReturn(slabs);

        var result = taxComputationService.generateForm16Data(1L, "2025-26");

        assertNotNull(result);
        assertEquals(1L, result.employeeId());
        assertEquals("John Doe", result.employeeName());
        assertEquals("2025-26", result.financialYear());
        assertEquals("NEW", result.regime());
        assertEquals(new BigDecimal("1200000.00"), result.grossAnnualIncome());
        assertEquals(new BigDecimal("6283.33"), result.totalTdsDeducted());
        assertEquals(1, result.monthlyBreakup().size());
    }

    @Test
    void generateForm16Data_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taxComputationService.generateForm16Data(999L, "2025-26"));
    }
}
