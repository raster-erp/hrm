package com.raster.hrm.attendancereport.dto;

import java.util.List;

public record MonthlySummaryReport(
        int year,
        int month,
        Long departmentId,
        String departmentName,
        List<MonthlySummaryEntry> entries
) {
}
