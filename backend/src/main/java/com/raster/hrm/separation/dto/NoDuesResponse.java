package com.raster.hrm.separation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NoDuesResponse(
        Long id,
        Long separationId,
        String department,
        boolean cleared,
        String clearedBy,
        LocalDateTime clearedAt,
        BigDecimal amountDue,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
