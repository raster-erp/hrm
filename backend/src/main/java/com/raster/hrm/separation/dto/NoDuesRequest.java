package com.raster.hrm.separation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record NoDuesRequest(
        @NotNull(message = "Separation ID is required")
        Long separationId,

        @NotBlank(message = "Department is required")
        @Size(max = 100, message = "Department must not exceed 100 characters")
        String department,

        BigDecimal amountDue,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}
