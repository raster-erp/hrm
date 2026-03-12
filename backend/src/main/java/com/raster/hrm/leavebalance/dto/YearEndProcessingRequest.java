package com.raster.hrm.leavebalance.dto;

import jakarta.validation.constraints.NotNull;

public record YearEndProcessingRequest(
        @NotNull(message = "Year is required")
        Integer year,

        String processedBy
) {
}
