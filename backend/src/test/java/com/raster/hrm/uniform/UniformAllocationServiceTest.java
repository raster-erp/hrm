package com.raster.hrm.uniform;

import com.raster.hrm.uniform.dto.UniformAllocationRequest;
import com.raster.hrm.uniform.entity.AllocationStatus;
import com.raster.hrm.uniform.entity.Uniform;
import com.raster.hrm.uniform.entity.UniformAllocation;
import com.raster.hrm.uniform.repository.UniformAllocationRepository;
import com.raster.hrm.uniform.repository.UniformRepository;
import com.raster.hrm.uniform.service.UniformAllocationService;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.ResourceNotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniformAllocationServiceTest {

    @Mock
    private UniformAllocationRepository allocationRepository;

    @Mock
    private UniformRepository uniformRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private UniformAllocationService allocationService;

    private Employee createEmployee(Long id, String code, String firstName, String lastName) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "@test.com");
        return employee;
    }

    private Uniform createUniform(Long id, String name, String type) {
        var uniform = new Uniform();
        uniform.setId(id);
        uniform.setName(name);
        uniform.setType(type);
        uniform.setSize("M");
        uniform.setDescription("Standard uniform");
        uniform.setActive(true);
        uniform.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        uniform.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return uniform;
    }

    private UniformAllocation createAllocation(Long id, Employee employee, Uniform uniform) {
        var allocation = new UniformAllocation();
        allocation.setId(id);
        allocation.setEmployee(employee);
        allocation.setUniform(uniform);
        allocation.setAllocatedDate(LocalDate.of(2024, 1, 1));
        allocation.setStatus(AllocationStatus.ALLOCATED);
        allocation.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        allocation.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return allocation;
    }

    private UniformAllocationRequest createRequest() {
        return new UniformAllocationRequest(
                1L, 1L, LocalDate.of(2024, 1, 1)
        );
    }

    @Test
    void getAll_shouldReturnPageOfAllocations() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var uniform = createUniform(1L, "Safety Vest", "PPE");
        var allocations = List.of(
                createAllocation(1L, employee, uniform),
                createAllocation(2L, employee, uniform)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(allocations, pageable, 2);
        when(allocationRepository.findAll(pageable)).thenReturn(page);

        var result = allocationService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("ALLOCATED", result.getContent().get(0).status());
        verify(allocationRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<UniformAllocation>(List.of(), pageable, 0);
        when(allocationRepository.findAll(pageable)).thenReturn(page);

        var result = allocationService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnAllocation() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var uniform = createUniform(1L, "Safety Vest", "PPE");
        var allocation = createAllocation(1L, employee, uniform);
        when(allocationRepository.findById(1L)).thenReturn(Optional.of(allocation));

        var result = allocationService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ALLOCATED", result.status());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("Safety Vest", result.uniformName());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(allocationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> allocationService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnAllocations() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var uniform = createUniform(1L, "Safety Vest", "PPE");
        var allocations = List.of(
                createAllocation(1L, employee, uniform),
                createAllocation(2L, employee, uniform)
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(allocationRepository.findByEmployeeId(1L)).thenReturn(allocations);

        var result = allocationService.getByEmployeeId(1L);

        assertEquals(2, result.size());
        assertEquals("Safety Vest", result.get(0).uniformName());
    }

    @Test
    void getByEmployeeId_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> allocationService.getByEmployeeId(999L));
        verify(allocationRepository, never()).findByEmployeeId(any());
    }

    @Test
    void getByEmployeeId_shouldReturnEmptyListWhenNoAllocations() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(allocationRepository.findByEmployeeId(1L)).thenReturn(List.of());

        var result = allocationService.getByEmployeeId(1L);

        assertEquals(0, result.size());
    }

    @Test
    void allocate_shouldCreateAndReturnAllocation() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var uniform = createUniform(1L, "Safety Vest", "PPE");
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(uniformRepository.findById(1L)).thenReturn(Optional.of(uniform));
        when(allocationRepository.save(any(UniformAllocation.class))).thenAnswer(invocation -> {
            UniformAllocation a = invocation.getArgument(0);
            a.setId(1L);
            a.setCreatedAt(LocalDateTime.now());
            a.setUpdatedAt(LocalDateTime.now());
            return a;
        });

        var result = allocationService.allocate(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ALLOCATED", result.status());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("Safety Vest", result.uniformName());
        assertEquals(LocalDate.of(2024, 1, 1), result.allocatedDate());
        verify(allocationRepository).save(any(UniformAllocation.class));
    }

    @Test
    void allocate_shouldThrowWhenEmployeeNotFound() {
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> allocationService.allocate(request));
        verify(allocationRepository, never()).save(any());
    }

    @Test
    void allocate_shouldThrowWhenUniformNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(uniformRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> allocationService.allocate(request));
        verify(allocationRepository, never()).save(any());
    }

    @Test
    void markReturned_shouldUpdateAllocation() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var uniform = createUniform(1L, "Safety Vest", "PPE");
        var allocation = createAllocation(1L, employee, uniform);
        when(allocationRepository.findById(1L)).thenReturn(Optional.of(allocation));
        when(allocationRepository.save(any(UniformAllocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = allocationService.markReturned(1L);

        assertEquals("RETURNED", result.status());
        assertNotNull(result.returnedDate());
        verify(allocationRepository).save(any(UniformAllocation.class));
    }

    @Test
    void markReturned_shouldThrowWhenNotFound() {
        when(allocationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> allocationService.markReturned(999L));
        verify(allocationRepository, never()).save(any());
    }

    @Test
    void getPendingReturns_shouldReturnAllocatedItems() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var uniform = createUniform(1L, "Safety Vest", "PPE");
        var allocations = List.of(
                createAllocation(1L, employee, uniform),
                createAllocation(2L, employee, uniform)
        );
        when(allocationRepository.findByStatus(AllocationStatus.ALLOCATED)).thenReturn(allocations);

        var result = allocationService.getPendingReturns();

        assertEquals(2, result.size());
        assertEquals("ALLOCATED", result.get(0).status());
        assertEquals("ALLOCATED", result.get(1).status());
    }

    @Test
    void getPendingReturns_shouldReturnEmptyListWhenNonePending() {
        when(allocationRepository.findByStatus(AllocationStatus.ALLOCATED)).thenReturn(List.of());

        var result = allocationService.getPendingReturns();

        assertEquals(0, result.size());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var uniform = createUniform(1L, "Safety Vest", "PPE");
        var allocation = createAllocation(1L, employee, uniform);
        when(allocationRepository.findById(1L)).thenReturn(Optional.of(allocation));

        var result = allocationService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals(1L, result.uniformId());
        assertEquals("Safety Vest", result.uniformName());
        assertEquals("PPE", result.uniformType());
        assertEquals("M", result.uniformSize());
        assertEquals(LocalDate.of(2024, 1, 1), result.allocatedDate());
        assertEquals("ALLOCATED", result.status());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.updatedAt());
    }

    @Test
    void allocate_shouldMapAllFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var uniform = createUniform(2L, "Work Boots", "Footwear");
        var request = new UniformAllocationRequest(1L, 2L, LocalDate.of(2024, 6, 1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(uniformRepository.findById(2L)).thenReturn(Optional.of(uniform));
        when(allocationRepository.save(any(UniformAllocation.class))).thenAnswer(invocation -> {
            UniformAllocation a = invocation.getArgument(0);
            a.setId(1L);
            a.setCreatedAt(LocalDateTime.now());
            a.setUpdatedAt(LocalDateTime.now());
            return a;
        });

        var result = allocationService.allocate(request);

        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("Work Boots", result.uniformName());
        assertEquals("Footwear", result.uniformType());
        assertEquals("M", result.uniformSize());
        assertEquals(LocalDate.of(2024, 6, 1), result.allocatedDate());
        assertEquals("ALLOCATED", result.status());
    }
}
