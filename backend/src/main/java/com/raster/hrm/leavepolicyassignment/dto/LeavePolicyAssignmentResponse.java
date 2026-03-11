package com.raster.hrm.leavepolicyassignment.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeavePolicyAssignmentResponse(
        Long id,
        Long leavePolicyId,
        String leavePolicyName,
        String assignmentType,
        Long departmentId,
        Long designationId,
        Long employeeId,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
