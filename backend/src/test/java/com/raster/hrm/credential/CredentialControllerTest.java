package com.raster.hrm.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.credential.controller.CredentialController;
import com.raster.hrm.credential.dto.CredentialAttachmentResponse;
import com.raster.hrm.credential.dto.CredentialRequest;
import com.raster.hrm.credential.dto.CredentialResponse;
import com.raster.hrm.credential.entity.VerificationStatus;
import com.raster.hrm.credential.service.CredentialService;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CredentialController.class)
class CredentialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CredentialService credentialService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/credentials";

    private CredentialResponse createResponse(Long id, String type, String name) {
        return new CredentialResponse(
                id, 1L, "EMP001", "John Doe",
                type, name, "Test Issuer",
                LocalDate.of(2023, 1, 1), LocalDate.of(2025, 12, 31),
                "CRED-001", "PENDING", "Test notes",
                LocalDateTime.of(2023, 1, 15, 10, 0),
                LocalDateTime.of(2023, 1, 15, 10, 0)
        );
    }

    private CredentialRequest createRequest() {
        return new CredentialRequest(
                1L, "LICENSE", "Medical License",
                "Medical Board", LocalDate.of(2023, 1, 1),
                LocalDate.of(2025, 12, 31), "ML-001", "Valid license"
        );
    }

    @Test
    void getAll_shouldReturnPageOfCredentials() throws Exception {
        var credentials = List.of(
                createResponse(1L, "LICENSE", "Medical License"),
                createResponse(2L, "CERTIFICATION", "CPR Certification")
        );
        var page = new PageImpl<>(credentials, PageRequest.of(0, 20), 2);
        when(credentialService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].credentialName").value("Medical License"))
                .andExpect(jsonPath("$.content[1].credentialName").value("CPR Certification"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnCredential() throws Exception {
        var response = createResponse(1L, "LICENSE", "Medical License");
        when(credentialService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.credentialType").value("LICENSE"))
                .andExpect(jsonPath("$.credentialName").value("Medical License"))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(credentialService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Credential", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturnCredentials() throws Exception {
        var credentials = List.of(
                createResponse(1L, "LICENSE", "Medical License"),
                createResponse(2L, "CERTIFICATION", "CPR Certification")
        );
        when(credentialService.getByEmployeeId(1L)).thenReturn(credentials);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].credentialName").value("Medical License"));
    }

    @Test
    void getByEmployee_shouldReturn404WhenEmployeeNotFound() throws Exception {
        when(credentialService.getByEmployeeId(999L))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/employee/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExpiringCredentials_shouldReturnCredentials() throws Exception {
        var credentials = List.of(createResponse(1L, "LICENSE", "Expiring License"));
        when(credentialService.getExpiringCredentials(30)).thenReturn(credentials);

        mockMvc.perform(get(BASE_URL + "/expiring").param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].credentialName").value("Expiring License"));
    }

    @Test
    void getExpiringCredentials_shouldUseDefaultDays() throws Exception {
        var credentials = List.of(createResponse(1L, "LICENSE", "Expiring License"));
        when(credentialService.getExpiringCredentials(30)).thenReturn(credentials);

        mockMvc.perform(get(BASE_URL + "/expiring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getExpiringCredentials_shouldReturn400WhenNegativeDays() throws Exception {
        when(credentialService.getExpiringCredentials(-1))
                .thenThrow(new BadRequestException("Days parameter must be a non-negative number"));

        mockMvc.perform(get(BASE_URL + "/expiring").param("days", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByStatus_shouldReturnCredentials() throws Exception {
        var credentials = List.of(createResponse(1L, "LICENSE", "Verified License"));
        when(credentialService.getByStatus(VerificationStatus.VERIFIED)).thenReturn(credentials);

        mockMvc.perform(get(BASE_URL + "/status/VERIFIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].credentialName").value("Verified License"));
    }

    @Test
    void getByStatus_shouldReturnPendingCredentials() throws Exception {
        var credentials = List.of(createResponse(1L, "LICENSE", "Pending License"));
        when(credentialService.getByStatus(VerificationStatus.PENDING)).thenReturn(credentials);

        mockMvc.perform(get(BASE_URL + "/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void create_shouldReturn201WithCreatedCredential() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "LICENSE", "Medical License");
        when(credentialService.create(any(CredentialRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.credentialType").value("LICENSE"))
                .andExpect(jsonPath("$.credentialName").value("Medical License"));
    }

    @Test
    void create_shouldReturn400WhenCredentialTypeBlank() throws Exception {
        var request = new CredentialRequest(1L, "", "Medical License",
                null, null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenCredentialNameBlank() throws Exception {
        var request = new CredentialRequest(1L, "LICENSE", "",
                null, null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenEmployeeIdNull() throws Exception {
        var request = new CredentialRequest(null, "LICENSE", "Medical License",
                null, null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn404WhenEmployeeNotFound() throws Exception {
        var request = createRequest();
        when(credentialService.create(any(CredentialRequest.class)))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 1L));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturnUpdatedCredential() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "LICENSE", "Medical License");
        when(credentialService.update(eq(1L), any(CredentialRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.credentialName").value("Medical License"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(credentialService.update(eq(999L), any(CredentialRequest.class)))
                .thenThrow(new ResourceNotFoundException("Credential", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn400WhenValidationFails() throws Exception {
        var request = new CredentialRequest(null, "", "",
                null, null, null, null, null);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateVerificationStatus_shouldReturnUpdatedCredential() throws Exception {
        var response = new CredentialResponse(
                1L, 1L, "EMP001", "John Doe",
                "LICENSE", "Medical License", "Medical Board",
                LocalDate.of(2023, 1, 1), LocalDate.of(2025, 12, 31),
                "ML-001", "VERIFIED", null,
                LocalDateTime.of(2023, 1, 15, 10, 0),
                LocalDateTime.of(2023, 1, 15, 10, 0)
        );
        when(credentialService.updateVerificationStatus(1L, VerificationStatus.VERIFIED))
                .thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "VERIFIED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
    }

    @Test
    void updateVerificationStatus_shouldReturn404WhenNotFound() throws Exception {
        when(credentialService.updateVerificationStatus(999L, VerificationStatus.VERIFIED))
                .thenThrow(new ResourceNotFoundException("Credential", "id", 999L));

        mockMvc.perform(patch(BASE_URL + "/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "VERIFIED"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAttachments_shouldReturnListOfAttachments() throws Exception {
        var attachments = List.of(
                new CredentialAttachmentResponse(
                        1L, 1L, "license.pdf", "/uploads/license.pdf",
                        1024L, "application/pdf",
                        LocalDateTime.of(2023, 6, 1, 10, 0)
                ),
                new CredentialAttachmentResponse(
                        2L, 1L, "cert.jpg", "/uploads/cert.jpg",
                        2048L, "image/jpeg",
                        LocalDateTime.of(2023, 6, 1, 10, 0)
                )
        );
        when(credentialService.getAttachments(1L)).thenReturn(attachments);

        mockMvc.perform(get(BASE_URL + "/1/attachments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fileName").value("license.pdf"))
                .andExpect(jsonPath("$[0].contentType").value("application/pdf"))
                .andExpect(jsonPath("$[1].fileName").value("cert.jpg"));
    }

    @Test
    void getAttachments_shouldReturn404WhenCredentialNotFound() throws Exception {
        when(credentialService.getAttachments(999L))
                .thenThrow(new ResourceNotFoundException("Credential", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999/attachments"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(credentialService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(credentialService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Credential", "id", 999L))
                .when(credentialService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
