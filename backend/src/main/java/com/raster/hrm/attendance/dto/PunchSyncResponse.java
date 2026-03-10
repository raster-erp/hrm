package com.raster.hrm.attendance.dto;

import java.util.List;

public record PunchSyncResponse(
        int totalReceived,
        int accepted,
        int duplicatesSkipped,
        List<Long> acceptedPunchIds
) {
}
