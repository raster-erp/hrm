package com.raster.hrm.compoff.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CompOffCreditResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        LocalDate workedDate,
        String reason,
        LocalDate creditDate,
        LocalDate expiryDate,
        BigDecimal hoursWorked,
        String status,
        String approvedBy,
        LocalDateTime approvedAt,
        LocalDate usedDate,
        String remarks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
