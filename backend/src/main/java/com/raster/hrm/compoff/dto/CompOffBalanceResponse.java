package com.raster.hrm.compoff.dto;

public record CompOffBalanceResponse(
        Long employeeId,
        String employeeName,
        long totalCredits,
        long approved,
        long pending,
        long used,
        long expired,
        long availableForUse
) {
}
