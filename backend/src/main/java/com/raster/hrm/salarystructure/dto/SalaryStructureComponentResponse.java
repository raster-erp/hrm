package com.raster.hrm.salarystructure.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SalaryStructureComponentResponse(
        Long id,
        Long salaryComponentId,
        String salaryComponentCode,
        String salaryComponentName,
        String salaryComponentType,
        String computationType,
        BigDecimal percentageValue,
        BigDecimal fixedAmount,
        int sortOrder,
        LocalDateTime createdAt
) {
}
