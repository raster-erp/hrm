package com.raster.hrm.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayrollDetailResponse(
        Long id,
        Long payrollRunId,
        Long employeeId,
        String employeeName,
        String employeeCode,
        Long salaryStructureId,
        String salaryStructureName,
        BigDecimal basicSalary,
        BigDecimal grossSalary,
        BigDecimal totalDeductions,
        BigDecimal netSalary,
        String componentBreakup,
        int daysPayable,
        int lopDays,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
