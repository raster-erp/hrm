package com.raster.hrm.uniform.dto;

import java.time.LocalDateTime;

public record UniformResponse(
        Long id,
        String name,
        String type,
        String size,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
