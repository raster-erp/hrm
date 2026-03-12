package com.raster.hrm.leaveplan.dto;

import com.raster.hrm.leaveplan.entity.LeavePlanStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeavePlanResponse(
    Long id,
    Long employeeId,
    String employeeCode,
    String employeeName,
    Long leaveTypeId,
    String leaveTypeName,
    LocalDate plannedFromDate,
    LocalDate plannedToDate,
    BigDecimal numberOfDays,
    String notes,
    LeavePlanStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
