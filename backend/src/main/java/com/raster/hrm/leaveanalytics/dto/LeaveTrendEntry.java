package com.raster.hrm.leaveanalytics.dto;

import java.math.BigDecimal;

public record LeaveTrendEntry(
        int year,
        int month,
        String leaveTypeName,
        long applicationCount,
        BigDecimal totalDays
) {
}
