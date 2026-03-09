package com.raster.hrm.contract.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContractAmendmentResponse(
        Long id,
        Long contractId,
        LocalDate amendmentDate,
        String description,
        String oldTerms,
        String newTerms,
        LocalDateTime createdAt
) {
}
