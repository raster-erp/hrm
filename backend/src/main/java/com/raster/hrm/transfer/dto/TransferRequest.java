package com.raster.hrm.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TransferRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        Long fromDepartmentId,

        Long toDepartmentId,

        @Size(max = 100, message = "From branch must not exceed 100 characters")
        String fromBranch,

        @Size(max = 100, message = "To branch must not exceed 100 characters")
        String toBranch,

        @NotBlank(message = "Transfer type is required")
        @Size(max = 30, message = "Transfer type must not exceed 30 characters")
        String transferType,

        @NotNull(message = "Effective date is required")
        LocalDate effectiveDate,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
