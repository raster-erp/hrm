package com.raster.hrm.shift.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record ShiftResponse(
        Long id,
        String name,
        String type,
        LocalTime startTime,
        LocalTime endTime,
        Integer breakDurationMinutes,
        Integer gracePeriodMinutes,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
