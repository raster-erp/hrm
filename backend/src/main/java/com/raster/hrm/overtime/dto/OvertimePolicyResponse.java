package com.raster.hrm.overtime.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OvertimePolicyResponse(
        Long id,
        String name,
        String type,
        BigDecimal rateMultiplier,
        Integer minOvertimeMinutes,
        Integer maxOvertimeMinutesPerDay,
        Integer maxOvertimeMinutesPerMonth,
        boolean requiresApproval,
        boolean active,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
