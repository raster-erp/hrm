package com.raster.hrm.salarycomponent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SalaryComponentRequest(
        @NotBlank(message = "Code is required")
        @Size(max = 20, message = "Code must not exceed 20 characters")
        String code,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotNull(message = "Type is required")
        String type,

        @NotNull(message = "Computation type is required")
        String computationType,

        BigDecimal percentageValue,

        boolean taxable,

        boolean mandatory,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
