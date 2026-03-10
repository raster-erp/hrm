package com.raster.hrm.attendance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PunchSyncRequest(
        @NotNull(message = "Device ID is required")
        Long deviceId,

        @NotEmpty(message = "Punches list must not be empty")
        List<@Valid PunchData> punches
) {

    public record PunchData(
            @NotNull(message = "Employee ID is required")
            Long employeeId,

            @NotNull(message = "Punch time is required")
            String punchTime,

            @NotNull(message = "Direction is required")
            String direction,

            String rawData
    ) {
    }
}
