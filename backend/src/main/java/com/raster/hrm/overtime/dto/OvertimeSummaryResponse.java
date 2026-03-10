package com.raster.hrm.overtime.dto;

import java.math.BigDecimal;

public record OvertimeSummaryResponse(
        Long employeeId,
        String employeeCode,
        String employeeName,
        Integer totalOvertimeMinutes,
        Integer approvedOvertimeMinutes,
        Integer pendingOvertimeMinutes,
        Integer rejectedOvertimeMinutes,
        BigDecimal weightedOvertimeMinutes,
        Integer recordCount
) {
}
