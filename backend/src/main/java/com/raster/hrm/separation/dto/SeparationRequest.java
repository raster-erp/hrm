package com.raster.hrm.separation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SeparationRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotBlank(message = "Separation type is required")
        @Size(max = 20, message = "Separation type must not exceed 20 characters")
        String separationType,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason,

        @NotNull(message = "Notice date is required")
        LocalDate noticeDate,

        @NotNull(message = "Last working day is required")
        LocalDate lastWorkingDay
) {
}
