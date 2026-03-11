package com.raster.hrm.overtime.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OvertimeApprovalRequest(
        @NotBlank(message = "Status is required (APPROVED or REJECTED)")
        String status,

        @NotBlank(message = "Approved by is required")
        @Size(max = 100, message = "Approved by must not exceed 100 characters")
        String approvedBy,

        @Size(max = 500, message = "Remarks must not exceed 500 characters")
        String remarks
) {
}
