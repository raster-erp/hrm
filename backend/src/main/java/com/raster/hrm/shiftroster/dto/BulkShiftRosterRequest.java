package com.raster.hrm.shiftroster.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record BulkShiftRosterRequest(
        @NotEmpty(message = "Employee IDs are required")
        List<Long> employeeIds,

        @NotNull(message = "Shift ID is required")
        Long shiftId,

        @NotNull(message = "Effective date is required")
        LocalDate effectiveDate,

        LocalDate endDate,

        Long rotationPatternId
) {
}
