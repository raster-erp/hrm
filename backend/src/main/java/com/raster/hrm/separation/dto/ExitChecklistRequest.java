package com.raster.hrm.separation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExitChecklistRequest(
        @NotNull(message = "Separation ID is required")
        Long separationId,

        @NotBlank(message = "Item name is required")
        @Size(max = 200, message = "Item name must not exceed 200 characters")
        String itemName,

        @NotBlank(message = "Department is required")
        @Size(max = 100, message = "Department must not exceed 100 characters")
        String department,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}
