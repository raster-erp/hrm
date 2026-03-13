package com.raster.hrm.payroll.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PayrollRunRequest(
        @NotNull @Min(2000) @Max(2100) Integer periodYear,
        @NotNull @Min(1) @Max(12) Integer periodMonth,
        String notes
) {}
