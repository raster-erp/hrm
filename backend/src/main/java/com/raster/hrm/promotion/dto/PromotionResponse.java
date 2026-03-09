package com.raster.hrm.promotion.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PromotionResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long oldDesignationId,
        String oldDesignationTitle,
        Long newDesignationId,
        String newDesignationTitle,
        String oldGrade,
        String newGrade,
        LocalDate effectiveDate,
        String status,
        String reason,
        Long approvedById,
        String approvedByName,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
