package com.raster.hrm.leaveapplication.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveApplicationResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long leaveTypeId,
        String leaveTypeName,
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal numberOfDays,
        String reason,
        String status,
        int approvalLevel,
        String remarks,
        String approvedBy,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
