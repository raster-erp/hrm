package com.raster.hrm.leavetype.dto;

import java.time.LocalDateTime;

public record LeaveTypeResponse(
        Long id,
        String code,
        String name,
        String category,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
