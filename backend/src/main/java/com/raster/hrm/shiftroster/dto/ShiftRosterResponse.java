package com.raster.hrm.shiftroster.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShiftRosterResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String employeeCode,
        Long shiftId,
        String shiftName,
        LocalDate effectiveDate,
        LocalDate endDate,
        Long rotationPatternId,
        String rotationPatternName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
