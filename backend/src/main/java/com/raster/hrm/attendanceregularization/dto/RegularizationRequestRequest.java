package com.raster.hrm.attendanceregularization.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegularizationRequestRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Request date is required")
        LocalDate requestDate,

        @NotNull(message = "Type is required")
        @Size(min = 1, message = "Type must not be empty")
        String type,

        @NotNull(message = "Reason is required")
        @Size(min = 1, max = 500, message = "Reason must be between 1 and 500 characters")
        String reason,

        LocalDateTime originalPunchIn,

        LocalDateTime originalPunchOut,

        @NotNull(message = "Corrected punch in is required")
        LocalDateTime correctedPunchIn,

        @NotNull(message = "Corrected punch out is required")
        LocalDateTime correctedPunchOut,

        @Size(max = 500, message = "Remarks must not exceed 500 characters")
        String remarks
) {
}
