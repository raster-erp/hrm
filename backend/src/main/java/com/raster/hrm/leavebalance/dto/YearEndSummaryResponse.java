package com.raster.hrm.leavebalance.dto;

import java.math.BigDecimal;

public record YearEndSummaryResponse(
        int processedYear,
        int nextYear,
        int employeesProcessed,
        int balancesCreated,
        BigDecimal totalCarryForwarded,
        BigDecimal totalLapsed
) {
}
