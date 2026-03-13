package com.raster.hrm.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayrollAdjustmentResponse(
        Long id,
        Long payrollRunId,
        Long employeeId,
        String employeeName,
        String employeeCode,
        String adjustmentType,
        String componentName,
        BigDecimal amount,
        String reason,
        LocalDateTime createdAt
) {}
