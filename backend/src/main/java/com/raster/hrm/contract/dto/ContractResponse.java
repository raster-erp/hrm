package com.raster.hrm.contract.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContractResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        String contractType,
        LocalDate startDate,
        LocalDate endDate,
        String terms,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
