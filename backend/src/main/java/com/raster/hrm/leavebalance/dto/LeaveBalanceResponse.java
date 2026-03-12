package com.raster.hrm.leavebalance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeaveBalanceResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long leaveTypeId,
        String leaveTypeName,
        String leaveTypeCode,
        int year,
        BigDecimal credited,
        BigDecimal used,
        BigDecimal pending,
        BigDecimal available,
        BigDecimal carryForwarded,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
