package com.raster.hrm.wfh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record WfhRequestCreateRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Request date is required")
        LocalDate requestDate,

        @NotBlank(message = "Reason is required")
        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason,

        @Size(max = 500, message = "Remarks must not exceed 500 characters")
        String remarks
) {
}
