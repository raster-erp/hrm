package com.raster.hrm.tds.service;

import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.tds.dto.Form16DataResponse;
import com.raster.hrm.tds.dto.InvestmentDeclarationItemResponse;
import com.raster.hrm.tds.dto.TaxComputationRequest;
import com.raster.hrm.tds.dto.TaxComputationResponse;
import com.raster.hrm.tds.entity.InvestmentDeclaration;
import com.raster.hrm.tds.entity.InvestmentDeclarationItem;
import com.raster.hrm.tds.entity.TaxComputation;
import com.raster.hrm.tds.entity.TaxRegime;
import com.raster.hrm.tds.entity.TaxSlab;
import com.raster.hrm.tds.repository.InvestmentDeclarationRepository;
import com.raster.hrm.tds.repository.TaxComputationRepository;
import com.raster.hrm.tds.repository.TaxSlabRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class TaxComputationService {

    private static final Logger log = LoggerFactory.getLogger(TaxComputationService.class);

    private static final BigDecimal STANDARD_DEDUCTION = new BigDecimal("50000");
    private static final BigDecimal CESS_RATE = new BigDecimal("0.04");
    private static final int MONTHS_IN_YEAR = 12;

    private final TaxComputationRepository taxComputationRepository;
    private final TaxSlabRepository taxSlabRepository;
    private final InvestmentDeclarationRepository investmentDeclarationRepository;
    private final EmployeeRepository employeeRepository;

    public TaxComputationService(TaxComputationRepository taxComputationRepository,
                                  TaxSlabRepository taxSlabRepository,
                                  InvestmentDeclarationRepository investmentDeclarationRepository,
                                  EmployeeRepository employeeRepository) {
        this.taxComputationRepository = taxComputationRepository;
        this.taxSlabRepository = taxSlabRepository;
        this.investmentDeclarationRepository = investmentDeclarationRepository;
        this.employeeRepository = employeeRepository;
    }

    public TaxComputationResponse computeMonthlyTds(TaxComputationRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var grossAnnualIncome = employee.getBasicSalary()
                .multiply(BigDecimal.valueOf(MONTHS_IN_YEAR))
                .setScale(2, RoundingMode.HALF_UP);

        var declaration = investmentDeclarationRepository
                .findByEmployeeIdAndFinancialYear(request.employeeId(), request.financialYear())
                .orElse(null);

        var regime = TaxRegime.NEW;
        var totalExemptions = BigDecimal.ZERO;

        if (declaration != null) {
            regime = declaration.getRegime();
            var verifiedAmount = declaration.getTotalVerifiedAmount();
            var declaredAmount = declaration.getTotalDeclaredAmount();
            totalExemptions = (verifiedAmount != null && verifiedAmount.compareTo(BigDecimal.ZERO) > 0)
                    ? verifiedAmount
                    : declaredAmount;
        }

        var standardDeduction = STANDARD_DEDUCTION;
        var taxableIncome = grossAnnualIncome
                .subtract(standardDeduction)
                .subtract(totalExemptions)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        var slabs = taxSlabRepository.findByRegimeAndFinancialYearAndActiveOrderBySlabFromAsc(
                regime, request.financialYear(), true);

        var totalAnnualTax = calculateTaxForIncome(taxableIncome, slabs);

        var cess = totalAnnualTax.multiply(CESS_RATE).setScale(2, RoundingMode.HALF_UP);
        var surcharge = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        var totalTaxWithCess = totalAnnualTax.add(cess).add(surcharge);

        var previousComputations = taxComputationRepository
                .findByEmployeeIdAndFinancialYearOrderByMonthAsc(request.employeeId(), request.financialYear());
        var tdsDeductedTillDate = previousComputations.stream()
                .filter(c -> c.getMonth() < request.month())
                .map(TaxComputation::getMonthlyTds)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        var remainingTds = totalTaxWithCess.subtract(tdsDeductedTillDate)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        int remainingMonths = MONTHS_IN_YEAR - request.month() + 1;
        var monthlyTds = (remainingMonths > 0)
                ? remainingTds.divide(BigDecimal.valueOf(remainingMonths), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        var computation = taxComputationRepository
                .findByEmployeeIdAndFinancialYearAndMonth(request.employeeId(), request.financialYear(), request.month())
                .orElse(new TaxComputation());

        computation.setEmployee(employee);
        computation.setFinancialYear(request.financialYear());
        computation.setMonth(request.month());
        computation.setGrossAnnualIncome(grossAnnualIncome);
        computation.setTotalExemptions(totalExemptions.setScale(2, RoundingMode.HALF_UP));
        computation.setTaxableIncome(taxableIncome);
        computation.setTotalAnnualTax(totalAnnualTax);
        computation.setMonthlyTds(monthlyTds);
        computation.setCess(cess);
        computation.setSurcharge(surcharge);
        computation.setTdsDeductedTillDate(tdsDeductedTillDate);
        computation.setRemainingTds(remainingTds);
        computation.setRegime(regime);

        var saved = taxComputationRepository.save(computation);
        log.info("Computed monthly TDS for employee: {} year: {} month: {} tds: {}",
                employee.getId(), request.financialYear(), request.month(), monthlyTds);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaxComputationResponse> getByEmployeeAndYear(Long employeeId, String financialYear) {
        return taxComputationRepository.findByEmployeeIdAndFinancialYearOrderByMonthAsc(employeeId, financialYear)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaxComputationResponse getByEmployeeYearMonth(Long employeeId, String financialYear, int month) {
        var computation = taxComputationRepository
                .findByEmployeeIdAndFinancialYearAndMonth(employeeId, financialYear, month)
                .orElseThrow(() -> new ResourceNotFoundException("TaxComputation", "employeeId+financialYear+month",
                        employeeId + "/" + financialYear + "/" + month));
        return mapToResponse(computation);
    }

    @Transactional(readOnly = true)
    public Form16DataResponse generateForm16Data(Long employeeId, String financialYear) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        var computations = taxComputationRepository
                .findByEmployeeIdAndFinancialYearOrderByMonthAsc(employeeId, financialYear);

        var monthlyBreakup = computations.stream()
                .map(this::mapToResponse)
                .toList();

        var grossAnnualIncome = employee.getBasicSalary()
                .multiply(BigDecimal.valueOf(MONTHS_IN_YEAR))
                .setScale(2, RoundingMode.HALF_UP);

        var totalTdsDeducted = computations.stream()
                .map(TaxComputation::getMonthlyTds)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        var declaration = investmentDeclarationRepository
                .findByEmployeeIdAndFinancialYear(employeeId, financialYear)
                .orElse(null);

        var regime = TaxRegime.NEW;
        var totalExemptions = BigDecimal.ZERO;
        List<InvestmentDeclarationItemResponse> verifiedInvestments = List.of();

        if (declaration != null) {
            regime = declaration.getRegime();
            totalExemptions = declaration.getTotalVerifiedAmount() != null
                    ? declaration.getTotalVerifiedAmount()
                    : declaration.getTotalDeclaredAmount();
            verifiedInvestments = declaration.getItems().stream()
                    .map(this::mapItemToResponse)
                    .toList();
        }

        var taxableIncome = grossAnnualIncome
                .subtract(STANDARD_DEDUCTION)
                .subtract(totalExemptions)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        var slabs = taxSlabRepository.findByRegimeAndFinancialYearAndActiveOrderBySlabFromAsc(
                regime, financialYear, true);
        var totalTaxPayable = calculateTaxForIncome(taxableIncome, slabs);
        var cess = totalTaxPayable.multiply(CESS_RATE).setScale(2, RoundingMode.HALF_UP);
        var surcharge = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        var employeeName = employee.getFirstName() + " " + employee.getLastName();

        return new Form16DataResponse(
                employee.getId(),
                employeeName,
                financialYear,
                regime.name(),
                grossAnnualIncome,
                totalExemptions.setScale(2, RoundingMode.HALF_UP),
                taxableIncome,
                totalTaxPayable.add(cess).add(surcharge),
                totalTdsDeducted,
                cess,
                surcharge,
                verifiedInvestments,
                monthlyBreakup
        );
    }

    private BigDecimal calculateTaxForIncome(BigDecimal taxableIncome, List<TaxSlab> slabs) {
        var totalTax = BigDecimal.ZERO;

        for (var slab : slabs) {
            if (taxableIncome.compareTo(slab.getSlabFrom()) <= 0) {
                break;
            }

            var slabUpperLimit = slab.getSlabTo() != null
                    ? slab.getSlabTo()
                    : taxableIncome;

            var taxableInSlab = taxableIncome.min(slabUpperLimit).subtract(slab.getSlabFrom()).max(BigDecimal.ZERO);

            var rate = slab.getRate().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            var slabTax = taxableInSlab.multiply(rate);
            totalTax = totalTax.add(slabTax);
        }

        return totalTax.setScale(2, RoundingMode.HALF_UP);
    }

    private TaxComputationResponse mapToResponse(TaxComputation computation) {
        var employee = computation.getEmployee();
        var employeeName = employee.getFirstName() + " " + employee.getLastName();

        return new TaxComputationResponse(
                computation.getId(),
                employee.getId(),
                employeeName,
                computation.getFinancialYear(),
                computation.getMonth(),
                computation.getGrossAnnualIncome(),
                computation.getTotalExemptions(),
                computation.getTaxableIncome(),
                computation.getTotalAnnualTax(),
                computation.getMonthlyTds(),
                computation.getCess(),
                computation.getSurcharge(),
                computation.getTdsDeductedTillDate(),
                computation.getRemainingTds(),
                computation.getRegime().name(),
                computation.getCreatedAt(),
                computation.getUpdatedAt()
        );
    }

    private InvestmentDeclarationItemResponse mapItemToResponse(InvestmentDeclarationItem item) {
        return new InvestmentDeclarationItemResponse(
                item.getId(),
                item.getSection(),
                item.getDescription(),
                item.getDeclaredAmount(),
                item.getVerifiedAmount(),
                item.getProofStatus().name(),
                item.getProofDocumentName(),
                item.getProofRemarks(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
