package com.raster.hrm.attendancereport.dto;

import java.time.LocalDate;
import java.util.List;

public record AbsenteeListReport(
        LocalDate startDate,
        LocalDate endDate,
        Long departmentId,
        String departmentName,
        List<AbsenteeEntry> entries,
        int totalAbsentInstances
) {
}
