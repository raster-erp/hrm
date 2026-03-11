package com.raster.hrm.attendancedeviation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AttendanceDeviationRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Deviation date is required")
        LocalDate deviationDate,

        @NotNull(message = "Type is required")
        @Size(min = 1, message = "Type must not be empty")
        String type,

        @NotNull(message = "Deviation minutes is required")
        @Min(value = 1, message = "Deviation minutes must be at least 1")
        Integer deviationMinutes,

        @NotNull(message = "Scheduled time is required")
        LocalTime scheduledTime,

        @NotNull(message = "Actual time is required")
        LocalDateTime actualTime,

        Integer gracePeriodMinutes,

        String penaltyAction,

        @Size(max = 500, message = "Remarks must not exceed 500 characters")
        String remarks
) {
}
