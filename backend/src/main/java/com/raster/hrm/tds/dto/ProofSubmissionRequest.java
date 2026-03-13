package com.raster.hrm.tds.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProofSubmissionRequest(
        @NotNull(message = "Item ID is required") Long itemId,
        @Size(max = 200) String proofDocumentName,
        BigDecimal declaredAmount
) {}
