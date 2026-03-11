package com.raster.hrm.leavepolicyassignment.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeavePolicyAssignmentResponse(
        Long id,
        Long leavePolicyId,
        String leavePolicyName,
        String assignmentType,
        Long departmentId,
        String departmentName,
        Long designationId,
        String designationTitle,
        Long employeeId,
        String employeeName,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
