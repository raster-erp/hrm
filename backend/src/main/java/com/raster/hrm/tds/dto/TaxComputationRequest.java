package com.raster.hrm.tds.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaxComputationRequest(
        @NotNull(message = "Employee ID is required") Long employeeId,
        @NotBlank(message = "Financial year is required") String financialYear,
        @NotNull(message = "Month is required") @Min(1) @Max(12) Integer month
) {}
