package com.raster.hrm.wfh.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WfhCheckInRequest(
        @NotNull(message = "WFH request ID is required")
        Long wfhRequestId,

        @Size(max = 45, message = "IP address must not exceed 45 characters")
        String ipAddress,

        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location
) {
}
