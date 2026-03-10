package com.raster.hrm.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeviceRequest(
        @NotBlank(message = "Serial number is required")
        @Size(max = 100, message = "Serial number must not exceed 100 characters")
        String serialNumber,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotNull(message = "Type is required")
        String type,

        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,

        @Size(max = 45, message = "IP address must not exceed 45 characters")
        String ipAddress
) {
}
