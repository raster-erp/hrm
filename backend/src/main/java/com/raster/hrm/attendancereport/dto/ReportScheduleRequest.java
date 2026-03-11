package com.raster.hrm.attendancereport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportScheduleRequest(
        @NotBlank(message = "Report name is required")
        @Size(max = 100, message = "Report name must not exceed 100 characters")
        String reportName,

        @NotNull(message = "Report type is required")
        String reportType,

        @NotNull(message = "Frequency is required")
        String frequency,

        Long departmentId,

        @Size(max = 500, message = "Recipients must not exceed 500 characters")
        String recipients,

        String exportFormat
) {
}
