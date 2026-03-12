package com.raster.hrm.leaveanalytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record LeaveUtilizationReport(
        int year,
        Long departmentId,
        String departmentName,
        BigDecimal overallUtilization,
        List<LeaveUtilizationEntry> entries
) {
}
