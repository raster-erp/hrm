package com.raster.hrm.overtime.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OvertimePolicyRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotNull(message = "Type is required")
        String type,

        @NotNull(message = "Rate multiplier is required")
        @DecimalMin(value = "0.01", message = "Rate multiplier must be greater than 0")
        BigDecimal rateMultiplier,

        @Min(value = 0, message = "Minimum overtime minutes must be non-negative")
        Integer minOvertimeMinutes,

        Integer maxOvertimeMinutesPerDay,

        Integer maxOvertimeMinutesPerMonth,

        Boolean requiresApproval,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
