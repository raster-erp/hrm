package com.raster.hrm.rotationpattern.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RotationPatternRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Rotation days is required")
        @Min(value = 1, message = "Rotation days must be at least 1")
        Integer rotationDays,

        @NotBlank(message = "Shift sequence is required")
        @Size(max = 1000, message = "Shift sequence must not exceed 1000 characters")
        String shiftSequence
) {
}
