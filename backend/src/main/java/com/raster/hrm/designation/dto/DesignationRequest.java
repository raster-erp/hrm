package com.raster.hrm.designation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DesignationRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must not exceed 100 characters")
        String title,

        @NotBlank(message = "Code is required")
        @Size(max = 20, message = "Code must not exceed 20 characters")
        String code,

        Integer level,

        @Size(max = 20, message = "Grade must not exceed 20 characters")
        String grade,

        Long departmentId,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        Boolean active
) {
}
