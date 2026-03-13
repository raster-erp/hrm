package com.raster.hrm.tds.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvestmentDeclarationItemResponse(
        Long id, String section, String description,
        BigDecimal declaredAmount, BigDecimal verifiedAmount,
        String proofStatus, String proofDocumentName, String proofRemarks,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {}
