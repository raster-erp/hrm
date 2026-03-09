package com.raster.hrm.credential.service;

import com.raster.hrm.credential.dto.CredentialAttachmentResponse;
import com.raster.hrm.credential.dto.CredentialRequest;
import com.raster.hrm.credential.dto.CredentialResponse;
import com.raster.hrm.credential.entity.Credential;
import com.raster.hrm.credential.entity.CredentialAttachment;
import com.raster.hrm.credential.entity.VerificationStatus;
import com.raster.hrm.credential.repository.CredentialAttachmentRepository;
import com.raster.hrm.credential.repository.CredentialRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CredentialService {

    private static final Logger log = LoggerFactory.getLogger(CredentialService.class);

    private final CredentialRepository credentialRepository;
    private final CredentialAttachmentRepository credentialAttachmentRepository;
    private final EmployeeRepository employeeRepository;

    public CredentialService(CredentialRepository credentialRepository,
                             CredentialAttachmentRepository credentialAttachmentRepository,
                             EmployeeRepository employeeRepository) {
        this.credentialRepository = credentialRepository;
        this.credentialAttachmentRepository = credentialAttachmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<CredentialResponse> getAll(Pageable pageable) {
        return credentialRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public CredentialResponse getById(Long id) {
        var credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential", "id", id));
        return mapToResponse(credential);
    }

    @Transactional(readOnly = true)
    public List<CredentialResponse> getByEmployeeId(Long employeeId) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        return credentialRepository.findByEmployeeId(employee.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CredentialResponse> getExpiringCredentials(int days) {
        if (days < 0) {
            throw new BadRequestException("Days parameter must be a non-negative number");
        }
        var today = LocalDate.now();
        var endDate = today.plusDays(days);
        return credentialRepository.findByExpiryDateBetween(today, endDate).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CredentialResponse> getByStatus(VerificationStatus status) {
        return credentialRepository.findByVerificationStatus(status).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CredentialResponse create(CredentialRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var credential = new Credential();
        mapRequestToEntity(request, credential, employee);

        var saved = credentialRepository.save(credential);
        log.info("Created credential with id: {} for employee: {}", saved.getId(), employee.getEmployeeCode());
        return mapToResponse(saved);
    }

    public CredentialResponse update(Long id, CredentialRequest request) {
        var credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential", "id", id));

        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        mapRequestToEntity(request, credential, employee);

        var saved = credentialRepository.save(credential);
        log.info("Updated credential with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public CredentialResponse updateVerificationStatus(Long id, VerificationStatus status) {
        var credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential", "id", id));

        credential.setVerificationStatus(status);
        var saved = credentialRepository.save(credential);
        log.info("Updated verification status of credential id: {} to {}", id, status);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential", "id", id));
        credentialRepository.delete(credential);
        log.info("Deleted credential with id: {}", id);
    }

    @Transactional(readOnly = true)
    public List<CredentialAttachmentResponse> getAttachments(Long credentialId) {
        var credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential", "id", credentialId));
        return credentialAttachmentRepository.findByCredentialId(credential.getId()).stream()
                .map(this::mapToAttachmentResponse)
                .toList();
    }

    private void mapRequestToEntity(CredentialRequest request, Credential credential, Employee employee) {
        credential.setEmployee(employee);
        credential.setCredentialType(request.credentialType());
        credential.setCredentialName(request.credentialName());
        credential.setIssuer(request.issuer());
        credential.setIssueDate(request.issueDate());
        credential.setExpiryDate(request.expiryDate());
        credential.setCredentialNumber(request.credentialNumber());
        credential.setNotes(request.notes());
    }

    private CredentialResponse mapToResponse(Credential credential) {
        var employee = credential.getEmployee();
        return new CredentialResponse(
                credential.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                credential.getCredentialType(),
                credential.getCredentialName(),
                credential.getIssuer(),
                credential.getIssueDate(),
                credential.getExpiryDate(),
                credential.getCredentialNumber(),
                credential.getVerificationStatus().name(),
                credential.getNotes(),
                credential.getCreatedAt(),
                credential.getUpdatedAt()
        );
    }

    private CredentialAttachmentResponse mapToAttachmentResponse(CredentialAttachment attachment) {
        return new CredentialAttachmentResponse(
                attachment.getId(),
                attachment.getCredential().getId(),
                attachment.getFileName(),
                attachment.getFilePath(),
                attachment.getFileSize(),
                attachment.getContentType(),
                attachment.getUploadedAt()
        );
    }
}
