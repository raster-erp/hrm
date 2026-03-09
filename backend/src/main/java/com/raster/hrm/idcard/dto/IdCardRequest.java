package com.raster.hrm.idcard.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record IdCardRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Issue date is required")
        LocalDate issueDate,

        LocalDate expiryDate
) {
}
