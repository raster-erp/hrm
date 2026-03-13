package com.raster.hrm.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PayrollAdjustmentRequest(
        @NotNull Long payrollRunId,
        @NotNull Long employeeId,
        @NotBlank String adjustmentType,
        @NotBlank String componentName,
        @NotNull @Positive BigDecimal amount,
        String reason
) {}
