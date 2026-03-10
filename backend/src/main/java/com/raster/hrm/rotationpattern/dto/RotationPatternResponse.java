package com.raster.hrm.rotationpattern.dto;

import java.time.LocalDateTime;

public record RotationPatternResponse(
        Long id,
        String name,
        String description,
        Integer rotationDays,
        String shiftSequence,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
