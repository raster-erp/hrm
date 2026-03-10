package com.raster.hrm.device.dto;

import java.time.LocalDateTime;

public record DeviceResponse(
        Long id,
        String serialNumber,
        String name,
        String type,
        String location,
        String ipAddress,
        String status,
        LocalDateTime lastSyncAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
