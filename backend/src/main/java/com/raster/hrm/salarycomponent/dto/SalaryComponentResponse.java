package com.raster.hrm.salarycomponent.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SalaryComponentResponse(
        Long id,
        String code,
        String name,
        String type,
        String computationType,
        BigDecimal percentageValue,
        boolean taxable,
        boolean mandatory,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
