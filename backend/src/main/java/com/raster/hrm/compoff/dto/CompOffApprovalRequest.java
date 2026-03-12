package com.raster.hrm.compoff.dto;

import com.raster.hrm.compoff.entity.CompOffStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompOffApprovalRequest(
        @NotNull CompOffStatus status,
        @NotNull String approvedBy,
        @Size(max = 500) String remarks
) {
}
