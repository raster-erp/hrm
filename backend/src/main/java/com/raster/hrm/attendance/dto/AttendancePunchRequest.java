package com.raster.hrm.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AttendancePunchRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Device ID is required")
        Long deviceId,

        @NotNull(message = "Punch time is required")
        LocalDateTime punchTime,

        @NotNull(message = "Direction is required")
        String direction,

        @Size(max = 500, message = "Raw data must not exceed 500 characters")
        String rawData
) {
}
