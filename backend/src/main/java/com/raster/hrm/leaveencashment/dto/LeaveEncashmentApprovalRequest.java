package com.raster.hrm.leaveencashment.dto;

import com.raster.hrm.leaveencashment.entity.EncashmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeaveEncashmentApprovalRequest(
        @NotNull EncashmentStatus status,
        String approvedBy,
        @Size(max = 500) String remarks
) {
}
