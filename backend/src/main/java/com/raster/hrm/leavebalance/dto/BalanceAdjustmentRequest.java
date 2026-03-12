package com.raster.hrm.leavebalance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BalanceAdjustmentRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Leave type ID is required")
        Long leaveTypeId,

        @NotNull(message = "Year is required")
        Integer year,

        @NotNull(message = "Amount is required")
        BigDecimal amount,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Size(max = 100, message = "Adjusted by must not exceed 100 characters")
        String adjustedBy
) {
}
