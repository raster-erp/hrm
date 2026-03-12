package com.raster.hrm.salarystructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SalaryStructureCloneRequest(
        @NotBlank(message = "New code is required")
        @Size(max = 20, message = "Code must not exceed 20 characters")
        String newCode,

        @NotBlank(message = "New name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String newName
) {
}
