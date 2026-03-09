package com.raster.hrm.credential.controller;

import com.raster.hrm.credential.dto.CredentialAttachmentResponse;
import com.raster.hrm.credential.dto.CredentialRequest;
import com.raster.hrm.credential.dto.CredentialResponse;
import com.raster.hrm.credential.entity.VerificationStatus;
import com.raster.hrm.credential.service.CredentialService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/credentials")
public class CredentialController {

    private final CredentialService credentialService;

    public CredentialController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @GetMapping
    public ResponseEntity<Page<CredentialResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(credentialService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CredentialResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(credentialService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<CredentialResponse>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(credentialService.getByEmployeeId(employeeId));
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<CredentialResponse>> getExpiringCredentials(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(credentialService.getExpiringCredentials(days));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CredentialResponse>> getByStatus(@PathVariable VerificationStatus status) {
        return ResponseEntity.ok(credentialService.getByStatus(status));
    }

    @PostMapping
    public ResponseEntity<CredentialResponse> create(@Valid @RequestBody CredentialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(credentialService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CredentialResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody CredentialRequest request) {
        return ResponseEntity.ok(credentialService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CredentialResponse> updateVerificationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        var status = VerificationStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(credentialService.updateVerificationStatus(id, status));
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<CredentialAttachmentResponse>> getAttachments(@PathVariable Long id) {
        return ResponseEntity.ok(credentialService.getAttachments(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        credentialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
