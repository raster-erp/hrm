package com.raster.hrm.salarystructure.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SalaryStructureComponentRequest(
        @NotNull(message = "Component ID is required")
        Long salaryComponentId,

        @NotNull(message = "Computation type is required")
        String computationType,

        BigDecimal percentageValue,

        BigDecimal fixedAmount,

        int sortOrder
) {
}
