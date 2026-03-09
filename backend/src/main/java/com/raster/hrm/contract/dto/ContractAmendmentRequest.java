package com.raster.hrm.contract.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ContractAmendmentRequest(
        @NotNull(message = "Amendment date is required")
        LocalDate amendmentDate,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        String oldTerms,

        String newTerms
) {
}
