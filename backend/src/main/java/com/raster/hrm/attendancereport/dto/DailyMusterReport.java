package com.raster.hrm.attendancereport.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyMusterReport(
        LocalDate date,
        Long departmentId,
        String departmentName,
        List<DailyMusterEntry> entries,
        int totalPresent,
        int totalAbsent,
        int totalIncomplete
) {
}
