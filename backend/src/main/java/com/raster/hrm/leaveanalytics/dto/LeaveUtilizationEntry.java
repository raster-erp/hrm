package com.raster.hrm.leaveanalytics.dto;

import java.math.BigDecimal;

public record LeaveUtilizationEntry(
        Long employeeId,
        String employeeCode,
        String employeeName,
        String departmentName,
        String leaveTypeName,
        BigDecimal entitled,
        BigDecimal used,
        BigDecimal available,
        BigDecimal utilizationPercent
) {
}
