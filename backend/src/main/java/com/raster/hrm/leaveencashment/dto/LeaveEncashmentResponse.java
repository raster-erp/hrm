package com.raster.hrm.leaveencashment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeaveEncashmentResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long leaveTypeId,
        String leaveTypeName,
        int year,
        BigDecimal numberOfDays,
        BigDecimal perDaySalary,
        BigDecimal totalAmount,
        String status,
        String approvedBy,
        LocalDateTime approvedAt,
        String remarks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
