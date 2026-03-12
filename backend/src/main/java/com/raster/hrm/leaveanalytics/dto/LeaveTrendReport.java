package com.raster.hrm.leaveanalytics.dto;

import java.util.List;

public record LeaveTrendReport(
        int startYear,
        int startMonth,
        int endYear,
        int endMonth,
        Long departmentId,
        String departmentName,
        List<LeaveTrendEntry> entries
) {
}
