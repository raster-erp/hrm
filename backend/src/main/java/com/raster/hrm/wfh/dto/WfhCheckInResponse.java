package com.raster.hrm.wfh.dto;

import java.time.LocalDateTime;

public record WfhCheckInResponse(
        Long id,
        Long wfhRequestId,
        Long employeeId,
        String employeeCode,
        String employeeName,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime,
        String ipAddress,
        String location,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
