package com.raster.hrm.uniform.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UniformAllocationResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long uniformId,
        String uniformName,
        String uniformType,
        String uniformSize,
        LocalDate allocatedDate,
        LocalDate returnedDate,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
