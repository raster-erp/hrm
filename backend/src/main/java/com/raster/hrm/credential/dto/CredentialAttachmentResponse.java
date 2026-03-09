package com.raster.hrm.credential.dto;

import java.time.LocalDateTime;

public record CredentialAttachmentResponse(
        Long id,
        Long credentialId,
        String fileName,
        String filePath,
        Long fileSize,
        String contentType,
        LocalDateTime uploadedAt
) {
}
