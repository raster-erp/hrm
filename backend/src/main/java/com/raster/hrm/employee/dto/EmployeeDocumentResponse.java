package com.raster.hrm.employee.dto;

import java.time.LocalDateTime;

public record EmployeeDocumentResponse(
        Long id,
        Long employeeId,
        String documentType,
        String documentName,
        String filePath,
        Long fileSize,
        String contentType,
        LocalDateTime uploadedAt
) {
}
