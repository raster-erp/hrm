package com.raster.hrm.promotion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PromotionRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        Long oldDesignationId,

        Long newDesignationId,

        @Size(max = 20, message = "Old grade must not exceed 20 characters")
        String oldGrade,

        @Size(max = 20, message = "New grade must not exceed 20 characters")
        String newGrade,

        @NotNull(message = "Effective date is required")
        LocalDate effectiveDate,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
