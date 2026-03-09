package com.raster.hrm.employee;

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
import com.raster.hrm.employee.service.EmployeeService;
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
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeDocumentRepository employeeDocumentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DesignationRepository designationRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Department createDepartment(Long id, String name) {
        var department = new Department();
        department.setId(id);
        department.setName(name);
        department.setCode("DEPT" + id);
        department.setActive(true);
        return department;
    }

    private Designation createDesignation(Long id, String title) {
        var designation = new Designation();
        designation.setId(id);
        designation.setTitle(title);
        designation.setCode("DES" + id);
        designation.setActive(true);
        return designation;
    }

    private Employee createEmployee(Long id, String code, String firstName, String lastName) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "@test.com");
        employee.setPhone("1234567890");
        employee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        employee.setGender("Male");
        employee.setAddressLine1("123 Main St");
        employee.setCity("Springfield");
        employee.setState("IL");
        employee.setCountry("US");
        employee.setZipCode("62701");
        employee.setEmergencyContactName("Jane Doe");
        employee.setEmergencyContactPhone("0987654321");
        employee.setEmergencyContactRelationship("Spouse");
        employee.setBankName("Test Bank");
        employee.setBankAccountNumber("123456789");
        employee.setBankIfscCode("TEST0001");
        employee.setJoiningDate(LocalDate.of(2023, 1, 15));
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setDeleted(false);
        employee.setCreatedAt(LocalDateTime.of(2023, 1, 15, 10, 0));
        employee.setUpdatedAt(LocalDateTime.of(2023, 1, 15, 10, 0));
        return employee;
    }

    private EmployeeRequest createEmployeeRequest(String code, String firstName, String lastName) {
        return new EmployeeRequest(
                code, firstName, lastName, firstName.toLowerCase() + "@test.com",
                "1234567890", LocalDate.of(1990, 1, 1), "Male",
                "123 Main St", null, "Springfield", "IL", "US", "62701",
                "Jane Doe", "0987654321", "Spouse",
                "Test Bank", "123456789", "TEST0001",
                1L, 1L, LocalDate.of(2023, 1, 15), "ACTIVE"
        );
    }

    @Test
    void getAll_shouldReturnPageOfEmployees() {
        var employees = List.of(
                createEmployee(1L, "EMP001", "John", "Doe"),
                createEmployee(2L, "EMP002", "Jane", "Smith")
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(employees, pageable, 2);
        when(employeeRepository.findByDeletedFalse(pageable)).thenReturn(page);

        var result = employeeService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).firstName());
        assertEquals("Jane", result.getContent().get(1).firstName());
    }

    @Test
    void getAll_shouldReturnEmptyPageWhenNoEmployees() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<Employee>(List.of(), pageable, 0);
        when(employeeRepository.findByDeletedFalse(pageable)).thenReturn(page);

        var result = employeeService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getById_shouldReturnEmployee() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var department = createDepartment(1L, "Engineering");
        var designation = createDesignation(1L, "Software Engineer");
        employee.setDepartment(department);
        employee.setDesignation(designation);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        var result = employeeService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals("Engineering", result.departmentName());
        assertEquals("Software Engineer", result.designationTitle());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getById(999L));

        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void getById_shouldThrowWhenDeleted() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        employee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getById(1L));
    }

    @Test
    void getById_shouldReturnEmployeeWithNullDepartmentAndDesignation() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        employee.setDepartment(null);
        employee.setDesignation(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        var result = employeeService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(null, result.departmentId());
        assertEquals(null, result.departmentName());
        assertEquals(null, result.designationId());
        assertEquals(null, result.designationTitle());
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_shouldReturnFilteredResults() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(List.of(employee), pageable, 1);
        when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        var criteria = new EmployeeSearchCriteria("John", null, null, null, null);
        var result = employeeService.search(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).firstName());
    }

    @Test
    void create_shouldCreateAndReturnEmployee() {
        var request = createEmployeeRequest("EMP001", "John", "Doe");
        var department = createDepartment(1L, "Engineering");
        var designation = createDesignation(1L, "Software Engineer");

        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            var emp = invocation.getArgument(0, Employee.class);
            emp.setId(1L);
            emp.setCreatedAt(LocalDateTime.now());
            emp.setUpdatedAt(LocalDateTime.now());
            return emp;
        });

        var result = employeeService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals("Engineering", result.departmentName());
        assertEquals("Software Engineer", result.designationTitle());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void create_shouldThrowWhenDuplicateCode() {
        var request = createEmployeeRequest("EMP001", "John", "Doe");
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(true);

        var exception = assertThrows(BadRequestException.class,
                () -> employeeService.create(request));

        assertTrue(exception.getMessage().contains("EMP001"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenDuplicateEmail() {
        var request = createEmployeeRequest("EMP001", "John", "Doe");
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("john@test.com")).thenReturn(true);

        var exception = assertThrows(BadRequestException.class,
                () -> employeeService.create(request));

        assertTrue(exception.getMessage().contains("john@test.com"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenDepartmentNotFound() {
        var request = createEmployeeRequest("EMP001", "John", "Doe");
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.create(request));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenDesignationNotFound() {
        var request = createEmployeeRequest("EMP001", "John", "Doe");
        var department = createDepartment(1L, "Engineering");
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(designationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.create(request));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_shouldCreateWithNullDepartmentAndDesignation() {
        var request = new EmployeeRequest(
                "EMP001", "John", "Doe", "john@test.com",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, "ACTIVE"
        );
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            var emp = invocation.getArgument(0, Employee.class);
            emp.setId(1L);
            emp.setCreatedAt(LocalDateTime.now());
            emp.setUpdatedAt(LocalDateTime.now());
            return emp;
        });

        var result = employeeService.create(request);

        assertNotNull(result);
        assertEquals(null, result.departmentId());
        assertEquals(null, result.designationId());
    }

    @Test
    void create_shouldCreateWithNullEmploymentStatus() {
        var request = new EmployeeRequest(
                "EMP001", "John", "Doe", "john@test.com",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null
        );
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            var emp = invocation.getArgument(0, Employee.class);
            emp.setId(1L);
            emp.setCreatedAt(LocalDateTime.now());
            emp.setUpdatedAt(LocalDateTime.now());
            return emp;
        });

        var result = employeeService.create(request);

        assertNotNull(result);
        assertEquals("ACTIVE", result.employmentStatus());
    }

    @Test
    void create_shouldCreateWithBlankEmploymentStatus() {
        var request = new EmployeeRequest(
                "EMP001", "John", "Doe", "john@test.com",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, ""
        );
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            var emp = invocation.getArgument(0, Employee.class);
            emp.setId(1L);
            emp.setCreatedAt(LocalDateTime.now());
            emp.setUpdatedAt(LocalDateTime.now());
            return emp;
        });

        var result = employeeService.create(request);

        assertNotNull(result);
        assertEquals("ACTIVE", result.employmentStatus());
    }

    @Test
    void update_shouldUpdateAndReturnEmployee() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var department = createDepartment(1L, "Engineering");
        var designation = createDesignation(1L, "Software Engineer");
        employee.setDepartment(department);
        employee.setDesignation(designation);

        var request = createEmployeeRequest("EMP001", "John", "Updated");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            var emp = invocation.getArgument(0, Employee.class);
            emp.setUpdatedAt(LocalDateTime.now());
            return emp;
        });

        var result = employeeService.update(1L, request);

        assertNotNull(result);
        assertEquals("Updated", result.lastName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var request = createEmployeeRequest("EMP001", "John", "Doe");
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.update(999L, request));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenDeleted() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        employee.setDeleted(true);
        var request = createEmployeeRequest("EMP001", "John", "Doe");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.update(1L, request));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenDuplicateCodeOnDifferentEmployee() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = createEmployeeRequest("EMP002", "John", "Doe");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmployeeCode("EMP002")).thenReturn(true);

        var exception = assertThrows(BadRequestException.class,
                () -> employeeService.update(1L, request));

        assertTrue(exception.getMessage().contains("EMP002"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenDuplicateEmailOnDifferentEmployee() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new EmployeeRequest(
                "EMP001", "John", "Doe", "other@test.com",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmail("other@test.com")).thenReturn(true);

        var exception = assertThrows(BadRequestException.class,
                () -> employeeService.update(1L, request));

        assertTrue(exception.getMessage().contains("other@test.com"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameCodeAndEmail() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        employee.setEmail("john@test.com");
        var request = createEmployeeRequest("EMP001", "John", "Updated");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment(1L, "Engineering")));
        when(designationRepository.findById(1L)).thenReturn(Optional.of(createDesignation(1L, "SE")));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            var emp = invocation.getArgument(0, Employee.class);
            emp.setUpdatedAt(LocalDateTime.now());
            return emp;
        });

        var result = employeeService.update(1L, request);

        assertNotNull(result);
        assertEquals("Updated", result.lastName());
    }

    @Test
    void softDelete_shouldSetDeletedAndInactive() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        employeeService.softDelete(1L);

        assertTrue(employee.isDeleted());
        assertEquals(EmploymentStatus.INACTIVE, employee.getEmploymentStatus());
        verify(employeeRepository).save(employee);
    }

    @Test
    void softDelete_shouldThrowWhenNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.softDelete(999L));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void softDelete_shouldThrowWhenAlreadyDeleted() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        employee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.softDelete(1L));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void getDocuments_shouldReturnDocumentList() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        var doc1 = new EmployeeDocument();
        doc1.setId(1L);
        doc1.setEmployee(employee);
        doc1.setDocumentType("ID_PROOF");
        doc1.setDocumentName("passport.pdf");
        doc1.setFilePath("/uploads/passport.pdf");
        doc1.setFileSize(1024L);
        doc1.setContentType("application/pdf");
        doc1.setUploadedAt(LocalDateTime.of(2023, 6, 1, 10, 0));

        var doc2 = new EmployeeDocument();
        doc2.setId(2L);
        doc2.setEmployee(employee);
        doc2.setDocumentType("PHOTO");
        doc2.setDocumentName("photo.jpg");
        doc2.setFilePath("/uploads/photo.jpg");
        doc2.setFileSize(2048L);
        doc2.setContentType("image/jpeg");
        doc2.setUploadedAt(LocalDateTime.of(2023, 6, 1, 10, 0));

        when(employeeDocumentRepository.findByEmployeeId(1L)).thenReturn(List.of(doc1, doc2));

        List<EmployeeDocumentResponse> result = employeeService.getDocuments(1L);

        assertEquals(2, result.size());
        assertEquals("ID_PROOF", result.get(0).documentType());
        assertEquals("passport.pdf", result.get(0).documentName());
        assertEquals(1024L, result.get(0).fileSize());
        assertEquals("PHOTO", result.get(1).documentType());
    }

    @Test
    void getDocuments_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getDocuments(999L));
    }

    @Test
    void getDocuments_shouldThrowWhenEmployeeDeleted() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        employee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getDocuments(1L));
    }

    @Test
    void getDocuments_shouldReturnEmptyListWhenNoDocuments() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeDocumentRepository.findByEmployeeId(1L)).thenReturn(List.of());

        var result = employeeService.getDocuments(1L);

        assertTrue(result.isEmpty());
    }
}
