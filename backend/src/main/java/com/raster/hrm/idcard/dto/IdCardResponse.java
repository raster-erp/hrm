package com.raster.hrm.idcard.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record IdCardResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        String cardNumber,
        LocalDate issueDate,
        LocalDate expiryDate,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
