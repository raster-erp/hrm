package com.raster.hrm.leaveanalytics.dto;

import java.math.BigDecimal;

public record AbsenteeismRateEntry(
        Long departmentId,
        String departmentName,
        int employeeCount,
        BigDecimal totalLeaveDays,
        int totalWorkingDays,
        BigDecimal absenteeismRate
) {
}
