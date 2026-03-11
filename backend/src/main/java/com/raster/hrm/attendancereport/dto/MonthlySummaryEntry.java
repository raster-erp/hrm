package com.raster.hrm.attendancereport.dto;

public record MonthlySummaryEntry(
        Long employeeId,
        String employeeCode,
        String employeeName,
        String departmentName,
        int totalPresent,
        int totalAbsent,
        int totalIncomplete,
        int totalWorkingDays
) {
}
