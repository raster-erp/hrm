package com.raster.hrm.tds.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaxSlabResponse(
        Long id, String regime, String financialYear,
        BigDecimal slabFrom, BigDecimal slabTo, BigDecimal rate,
        String description, boolean active,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {}
