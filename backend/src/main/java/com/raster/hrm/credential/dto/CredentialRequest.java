package com.raster.hrm.credential.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CredentialRequest(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotBlank(message = "Credential type is required")
        @Size(max = 50, message = "Credential type must not exceed 50 characters")
        String credentialType,

        @NotBlank(message = "Credential name is required")
        @Size(max = 100, message = "Credential name must not exceed 100 characters")
        String credentialName,

        @Size(max = 200, message = "Issuer must not exceed 200 characters")
        String issuer,

        LocalDate issueDate,

        LocalDate expiryDate,

        @Size(max = 100, message = "Credential number must not exceed 100 characters")
        String credentialNumber,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}
