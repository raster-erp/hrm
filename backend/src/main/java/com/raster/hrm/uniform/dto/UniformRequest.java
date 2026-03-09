package com.raster.hrm.uniform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UniformRequest(
        @NotBlank(message = "Uniform name is required")
        @Size(max = 100, message = "Uniform name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Uniform type is required")
        @Size(max = 50, message = "Uniform type must not exceed 50 characters")
        String type,

        @Size(max = 20, message = "Size must not exceed 20 characters")
        String size,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
