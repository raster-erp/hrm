package com.raster.hrm.overtime.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record OvertimeRecordRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Overtime date is required")
        LocalDate overtimeDate,

        @NotNull(message = "Overtime policy ID is required")
        Long overtimePolicyId,

        @NotNull(message = "Overtime minutes is required")
        @Min(value = 1, message = "Overtime minutes must be at least 1")
        Integer overtimeMinutes,

        LocalTime shiftStartTime,

        LocalTime shiftEndTime,

        LocalDateTime actualStartTime,

        LocalDateTime actualEndTime,

        @Size(max = 500, message = "Remarks must not exceed 500 characters")
        String remarks
) {
}
