package com.raster.hrm.leavepolicy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LeavePolicyRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotNull(message = "Leave type ID is required")
        Long leaveTypeId,

        @NotNull(message = "Accrual frequency is required")
        String accrualFrequency,

        @NotNull(message = "Accrual days is required")
        @DecimalMin(value = "0.01", message = "Accrual days must be greater than 0")
        BigDecimal accrualDays,

        BigDecimal maxAccumulation,

        BigDecimal carryForwardLimit,

        Boolean proRataForNewJoiners,

        @Min(value = 0, message = "Minimum service days required must be non-negative")
        Integer minServiceDaysRequired,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
