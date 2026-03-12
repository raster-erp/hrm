package com.raster.hrm.leaveencashment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LeaveEncashmentRequest(
        @NotNull Long employeeId,
        @NotNull Long leaveTypeId,
        @NotNull @Positive BigDecimal numberOfDays,
        String remarks
) {
}
