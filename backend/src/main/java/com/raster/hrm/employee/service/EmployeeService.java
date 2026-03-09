package com.raster.hrm.employee.service;

import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.designation.entity.Designation;
import com.raster.hrm.designation.repository.DesignationRepository;
import com.raster.hrm.employee.dto.EmployeeDocumentResponse;
import com.raster.hrm.employee.dto.EmployeeRequest;
import com.raster.hrm.employee.dto.EmployeeResponse;
import com.raster.hrm.employee.dto.EmployeeSearchCriteria;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.entity.EmployeeDocument;
import com.raster.hrm.employee.entity.EmploymentStatus;
import com.raster.hrm.employee.repository.EmployeeDocumentRepository;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           EmployeeDocumentRepository employeeDocumentRepository,
                           DepartmentRepository departmentRepository,
                           DesignationRepository designationRepository) {
        this.employeeRepository = employeeRepository;
        this.employeeDocumentRepository = employeeDocumentRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAll(Pageable pageable) {
        return employeeRepository.findByDeletedFalse(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        var employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return mapToResponse(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> search(EmployeeSearchCriteria criteria, Pageable pageable) {
        var specification = EmployeeSpecification.buildSpecification(criteria);
        return employeeRepository.findAll(specification, pageable)
                .map(this::mapToResponse);
    }

    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new BadRequestException(
                    "Employee with code '" + request.employeeCode() + "' already exists");
        }
        if (employeeRepository.existsByEmail(request.email())) {
            throw new BadRequestException(
                    "Employee with email '" + request.email() + "' already exists");
        }

        var employee = new Employee();
        mapRequestToEntity(request, employee);

        var saved = employeeRepository.save(employee);
        log.info("Created employee with id: {} and code: {}", saved.getId(), saved.getEmployeeCode());
        return mapToResponse(saved);
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        var employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (!employee.getEmployeeCode().equals(request.employeeCode())
                && employeeRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new BadRequestException(
                    "Employee with code '" + request.employeeCode() + "' already exists");
        }
        if (!employee.getEmail().equals(request.email())
                && employeeRepository.existsByEmail(request.email())) {
            throw new BadRequestException(
                    "Employee with email '" + request.email() + "' already exists");
        }

        mapRequestToEntity(request, employee);

        var saved = employeeRepository.save(employee);
        log.info("Updated employee with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public void softDelete(Long id) {
        var employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        employee.setDeleted(true);
        employee.setEmploymentStatus(EmploymentStatus.INACTIVE);
        employeeRepository.save(employee);
        log.info("Soft-deleted employee with id: {}", id);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDocumentResponse> getDocuments(Long employeeId) {
        var employee = employeeRepository.findById(employeeId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        return employeeDocumentRepository.findByEmployeeId(employee.getId()).stream()
                .map(this::mapToDocumentResponse)
                .toList();
    }

    private void mapRequestToEntity(EmployeeRequest request, Employee employee) {
        employee.setEmployeeCode(request.employeeCode());
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPhone(request.phone());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setGender(request.gender());
        employee.setAddressLine1(request.addressLine1());
        employee.setAddressLine2(request.addressLine2());
        employee.setCity(request.city());
        employee.setState(request.state());
        employee.setCountry(request.country());
        employee.setZipCode(request.zipCode());
        employee.setEmergencyContactName(request.emergencyContactName());
        employee.setEmergencyContactPhone(request.emergencyContactPhone());
        employee.setEmergencyContactRelationship(request.emergencyContactRelationship());
        employee.setBankName(request.bankName());
        employee.setBankAccountNumber(request.bankAccountNumber());
        employee.setBankIfscCode(request.bankIfscCode());
        employee.setJoiningDate(request.joiningDate());

        if (request.employmentStatus() != null && !request.employmentStatus().isBlank()) {
            employee.setEmploymentStatus(EmploymentStatus.valueOf(request.employmentStatus()));
        }

        if (request.departmentId() != null) {
            var department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.departmentId()));
            employee.setDepartment(department);
        } else {
            employee.setDepartment(null);
        }

        if (request.designationId() != null) {
            var designation = designationRepository.findById(request.designationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.designationId()));
            employee.setDesignation(designation);
        } else {
            employee.setDesignation(null);
        }
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getDateOfBirth(),
                employee.getGender(),
                employee.getAddressLine1(),
                employee.getAddressLine2(),
                employee.getCity(),
                employee.getState(),
                employee.getCountry(),
                employee.getZipCode(),
                employee.getEmergencyContactName(),
                employee.getEmergencyContactPhone(),
                employee.getEmergencyContactRelationship(),
                employee.getBankName(),
                employee.getBankAccountNumber(),
                employee.getBankIfscCode(),
                employee.getDepartment() != null ? employee.getDepartment().getId() : null,
                employee.getDepartment() != null ? employee.getDepartment().getName() : null,
                employee.getDesignation() != null ? employee.getDesignation().getId() : null,
                employee.getDesignation() != null ? employee.getDesignation().getTitle() : null,
                employee.getJoiningDate(),
                employee.getEmploymentStatus().name(),
                employee.getPhotoUrl(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }

    private EmployeeDocumentResponse mapToDocumentResponse(EmployeeDocument document) {
        return new EmployeeDocumentResponse(
                document.getId(),
                document.getEmployee().getId(),
                document.getDocumentType(),
                document.getDocumentName(),
                document.getFilePath(),
                document.getFileSize(),
                document.getContentType(),
                document.getUploadedAt()
        );
    }
}
