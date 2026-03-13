package com.raster.hrm.tds.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaxComputationResponse(
        Long id, Long employeeId, String employeeName,
        String financialYear, int month,
        BigDecimal grossAnnualIncome, BigDecimal totalExemptions,
        BigDecimal taxableIncome, BigDecimal totalAnnualTax,
        BigDecimal monthlyTds, BigDecimal cess, BigDecimal surcharge,
        BigDecimal tdsDeductedTillDate, BigDecimal remainingTds,
        String regime,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {}
