package com.raster.hrm.leavebalance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeaveTransactionResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long leaveTypeId,
        String leaveTypeName,
        String transactionType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String referenceType,
        Long referenceId,
        String description,
        String createdBy,
        LocalDateTime createdAt
) {
}
