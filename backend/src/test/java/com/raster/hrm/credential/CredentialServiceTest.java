package com.raster.hrm.credential;

import com.raster.hrm.credential.dto.CredentialAttachmentResponse;
import com.raster.hrm.credential.dto.CredentialRequest;
import com.raster.hrm.credential.dto.CredentialResponse;
import com.raster.hrm.credential.entity.Credential;
import com.raster.hrm.credential.entity.CredentialAttachment;
import com.raster.hrm.credential.entity.VerificationStatus;
import com.raster.hrm.credential.repository.CredentialAttachmentRepository;
import com.raster.hrm.credential.repository.CredentialRepository;
import com.raster.hrm.credential.service.CredentialService;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private CredentialAttachmentRepository credentialAttachmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CredentialService credentialService;

    private Employee createEmployee(Long id, String code, String firstName, String lastName) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "@test.com");
        return employee;
    }

    private Credential createCredential(Long id, Employee employee, String type, String name) {
        var credential = new Credential();
        credential.setId(id);
        credential.setEmployee(employee);
        credential.setCredentialType(type);
        credential.setCredentialName(name);
        credential.setIssuer("Test Issuer");
        credential.setIssueDate(LocalDate.of(2023, 1, 1));
        credential.setExpiryDate(LocalDate.of(2025, 12, 31));
        credential.setCredentialNumber("CRED-" + id);
        credential.setVerificationStatus(VerificationStatus.PENDING);
        credential.setNotes("Test notes");
        credential.setCreatedAt(LocalDateTime.of(2023, 1, 15, 10, 0));
        credential.setUpdatedAt(LocalDateTime.of(2023, 1, 15, 10, 0));
        return credential;
    }

    private CredentialRequest createRequest() {
        return new CredentialRequest(
                1L, "LICENSE", "Medical License",
                "Medical Board", LocalDate.of(2023, 1, 1),
                LocalDate.of(2025, 12, 31), "ML-001", "Valid license"
        );
    }

    @Test
    void getAll_shouldReturnPageOfCredentials() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credentials = List.of(
                createCredential(1L, employee, "LICENSE", "Medical License"),
                createCredential(2L, employee, "CERTIFICATION", "CPR Cert")
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(credentials, pageable, 2);
        when(credentialRepository.findAll(pageable)).thenReturn(page);

        var result = credentialService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Medical License", result.getContent().get(0).credentialName());
        assertEquals("CPR Cert", result.getContent().get(1).credentialName());
        verify(credentialRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<Credential>(List.of(), pageable, 0);
        when(credentialRepository.findAll(pageable)).thenReturn(page);

        var result = credentialService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnCredential() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credential = createCredential(1L, employee, "LICENSE", "Medical License");
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credential));

        var result = credentialService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("LICENSE", result.credentialType());
        assertEquals("Medical License", result.credentialName());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("PENDING", result.verificationStatus());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(credentialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> credentialService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnCredentials() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credentials = List.of(
                createCredential(1L, employee, "LICENSE", "Medical License"),
                createCredential(2L, employee, "CERTIFICATION", "CPR Cert")
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(credentialRepository.findByEmployeeId(1L)).thenReturn(credentials);

        var result = credentialService.getByEmployeeId(1L);

        assertEquals(2, result.size());
        assertEquals("Medical License", result.get(0).credentialName());
        assertEquals("CPR Cert", result.get(1).credentialName());
    }

    @Test
    void getByEmployeeId_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> credentialService.getByEmployeeId(999L));
        verify(credentialRepository, never()).findByEmployeeId(any());
    }

    @Test
    void getByEmployeeId_shouldReturnEmptyListWhenNoCredentials() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(credentialRepository.findByEmployeeId(1L)).thenReturn(List.of());

        var result = credentialService.getByEmployeeId(1L);

        assertEquals(0, result.size());
    }

    @Test
    void getExpiringCredentials_shouldReturnCredentials() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credential = createCredential(1L, employee, "LICENSE", "Expiring License");
        when(credentialRepository.findByExpiryDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(credential));

        var result = credentialService.getExpiringCredentials(30);

        assertEquals(1, result.size());
        assertEquals("Expiring License", result.get(0).credentialName());
    }

    @Test
    void getExpiringCredentials_shouldReturnEmptyListWhenNoneExpiring() {
        when(credentialRepository.findByExpiryDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        var result = credentialService.getExpiringCredentials(30);

        assertEquals(0, result.size());
    }

    @Test
    void getExpiringCredentials_shouldThrowWhenNegativeDays() {
        assertThrows(BadRequestException.class,
                () -> credentialService.getExpiringCredentials(-1));
    }

    @Test
    void getByStatus_shouldReturnCredentials() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credential = createCredential(1L, employee, "LICENSE", "Verified License");
        credential.setVerificationStatus(VerificationStatus.VERIFIED);
        when(credentialRepository.findByVerificationStatus(VerificationStatus.VERIFIED))
                .thenReturn(List.of(credential));

        var result = credentialService.getByStatus(VerificationStatus.VERIFIED);

        assertEquals(1, result.size());
        assertEquals("VERIFIED", result.get(0).verificationStatus());
    }

    @Test
    void getByStatus_shouldReturnEmptyListWhenNoneMatch() {
        when(credentialRepository.findByVerificationStatus(VerificationStatus.EXPIRED))
                .thenReturn(List.of());

        var result = credentialService.getByStatus(VerificationStatus.EXPIRED);

        assertEquals(0, result.size());
    }

    @Test
    void create_shouldCreateAndReturnCredential() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(credentialRepository.save(any(Credential.class))).thenAnswer(invocation -> {
            Credential c = invocation.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        var result = credentialService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("LICENSE", result.credentialType());
        assertEquals("Medical License", result.credentialName());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        verify(credentialRepository).save(any(Credential.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> credentialService.create(request));
        verify(credentialRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateAndReturnCredential() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credential = createCredential(1L, employee, "CERTIFICATION", "Old Cert");
        var request = createRequest();
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credential));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(credentialRepository.save(any(Credential.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = credentialService.update(1L, request);

        assertNotNull(result);
        assertEquals("LICENSE", result.credentialType());
        assertEquals("Medical License", result.credentialName());
        verify(credentialRepository).save(any(Credential.class));
    }

    @Test
    void update_shouldThrowWhenCredentialNotFound() {
        var request = createRequest();
        when(credentialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> credentialService.update(999L, request));
        verify(credentialRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenEmployeeNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credential = createCredential(1L, employee, "LICENSE", "License");
        var request = createRequest();
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credential));
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> credentialService.update(1L, request));
        verify(credentialRepository, never()).save(any());
    }

    @Test
    void updateVerificationStatus_shouldUpdateStatus() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credential = createCredential(1L, employee, "LICENSE", "Medical License");
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credential));
        when(credentialRepository.save(any(Credential.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = credentialService.updateVerificationStatus(1L, VerificationStatus.VERIFIED);

        assertEquals("VERIFIED", result.verificationStatus());
        verify(credentialRepository).save(any(Credential.class));
    }

    @Test
    void updateVerificationStatus_shouldThrowWhenNotFound() {
        when(credentialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> credentialService.updateVerificationStatus(999L, VerificationStatus.VERIFIED));
        verify(credentialRepository, never()).save(any());
    }

    @Test
    void updateVerificationStatus_shouldSetExpiredStatus() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credential = createCredential(1L, employee, "LICENSE", "Medical License");
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credential));
        when(credentialRepository.save(any(Credential.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = credentialService.updateVerificationStatus(1L, VerificationStatus.EXPIRED);

        assertEquals("EXPIRED", result.verificationStatus());
    }

    @Test
    void delete_shouldDeleteCredential() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credential = createCredential(1L, employee, "LICENSE", "Medical License");
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credential));

        credentialService.delete(1L);

        verify(credentialRepository).delete(credential);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(credentialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> credentialService.delete(999L));
        verify(credentialRepository, never()).delete(any());
    }

    @Test
    void getAttachments_shouldReturnAttachments() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credential = createCredential(1L, employee, "LICENSE", "Medical License");

        var attachment1 = new CredentialAttachment();
        attachment1.setId(1L);
        attachment1.setCredential(credential);
        attachment1.setFileName("license.pdf");
        attachment1.setFilePath("/uploads/license.pdf");
        attachment1.setFileSize(1024L);
        attachment1.setContentType("application/pdf");
        attachment1.setUploadedAt(LocalDateTime.of(2023, 6, 1, 10, 0));

        var attachment2 = new CredentialAttachment();
        attachment2.setId(2L);
        attachment2.setCredential(credential);
        attachment2.setFileName("cert.jpg");
        attachment2.setFilePath("/uploads/cert.jpg");
        attachment2.setFileSize(2048L);
        attachment2.setContentType("image/jpeg");
        attachment2.setUploadedAt(LocalDateTime.of(2023, 6, 1, 10, 0));

        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credential));
        when(credentialAttachmentRepository.findByCredentialId(1L))
                .thenReturn(List.of(attachment1, attachment2));

        var result = credentialService.getAttachments(1L);

        assertEquals(2, result.size());
        assertEquals("license.pdf", result.get(0).fileName());
        assertEquals("application/pdf", result.get(0).contentType());
        assertEquals(1024L, result.get(0).fileSize());
        assertEquals("cert.jpg", result.get(1).fileName());
    }

    @Test
    void getAttachments_shouldThrowWhenCredentialNotFound() {
        when(credentialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> credentialService.getAttachments(999L));
        verify(credentialAttachmentRepository, never()).findByCredentialId(any());
    }

    @Test
    void getAttachments_shouldReturnEmptyListWhenNoAttachments() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credential = createCredential(1L, employee, "LICENSE", "Medical License");
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credential));
        when(credentialAttachmentRepository.findByCredentialId(1L)).thenReturn(List.of());

        var result = credentialService.getAttachments(1L);

        assertEquals(0, result.size());
    }

    @Test
    void create_shouldMapAllFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new CredentialRequest(
                1L, "CERTIFICATION", "AWS Certified",
                "Amazon", LocalDate.of(2023, 6, 1),
                LocalDate.of(2026, 6, 1), "AWS-123", "Cloud cert"
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(credentialRepository.save(any(Credential.class))).thenAnswer(invocation -> {
            Credential c = invocation.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        var result = credentialService.create(request);

        assertEquals("CERTIFICATION", result.credentialType());
        assertEquals("AWS Certified", result.credentialName());
        assertEquals("Amazon", result.issuer());
        assertEquals(LocalDate.of(2023, 6, 1), result.issueDate());
        assertEquals(LocalDate.of(2026, 6, 1), result.expiryDate());
        assertEquals("AWS-123", result.credentialNumber());
        assertEquals("Cloud cert", result.notes());
        assertEquals("PENDING", result.verificationStatus());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var credential = createCredential(1L, employee, "LICENSE", "Medical License");
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credential));

        var result = credentialService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("LICENSE", result.credentialType());
        assertEquals("Medical License", result.credentialName());
        assertEquals("Test Issuer", result.issuer());
        assertEquals(LocalDate.of(2023, 1, 1), result.issueDate());
        assertEquals(LocalDate.of(2025, 12, 31), result.expiryDate());
        assertEquals("CRED-1", result.credentialNumber());
        assertEquals("PENDING", result.verificationStatus());
        assertEquals("Test notes", result.notes());
    }
}
