package com.raster.hrm.attendancereport.dto;

import java.time.LocalDate;

public record AbsenteeEntry(
        Long employeeId,
        String employeeCode,
        String employeeName,
        String departmentName,
        LocalDate absentDate
) {
}
