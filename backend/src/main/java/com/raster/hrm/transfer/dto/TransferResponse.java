package com.raster.hrm.transfer.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransferResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long fromDepartmentId,
        String fromDepartmentName,
        Long toDepartmentId,
        String toDepartmentName,
        String fromBranch,
        String toBranch,
        String transferType,
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
