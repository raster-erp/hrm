package com.raster.hrm.attendanceregularization.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegularizationRequestResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        LocalDate requestDate,
        String type,
        String reason,
        LocalDateTime originalPunchIn,
        LocalDateTime originalPunchOut,
        LocalDateTime correctedPunchIn,
        LocalDateTime correctedPunchOut,
        String status,
        Integer approvalLevel,
        String remarks,
        String approvedBy,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
