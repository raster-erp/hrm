package com.raster.hrm.employeesalary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeSalaryDetailResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String employeeCode,
        Long salaryStructureId,
        String salaryStructureName,
        BigDecimal ctc,
        BigDecimal basicSalary,
        LocalDate effectiveDate,
        String notes,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
