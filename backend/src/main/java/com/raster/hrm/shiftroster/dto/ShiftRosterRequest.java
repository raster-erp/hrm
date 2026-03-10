package com.raster.hrm.shiftroster.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ShiftRosterRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Shift ID is required")
        Long shiftId,

        @NotNull(message = "Effective date is required")
        LocalDate effectiveDate,

        LocalDate endDate,

        Long rotationPatternId
) {
}
