package com.raster.hrm.department;

import com.raster.hrm.department.dto.DepartmentRequest;
import com.raster.hrm.department.dto.DepartmentResponse;
import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.department.service.DepartmentService;
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
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department createDepartment(Long id, String name, String code) {
        Department department = new Department();
        department.setId(id);
        department.setName(name);
        department.setCode(code);
        department.setActive(true);
        department.setChildren(new ArrayList<>());
        return department;
    }

    @Test
    void getAll_shouldReturnAllDepartments() {
        var departments = List.of(
                createDepartment(1L, "Engineering", "ENG"),
                createDepartment(2L, "Marketing", "MKT")
        );
        when(departmentRepository.findAll()).thenReturn(departments);

        List<DepartmentResponse> result = departmentService.getAll();

        assertEquals(2, result.size());
        assertEquals("Engineering", result.get(0).name());
        assertEquals("Marketing", result.get(1).name());
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoDepartments() {
        when(departmentRepository.findAll()).thenReturn(List.of());

        List<DepartmentResponse> result = departmentService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getById_shouldReturnDepartment() {
        Department department = createDepartment(1L, "Engineering", "ENG");
        department.setDescription("Engineering dept");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        DepartmentResponse result = departmentService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("Engineering", result.name());
        assertEquals("ENG", result.code());
        assertEquals("Engineering dept", result.description());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.getById(999L));
    }

    @Test
    void getByParentId_shouldReturnChildren() {
        Department parent = createDepartment(1L, "Engineering", "ENG");
        Department child = createDepartment(2L, "Frontend", "FE");
        child.setParent(parent);
        when(departmentRepository.findByParentId(1L)).thenReturn(List.of(child));

        List<DepartmentResponse> result = departmentService.getByParentId(1L);

        assertEquals(1, result.size());
        assertEquals("Frontend", result.get(0).name());
        assertEquals(1L, result.get(0).parentId());
        assertEquals("Engineering", result.get(0).parentName());
    }

    @Test
    void getRootDepartments_shouldReturnDepartmentsWithNoParent() {
        var roots = List.of(createDepartment(1L, "Engineering", "ENG"));
        when(departmentRepository.findByParentIsNull()).thenReturn(roots);

        List<DepartmentResponse> result = departmentService.getRootDepartments();

        assertEquals(1, result.size());
        assertNull(result.get(0).parentId());
        assertNull(result.get(0).parentName());
    }

    @Test
    void create_shouldCreateAndReturnDepartment() {
        var request = new DepartmentRequest("Engineering", "ENG", null, "Engineering dept", true);
        Department saved = createDepartment(1L, "Engineering", "ENG");
        saved.setDescription("Engineering dept");

        when(departmentRepository.existsByCode("ENG")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(saved);

        DepartmentResponse result = departmentService.create(request);

        assertEquals("Engineering", result.name());
        assertEquals("ENG", result.code());
        assertEquals("Engineering dept", result.description());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void create_shouldSetActiveToTrueWhenNull() {
        var request = new DepartmentRequest("Engineering", "ENG", null, null, null);
        Department saved = createDepartment(1L, "Engineering", "ENG");

        when(departmentRepository.existsByCode("ENG")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department dept = invocation.getArgument(0);
            assertTrue(dept.getActive());
            dept.setId(1L);
            dept.setChildren(new ArrayList<>());
            return dept;
        });

        departmentService.create(request);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void create_shouldThrowWhenCodeAlreadyExists() {
        var request = new DepartmentRequest("Engineering", "ENG", null, null, true);
        when(departmentRepository.existsByCode("ENG")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> departmentService.create(request));
        assertTrue(ex.getMessage().contains("ENG"));
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void create_shouldSetParentWhenParentIdProvided() {
        var request = new DepartmentRequest("Frontend", "FE", 1L, null, true);
        Department parent = createDepartment(1L, "Engineering", "ENG");
        Department saved = createDepartment(2L, "Frontend", "FE");
        saved.setParent(parent);

        when(departmentRepository.existsByCode("FE")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(departmentRepository.save(any(Department.class))).thenReturn(saved);

        DepartmentResponse result = departmentService.create(request);

        assertEquals(1L, result.parentId());
        assertEquals("Engineering", result.parentName());
    }

    @Test
    void create_shouldThrowWhenParentNotFound() {
        var request = new DepartmentRequest("Frontend", "FE", 999L, null, true);
        when(departmentRepository.existsByCode("FE")).thenReturn(false);
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.create(request));
    }

    @Test
    void update_shouldUpdateAndReturnDepartment() {
        var request = new DepartmentRequest("Engineering Updated", "ENG", null, "Updated desc", true);
        Department existing = createDepartment(1L, "Engineering", "ENG");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByCode("ENG")).thenReturn(Optional.of(existing));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department dept = invocation.getArgument(0);
            dept.setChildren(new ArrayList<>());
            return dept;
        });

        DepartmentResponse result = departmentService.update(1L, request);

        assertEquals("Engineering Updated", result.name());
        assertEquals("Updated desc", result.description());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var request = new DepartmentRequest("Engineering", "ENG", null, null, true);
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.update(999L, request));
    }

    @Test
    void update_shouldThrowWhenCodeConflictsWithAnotherDepartment() {
        var request = new DepartmentRequest("Marketing", "MKT", null, null, true);
        Department existing = createDepartment(1L, "Engineering", "ENG");
        Department conflicting = createDepartment(2L, "Marketing", "MKT");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByCode("MKT")).thenReturn(Optional.of(conflicting));

        assertThrows(BadRequestException.class, () -> departmentService.update(1L, request));
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameCodeForSameDepartment() {
        var request = new DepartmentRequest("Engineering Updated", "ENG", null, null, true);
        Department existing = createDepartment(1L, "Engineering", "ENG");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByCode("ENG")).thenReturn(Optional.of(existing));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department dept = invocation.getArgument(0);
            dept.setChildren(new ArrayList<>());
            return dept;
        });

        DepartmentResponse result = departmentService.update(1L, request);

        assertNotNull(result);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void update_shouldAllowNewUniqueCode() {
        var request = new DepartmentRequest("Engineering", "ENG2", null, null, true);
        Department existing = createDepartment(1L, "Engineering", "ENG");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByCode("ENG2")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department dept = invocation.getArgument(0);
            dept.setChildren(new ArrayList<>());
            return dept;
        });

        DepartmentResponse result = departmentService.update(1L, request);

        assertNotNull(result);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void update_shouldSetParentWhenParentIdProvided() {
        var request = new DepartmentRequest("Frontend", "FE", 1L, null, true);
        Department existing = createDepartment(2L, "Frontend", "FE");
        Department parent = createDepartment(1L, "Engineering", "ENG");

        when(departmentRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByCode("FE")).thenReturn(Optional.of(existing));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department dept = invocation.getArgument(0);
            dept.setChildren(new ArrayList<>());
            return dept;
        });

        DepartmentResponse result = departmentService.update(2L, request);

        assertEquals(1L, result.parentId());
        assertEquals("Engineering", result.parentName());
    }

    @Test
    void update_shouldClearParentWhenParentIdIsNull() {
        var request = new DepartmentRequest("Frontend", "FE", null, null, true);
        Department parent = createDepartment(1L, "Engineering", "ENG");
        Department existing = createDepartment(2L, "Frontend", "FE");
        existing.setParent(parent);

        when(departmentRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByCode("FE")).thenReturn(Optional.of(existing));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department dept = invocation.getArgument(0);
            dept.setChildren(new ArrayList<>());
            return dept;
        });

        DepartmentResponse result = departmentService.update(2L, request);

        assertNull(result.parentId());
        assertNull(result.parentName());
    }

    @Test
    void update_shouldThrowWhenParentNotFound() {
        var request = new DepartmentRequest("Frontend", "FE", 999L, null, true);
        Department existing = createDepartment(2L, "Frontend", "FE");

        when(departmentRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByCode("FE")).thenReturn(Optional.of(existing));
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.update(2L, request));
    }

    @Test
    void update_shouldKeepExistingActiveWhenNull() {
        var request = new DepartmentRequest("Engineering", "ENG", null, null, null);
        Department existing = createDepartment(1L, "Engineering", "ENG");
        existing.setActive(false);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByCode("ENG")).thenReturn(Optional.of(existing));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department dept = invocation.getArgument(0);
            assertFalse(dept.getActive());
            dept.setChildren(new ArrayList<>());
            return dept;
        });

        departmentService.update(1L, request);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void delete_shouldDeleteDepartment() {
        Department department = createDepartment(1L, "Engineering", "ENG");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.countEmployeesByDepartmentId(1L)).thenReturn(0L);

        departmentService.delete(1L);

        verify(departmentRepository).delete(department);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.delete(999L));
    }

    @Test
    void delete_shouldThrowWhenDepartmentHasEmployees() {
        Department department = createDepartment(1L, "Engineering", "ENG");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.countEmployeesByDepartmentId(1L)).thenReturn(5L);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> departmentService.delete(1L));
        assertTrue(ex.getMessage().contains("employees"));
        verify(departmentRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapChildrenCorrectly() {
        Department parent = createDepartment(1L, "Engineering", "ENG");
        Department child = createDepartment(2L, "Frontend", "FE");
        child.setParent(parent);
        child.setChildren(new ArrayList<>());
        parent.setChildren(List.of(child));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(parent));

        DepartmentResponse result = departmentService.getById(1L);

        assertEquals(1, result.children().size());
        assertEquals("Frontend", result.children().get(0).name());
    }
}
