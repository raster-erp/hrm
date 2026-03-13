package com.raster.hrm.tds.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProfessionalTaxSlabResponse(
        Long id, String state,
        BigDecimal slabFrom, BigDecimal slabTo,
        BigDecimal monthlyTax, BigDecimal februaryTax,
        boolean active,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {}
