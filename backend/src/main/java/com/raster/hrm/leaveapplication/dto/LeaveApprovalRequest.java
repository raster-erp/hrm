package com.raster.hrm.leaveapplication.dto;

import com.raster.hrm.leaveapplication.entity.LeaveApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeaveApprovalRequest(
        @NotNull(message = "Status is required")
        LeaveApplicationStatus status,

        String approvedBy,

        @Size(max = 500, message = "Remarks must not exceed 500 characters")
        String remarks
) {
}
