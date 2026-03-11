package com.raster.hrm.leavepolicyassignment.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeavePolicyAssignmentRequest(
        @NotNull(message = "Leave policy ID is required")
        Long leavePolicyId,

        @NotNull(message = "Assignment type is required")
        String assignmentType,

        Long departmentId,

        Long designationId,

        Long employeeId,

        @NotNull(message = "Effective from date is required")
        LocalDate effectiveFrom,

        LocalDate effectiveTo
) {
}
