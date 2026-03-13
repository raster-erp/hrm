package com.raster.hrm.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PayrollRunResponse(
        Long id,
        int periodYear,
        int periodMonth,
        LocalDate runDate,
        String status,
        BigDecimal totalGross,
        BigDecimal totalDeductions,
        BigDecimal totalNet,
        int employeeCount,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
