package com.raster.hrm.uniform.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UniformAllocationRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Uniform ID is required")
        Long uniformId,

        @NotNull(message = "Allocated date is required")
        LocalDate allocatedDate
) {
}
