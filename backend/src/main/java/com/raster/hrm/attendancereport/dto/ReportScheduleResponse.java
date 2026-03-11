package com.raster.hrm.attendancereport.dto;

import java.time.LocalDateTime;

public record ReportScheduleResponse(
        Long id,
        String reportName,
        String reportType,
        String frequency,
        Long departmentId,
        String departmentName,
        String recipients,
        String exportFormat,
        boolean active,
        LocalDateTime lastRunAt,
        LocalDateTime nextRunAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
