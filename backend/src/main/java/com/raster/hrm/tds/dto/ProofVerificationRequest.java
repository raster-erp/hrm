package com.raster.hrm.tds.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProofVerificationRequest(
        @NotNull(message = "Item ID is required") Long itemId,
        @NotNull(message = "Verified amount is required") BigDecimal verifiedAmount,
        @NotNull(message = "Status is required") String status,
        @Size(max = 500) String remarks
) {}
