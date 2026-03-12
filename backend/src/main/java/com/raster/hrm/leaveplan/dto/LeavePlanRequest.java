package com.raster.hrm.leaveplan.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LeavePlanRequest(
    @NotNull Long employeeId,
    @NotNull Long leaveTypeId,
    @NotNull LocalDate plannedFromDate,
    @NotNull LocalDate plannedToDate,
    @NotNull BigDecimal numberOfDays,
    @Size(max = 500) String notes
) {}
