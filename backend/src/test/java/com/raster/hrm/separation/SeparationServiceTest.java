package com.raster.hrm.separation;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.entity.EmploymentStatus;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.separation.dto.SeparationRequest;
import com.raster.hrm.separation.dto.SeparationResponse;
import com.raster.hrm.separation.entity.Separation;
import com.raster.hrm.separation.entity.SeparationStatus;
import com.raster.hrm.separation.entity.SeparationType;
import com.raster.hrm.separation.repository.SeparationRepository;
import com.raster.hrm.separation.service.SeparationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeparationServiceTest {

    @Mock
    private SeparationRepository separationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SeparationService separationService;

    private Employee createEmployee(Long id, String code, String firstName, String lastName) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "@test.com");
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        return employee;
    }

    private Separation createSeparation(Long id, Employee employee, SeparationStatus status) {
        var separation = new Separation();
        separation.setId(id);
        separation.setEmployee(employee);
        separation.setSeparationType(SeparationType.RESIGNATION);
        separation.setReason("Personal reasons");
        separation.setNoticeDate(LocalDate.of(2024, 1, 1));
        separation.setLastWorkingDay(LocalDate.of(2024, 2, 1));
        separation.setStatus(status);
        separation.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        separation.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return separation;
    }

    private SeparationRequest createRequest() {
        return new SeparationRequest(
                1L, "RESIGNATION", "Personal reasons",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)
        );
    }

    @Test
    void getAll_shouldReturnPageOfSeparations() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separations = List.of(
                createSeparation(1L, employee, SeparationStatus.PENDING),
                createSeparation(2L, employee, SeparationStatus.APPROVED)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(separations, pageable, 2);
        when(separationRepository.findAll(pageable)).thenReturn(page);

        var result = separationService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("PENDING", result.getContent().get(0).status());
        assertEquals("APPROVED", result.getContent().get(1).status());
        verify(separationRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<Separation>(List.of(), pageable, 0);
        when(separationRepository.findAll(pageable)).thenReturn(page);

        var result = separationService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnSeparation() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separation = createSeparation(1L, employee, SeparationStatus.PENDING);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));

        var result = separationService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("RESIGNATION", result.separationType());
        assertEquals("Personal reasons", result.reason());
        assertEquals("PENDING", result.status());
        assertNull(result.approvedById());
        assertNull(result.approvedByName());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(separationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> separationService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnSeparations() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separations = List.of(
                createSeparation(1L, employee, SeparationStatus.PENDING),
                createSeparation(2L, employee, SeparationStatus.REJECTED)
        );
        when(separationRepository.findByEmployeeId(1L)).thenReturn(separations);

        var result = separationService.getByEmployeeId(1L);

        assertEquals(2, result.size());
        assertEquals("PENDING", result.get(0).status());
        assertEquals("REJECTED", result.get(1).status());
    }

    @Test
    void getByEmployeeId_shouldReturnEmptyList() {
        when(separationRepository.findByEmployeeId(1L)).thenReturn(List.of());

        var result = separationService.getByEmployeeId(1L);

        assertEquals(0, result.size());
    }

    @Test
    void getPendingSeparations_shouldReturnPendingSeparations() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separations = List.of(createSeparation(1L, employee, SeparationStatus.PENDING));
        when(separationRepository.findByStatus(SeparationStatus.PENDING)).thenReturn(separations);

        var result = separationService.getPendingSeparations();

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).status());
    }

    @Test
    void getPendingSeparations_shouldReturnEmptyList() {
        when(separationRepository.findByStatus(SeparationStatus.PENDING)).thenReturn(List.of());

        var result = separationService.getPendingSeparations();

        assertEquals(0, result.size());
    }

    @Test
    void create_shouldCreateAndReturnSeparation() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(separationRepository.save(any(Separation.class))).thenAnswer(invocation -> {
            Separation s = invocation.getArgument(0);
            s.setId(1L);
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        var result = separationService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("RESIGNATION", result.separationType());
        assertEquals("Personal reasons", result.reason());
        assertEquals("PENDING", result.status());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        verify(separationRepository).save(any(Separation.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> separationService.create(request));
        verify(separationRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenInvalidSeparationType() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new SeparationRequest(1L, "INVALID_TYPE", "Reason",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(BadRequestException.class,
                () -> separationService.create(request));
        verify(separationRepository, never()).save(any());
    }

    @Test
    void create_shouldMapAllFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new SeparationRequest(
                1L, "TERMINATION", "Policy violation",
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 15)
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(separationRepository.save(any(Separation.class))).thenAnswer(invocation -> {
            Separation s = invocation.getArgument(0);
            s.setId(1L);
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        var result = separationService.create(request);

        assertEquals("TERMINATION", result.separationType());
        assertEquals("Policy violation", result.reason());
        assertEquals(LocalDate.of(2024, 3, 1), result.noticeDate());
        assertEquals(LocalDate.of(2024, 3, 15), result.lastWorkingDay());
        assertEquals("PENDING", result.status());
    }

    @Test
    void approve_shouldApproveAndReturnSeparation() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var separation = createSeparation(1L, employee, SeparationStatus.PENDING);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(separationRepository.save(any(Separation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = separationService.approve(1L, 2L);

        assertEquals("APPROVED", result.status());
        assertEquals(2L, result.approvedById());
        assertEquals("Jane Smith", result.approvedByName());
        assertNotNull(result.approvedAt());
        verify(separationRepository).save(any(Separation.class));
    }

    @Test
    void approve_shouldThrowWhenSeparationNotFound() {
        when(separationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> separationService.approve(999L, 2L));
        verify(separationRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenNotPending() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separation = createSeparation(1L, employee, SeparationStatus.APPROVED);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));

        assertThrows(BadRequestException.class,
                () -> separationService.approve(1L, 2L));
        verify(separationRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenApproverNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separation = createSeparation(1L, employee, SeparationStatus.PENDING);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> separationService.approve(1L, 2L));
        verify(separationRepository, never()).save(any());
    }

    @Test
    void reject_shouldRejectAndReturnSeparation() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var separation = createSeparation(1L, employee, SeparationStatus.PENDING);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(separationRepository.save(any(Separation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = separationService.reject(1L, 2L);

        assertEquals("REJECTED", result.status());
        assertEquals(2L, result.approvedById());
        assertEquals("Jane Smith", result.approvedByName());
        assertNotNull(result.approvedAt());
        verify(separationRepository).save(any(Separation.class));
    }

    @Test
    void reject_shouldThrowWhenSeparationNotFound() {
        when(separationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> separationService.reject(999L, 2L));
        verify(separationRepository, never()).save(any());
    }

    @Test
    void reject_shouldThrowWhenNotPending() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separation = createSeparation(1L, employee, SeparationStatus.APPROVED);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));

        assertThrows(BadRequestException.class,
                () -> separationService.reject(1L, 2L));
        verify(separationRepository, never()).save(any());
    }

    @Test
    void reject_shouldThrowWhenApproverNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separation = createSeparation(1L, employee, SeparationStatus.PENDING);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> separationService.reject(1L, 2L));
        verify(separationRepository, never()).save(any());
    }

    @Test
    void finalizeSeparation_shouldFinalizeAndSetEmployeeInactive() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separation = createSeparation(1L, employee, SeparationStatus.APPROVED);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(separationRepository.save(any(Separation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = separationService.finalizeSeparation(1L);

        assertEquals("FINALIZED", result.status());
        assertEquals(EmploymentStatus.INACTIVE, employee.getEmploymentStatus());
        verify(employeeRepository).save(employee);
        verify(separationRepository).save(any(Separation.class));
    }

    @Test
    void finalizeSeparation_shouldThrowWhenSeparationNotFound() {
        when(separationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> separationService.finalizeSeparation(999L));
        verify(separationRepository, never()).save(any());
    }

    @Test
    void finalizeSeparation_shouldThrowWhenNotApproved() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separation = createSeparation(1L, employee, SeparationStatus.PENDING);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));

        assertThrows(BadRequestException.class,
                () -> separationService.finalizeSeparation(1L));
        verify(separationRepository, never()).save(any());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void finalizeSeparation_shouldThrowWhenRejected() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separation = createSeparation(1L, employee, SeparationStatus.REJECTED);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));

        assertThrows(BadRequestException.class,
                () -> separationService.finalizeSeparation(1L));
    }

    @Test
    void finalizeSeparation_shouldThrowWhenAlreadyFinalized() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separation = createSeparation(1L, employee, SeparationStatus.FINALIZED);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));

        assertThrows(BadRequestException.class,
                () -> separationService.finalizeSeparation(1L));
    }

    @Test
    void delete_shouldDeletePendingSeparation() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separation = createSeparation(1L, employee, SeparationStatus.PENDING);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));

        separationService.delete(1L);

        verify(separationRepository).delete(separation);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(separationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> separationService.delete(999L));
        verify(separationRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowWhenNotPending() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separation = createSeparation(1L, employee, SeparationStatus.APPROVED);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));

        assertThrows(BadRequestException.class,
                () -> separationService.delete(1L));
        verify(separationRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowWhenFinalized() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var separation = createSeparation(1L, employee, SeparationStatus.FINALIZED);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));

        assertThrows(BadRequestException.class,
                () -> separationService.delete(1L));
        verify(separationRepository, never()).delete(any());
    }

    @Test
    void workflowTest_createApproveFinalize() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var request = createRequest();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(separationRepository.save(any(Separation.class))).thenAnswer(invocation -> {
            Separation s = invocation.getArgument(0);
            s.setId(1L);
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        var created = separationService.create(request);
        assertEquals("PENDING", created.status());

        var separation = createSeparation(1L, employee, SeparationStatus.PENDING);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(separationRepository.save(any(Separation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var approved = separationService.approve(1L, 2L);
        assertEquals("APPROVED", approved.status());

        separation.setStatus(SeparationStatus.APPROVED);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var finalized = separationService.finalizeSeparation(1L);
        assertEquals("FINALIZED", finalized.status());
        assertEquals(EmploymentStatus.INACTIVE, employee.getEmploymentStatus());
    }

    @Test
    void workflowTest_createReject() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var request = createRequest();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(separationRepository.save(any(Separation.class))).thenAnswer(invocation -> {
            Separation s = invocation.getArgument(0);
            s.setId(1L);
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        var created = separationService.create(request);
        assertEquals("PENDING", created.status());

        var separation = createSeparation(1L, employee, SeparationStatus.PENDING);
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(separationRepository.save(any(Separation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var rejected = separationService.reject(1L, 2L);
        assertEquals("REJECTED", rejected.status());
    }

    @Test
    void getById_shouldMapApprovedByFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var separation = createSeparation(1L, employee, SeparationStatus.APPROVED);
        separation.setApprovedBy(approver);
        separation.setApprovedAt(LocalDateTime.of(2024, 2, 1, 10, 0));
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));

        var result = separationService.getById(1L);

        assertEquals(2L, result.approvedById());
        assertEquals("Jane Smith", result.approvedByName());
        assertEquals(LocalDateTime.of(2024, 2, 1, 10, 0), result.approvedAt());
    }
}
