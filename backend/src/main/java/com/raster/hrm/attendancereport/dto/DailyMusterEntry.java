package com.raster.hrm.attendancereport.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailyMusterEntry(
        Long employeeId,
        String employeeCode,
        String employeeName,
        String departmentName,
        LocalDate date,
        LocalDateTime firstPunchIn,
        LocalDateTime lastPunchOut,
        int totalPunches,
        String status
) {
}
