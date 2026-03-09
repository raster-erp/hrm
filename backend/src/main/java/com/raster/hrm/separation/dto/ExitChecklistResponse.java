package com.raster.hrm.separation.dto;

import java.time.LocalDateTime;

public record ExitChecklistResponse(
        Long id,
        Long separationId,
        String itemName,
        String department,
        boolean cleared,
        String clearedBy,
        LocalDateTime clearedAt,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
