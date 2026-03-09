package com.raster.hrm.separation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SeparationResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        String separationType,
        String reason,
        LocalDate noticeDate,
        LocalDate lastWorkingDay,
        String status,
        Long approvedById,
        String approvedByName,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
