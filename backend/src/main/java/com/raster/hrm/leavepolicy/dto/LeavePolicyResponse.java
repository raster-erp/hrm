package com.raster.hrm.leavepolicy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeavePolicyResponse(
        Long id,
        String name,
        Long leaveTypeId,
        String leaveTypeName,
        String leaveTypeCode,
        String accrualFrequency,
        BigDecimal accrualDays,
        BigDecimal maxAccumulation,
        BigDecimal carryForwardLimit,
        boolean proRataForNewJoiners,
        int minServiceDaysRequired,
        boolean active,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
