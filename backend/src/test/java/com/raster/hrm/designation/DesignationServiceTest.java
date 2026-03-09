package com.raster.hrm.designation;

import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.designation.dto.DesignationRequest;
import com.raster.hrm.designation.dto.DesignationResponse;
import com.raster.hrm.designation.entity.Designation;
import com.raster.hrm.designation.repository.DesignationRepository;
import com.raster.hrm.designation.service.DesignationService;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DesignationServiceTest {

    @Mock
    private DesignationRepository designationRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DesignationService designationService;

    private Department createDepartment(Long id, String name, String code) {
        Department department = new Department();
        department.setId(id);
        department.setName(name);
        department.setCode(code);
        department.setActive(true);
        department.setChildren(new ArrayList<>());
        return department;
    }

    private Designation createDesignation(Long id, String title, String code) {
        Designation designation = new Designation();
        designation.setId(id);
        designation.setTitle(title);
        designation.setCode(code);
        designation.setActive(true);
        return designation;
    }

    @Test
    void getAll_shouldReturnAllDesignations() {
        var designations = List.of(
                createDesignation(1L, "Software Engineer", "SE"),
                createDesignation(2L, "Senior Engineer", "SSE")
        );
        when(designationRepository.findAll()).thenReturn(designations);

        List<DesignationResponse> result = designationService.getAll();

        assertEquals(2, result.size());
        assertEquals("Software Engineer", result.get(0).title());
        assertEquals("Senior Engineer", result.get(1).title());
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoDesignations() {
        when(designationRepository.findAll()).thenReturn(List.of());

        List<DesignationResponse> result = designationService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getById_shouldReturnDesignation() {
        Department department = createDepartment(1L, "Engineering", "ENG");
        Designation designation = createDesignation(1L, "Software Engineer", "SE");
        designation.setLevel(3);
        designation.setGrade("A");
        designation.setDepartment(department);
        designation.setDescription("SE role");

        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));

        DesignationResponse result = designationService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("Software Engineer", result.title());
        assertEquals("SE", result.code());
        assertEquals(3, result.level());
        assertEquals("A", result.grade());
        assertEquals(1L, result.departmentId());
        assertEquals("Engineering", result.departmentName());
        assertEquals("SE role", result.description());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldReturnDesignationWithNullDepartment() {
        Designation designation = createDesignation(1L, "Software Engineer", "SE");

        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));

        DesignationResponse result = designationService.getById(1L);

        assertNull(result.departmentId());
        assertNull(result.departmentName());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(designationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> designationService.getById(999L));
    }

    @Test
    void getByDepartmentId_shouldReturnDesignations() {
        Department department = createDepartment(1L, "Engineering", "ENG");
        Designation designation = createDesignation(1L, "Software Engineer", "SE");
        designation.setDepartment(department);

        when(designationRepository.findByDepartmentId(1L)).thenReturn(List.of(designation));

        List<DesignationResponse> result = designationService.getByDepartmentId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).departmentId());
    }

    @Test
    void create_shouldCreateAndReturnDesignation() {
        var request = new DesignationRequest("Software Engineer", "SE", 3, "A", 1L, "SE role", true);
        Department department = createDepartment(1L, "Engineering", "ENG");
        Designation saved = createDesignation(1L, "Software Engineer", "SE");
        saved.setLevel(3);
        saved.setGrade("A");
        saved.setDepartment(department);
        saved.setDescription("SE role");

        when(designationRepository.existsByCode("SE")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(designationRepository.save(any(Designation.class))).thenReturn(saved);

        DesignationResponse result = designationService.create(request);

        assertEquals("Software Engineer", result.title());
        assertEquals("SE", result.code());
        assertEquals(3, result.level());
        assertEquals("A", result.grade());
        assertEquals(1L, result.departmentId());
        verify(designationRepository).save(any(Designation.class));
    }

    @Test
    void create_shouldCreateWithoutDepartment() {
        var request = new DesignationRequest("Software Engineer", "SE", 3, "A", null, null, true);
        Designation saved = createDesignation(1L, "Software Engineer", "SE");
        saved.setLevel(3);
        saved.setGrade("A");

        when(designationRepository.existsByCode("SE")).thenReturn(false);
        when(designationRepository.save(any(Designation.class))).thenReturn(saved);

        DesignationResponse result = designationService.create(request);

        assertNull(result.departmentId());
        assertNull(result.departmentName());
        verify(designationRepository).save(any(Designation.class));
    }

    @Test
    void create_shouldSetActiveToTrueWhenNull() {
        var request = new DesignationRequest("Software Engineer", "SE", null, null, null, null, null);
        Designation saved = createDesignation(1L, "Software Engineer", "SE");

        when(designationRepository.existsByCode("SE")).thenReturn(false);
        when(designationRepository.save(any(Designation.class))).thenAnswer(invocation -> {
            Designation desig = invocation.getArgument(0);
            assertTrue(desig.getActive());
            desig.setId(1L);
            return desig;
        });

        designationService.create(request);
        verify(designationRepository).save(any(Designation.class));
    }

    @Test
    void create_shouldThrowWhenCodeAlreadyExists() {
        var request = new DesignationRequest("Software Engineer", "SE", 3, "A", 1L, null, true);
        when(designationRepository.existsByCode("SE")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> designationService.create(request));
        assertTrue(ex.getMessage().contains("SE"));
        verify(designationRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenDepartmentNotFound() {
        var request = new DesignationRequest("Software Engineer", "SE", 3, "A", 999L, null, true);
        when(designationRepository.existsByCode("SE")).thenReturn(false);
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> designationService.create(request));
    }

    @Test
    void update_shouldUpdateAndReturnDesignation() {
        var request = new DesignationRequest("Senior Engineer", "SE", 4, "B", 1L, "Updated", true);
        Department department = createDepartment(1L, "Engineering", "ENG");
        Designation existing = createDesignation(1L, "Software Engineer", "SE");
        existing.setDepartment(department);

        when(designationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(designationRepository.findByCode("SE")).thenReturn(Optional.of(existing));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(designationRepository.save(any(Designation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DesignationResponse result = designationService.update(1L, request);

        assertEquals("Senior Engineer", result.title());
        assertEquals(4, result.level());
        assertEquals("B", result.grade());
        verify(designationRepository).save(any(Designation.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var request = new DesignationRequest("Software Engineer", "SE", 3, "A", 1L, null, true);
        when(designationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> designationService.update(999L, request));
    }

    @Test
    void update_shouldThrowWhenCodeConflictsWithAnotherDesignation() {
        var request = new DesignationRequest("Senior Engineer", "SSE", 4, "B", null, null, true);
        Designation existing = createDesignation(1L, "Software Engineer", "SE");
        Designation conflicting = createDesignation(2L, "Senior Engineer", "SSE");

        when(designationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(designationRepository.findByCode("SSE")).thenReturn(Optional.of(conflicting));

        assertThrows(BadRequestException.class, () -> designationService.update(1L, request));
        verify(designationRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameCodeForSameDesignation() {
        var request = new DesignationRequest("Software Engineer Updated", "SE", 3, "A", null, null, true);
        Designation existing = createDesignation(1L, "Software Engineer", "SE");

        when(designationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(designationRepository.findByCode("SE")).thenReturn(Optional.of(existing));
        when(designationRepository.save(any(Designation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DesignationResponse result = designationService.update(1L, request);

        assertNotNull(result);
        verify(designationRepository).save(any(Designation.class));
    }

    @Test
    void update_shouldAllowNewUniqueCode() {
        var request = new DesignationRequest("Software Engineer", "SE2", 3, "A", null, null, true);
        Designation existing = createDesignation(1L, "Software Engineer", "SE");

        when(designationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(designationRepository.findByCode("SE2")).thenReturn(Optional.empty());
        when(designationRepository.save(any(Designation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DesignationResponse result = designationService.update(1L, request);

        assertNotNull(result);
        verify(designationRepository).save(any(Designation.class));
    }

    @Test
    void update_shouldClearDepartmentWhenDepartmentIdIsNull() {
        var request = new DesignationRequest("Software Engineer", "SE", 3, "A", null, null, true);
        Department department = createDepartment(1L, "Engineering", "ENG");
        Designation existing = createDesignation(1L, "Software Engineer", "SE");
        existing.setDepartment(department);

        when(designationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(designationRepository.findByCode("SE")).thenReturn(Optional.of(existing));
        when(designationRepository.save(any(Designation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DesignationResponse result = designationService.update(1L, request);

        assertNull(result.departmentId());
        assertNull(result.departmentName());
    }

    @Test
    void update_shouldThrowWhenDepartmentNotFound() {
        var request = new DesignationRequest("Software Engineer", "SE", 3, "A", 999L, null, true);
        Designation existing = createDesignation(1L, "Software Engineer", "SE");

        when(designationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(designationRepository.findByCode("SE")).thenReturn(Optional.of(existing));
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> designationService.update(1L, request));
    }

    @Test
    void update_shouldKeepExistingActiveWhenNull() {
        var request = new DesignationRequest("Software Engineer", "SE", 3, "A", null, null, null);
        Designation existing = createDesignation(1L, "Software Engineer", "SE");
        existing.setActive(false);

        when(designationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(designationRepository.findByCode("SE")).thenReturn(Optional.of(existing));
        when(designationRepository.save(any(Designation.class))).thenAnswer(invocation -> {
            Designation desig = invocation.getArgument(0);
            assertFalse(desig.getActive());
            return desig;
        });

        designationService.update(1L, request);
        verify(designationRepository).save(any(Designation.class));
    }

    @Test
    void delete_shouldDeleteDesignation() {
        Designation designation = createDesignation(1L, "Software Engineer", "SE");
        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));
        when(designationRepository.countEmployeesByDesignationId(1L)).thenReturn(0L);

        designationService.delete(1L);

        verify(designationRepository).delete(designation);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(designationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> designationService.delete(999L));
    }

    @Test
    void delete_shouldThrowWhenDesignationHasEmployees() {
        Designation designation = createDesignation(1L, "Software Engineer", "SE");
        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));
        when(designationRepository.countEmployeesByDesignationId(1L)).thenReturn(3L);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> designationService.delete(1L));
        assertTrue(ex.getMessage().contains("employees"));
        verify(designationRepository, never()).delete(any());
    }
}
