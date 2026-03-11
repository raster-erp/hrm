package com.raster.hrm.wfh.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WfhRequestResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        LocalDate requestDate,
        String reason,
        String status,
        String approvedBy,
        LocalDateTime approvedAt,
        String remarks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
