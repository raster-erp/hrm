package com.raster.hrm.compoff.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompOffCreditRequest(
        @NotNull Long employeeId,
        @NotNull LocalDate workedDate,
        @NotNull @Size(max = 255) String reason,
        BigDecimal hoursWorked,
        String remarks
) {
}
