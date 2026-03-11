package com.raster.hrm.overtime.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record OvertimeRecordResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        LocalDate overtimeDate,
        Long overtimePolicyId,
        String overtimePolicyName,
        String overtimePolicyType,
        Integer overtimeMinutes,
        String status,
        String source,
        LocalTime shiftStartTime,
        LocalTime shiftEndTime,
        LocalDateTime actualStartTime,
        LocalDateTime actualEndTime,
        String remarks,
        String approvedBy,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
