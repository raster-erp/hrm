package com.raster.hrm.credential.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CredentialResponse(
        Long id,
        Long employeeId,
        String employeeCode,
        String employeeName,
        String credentialType,
        String credentialName,
        String issuer,
        LocalDate issueDate,
        LocalDate expiryDate,
        String credentialNumber,
        String verificationStatus,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
