package com.raster.hrm.tds.dto;

import java.math.BigDecimal;
import java.util.List;

public record Form16DataResponse(
        Long employeeId, String employeeName, String financialYear,
        String regime, BigDecimal grossAnnualIncome,
        BigDecimal totalExemptions, BigDecimal taxableIncome,
        BigDecimal totalTaxPayable, BigDecimal totalTdsDeducted,
        BigDecimal cess, BigDecimal surcharge,
        List<InvestmentDeclarationItemResponse> verifiedInvestments,
        List<TaxComputationResponse> monthlyBreakup
) {}
