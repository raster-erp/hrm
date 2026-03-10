package com.raster.hrm.attendance.dto;

import java.time.LocalDateTime;

public record AttendancePunchResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long deviceId,
        String deviceSerialNumber,
        String deviceName,
        LocalDateTime punchTime,
        String direction,
        String rawData,
        boolean normalized,
        String source,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
