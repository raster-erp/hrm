package com.raster.hrm.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ContractRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotBlank(message = "Contract type is required")
        @Size(max = 20, message = "Contract type must not exceed 20 characters")
        String contractType,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate,

        String terms,

        @Size(max = 20, message = "Status must not exceed 20 characters")
        String status
) {
}
