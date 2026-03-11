package com.raster.hrm.attendancedeviation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AttendanceDeviationResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        LocalDate deviationDate,
        String type,
        Integer deviationMinutes,
        LocalTime scheduledTime,
        LocalDateTime actualTime,
        Integer gracePeriodMinutes,
        String penaltyAction,
        String status,
        String remarks,
        String approvedBy,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
