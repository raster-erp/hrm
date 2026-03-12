package com.raster.hrm.leaveencashment.dto;

import java.math.BigDecimal;

public record EncashmentEligibilityResponse(
        Long employeeId,
        String employeeName,
        Long leaveTypeId,
        String leaveTypeName,
        int year,
        boolean eligible,
        BigDecimal availableBalance,
        BigDecimal minRequiredBalance,
        BigDecimal maxEncashableDays,
        BigDecimal perDaySalary,
        String reason
) {
}
