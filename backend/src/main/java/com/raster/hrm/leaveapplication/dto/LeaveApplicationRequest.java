package com.raster.hrm.leaveapplication.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LeaveApplicationRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Leave type ID is required")
        Long leaveTypeId,

        @NotNull(message = "From date is required")
        LocalDate fromDate,

        @NotNull(message = "To date is required")
        LocalDate toDate,

        @NotNull(message = "Number of days is required")
        @Positive(message = "Number of days must be positive")
        BigDecimal numberOfDays,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason,

        @Size(max = 500, message = "Remarks must not exceed 500 characters")
        String remarks
) {
}
