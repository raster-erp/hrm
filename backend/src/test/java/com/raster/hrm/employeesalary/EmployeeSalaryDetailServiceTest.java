package com.raster.hrm.employeesalary;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.employeesalary.dto.EmployeeSalaryDetailRequest;
import com.raster.hrm.employeesalary.dto.SalaryRevisionRequest;
import com.raster.hrm.employeesalary.entity.EmployeeSalaryDetail;
import com.raster.hrm.employeesalary.repository.EmployeeSalaryDetailRepository;
import com.raster.hrm.employeesalary.service.EmployeeSalaryDetailService;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.salarystructure.entity.SalaryStructure;
import com.raster.hrm.salarystructure.repository.SalaryStructureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeSalaryDetailServiceTest {

    @Mock
    private EmployeeSalaryDetailRepository employeeSalaryDetailRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryStructureRepository salaryStructureRepository;

    @InjectMocks
    private EmployeeSalaryDetailService employeeSalaryDetailService;

    private Employee createEmployee(Long id) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        return employee;
    }

    private SalaryStructure createSalaryStructure(Long id) {
        var structure = new SalaryStructure();
        structure.setId(id);
        structure.setCode("STD");
        structure.setName("Standard");
        structure.setComponents(new ArrayList<>());
        structure.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        structure.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return structure;
    }

    private EmployeeSalaryDetail createDetail(Long id, Employee employee, SalaryStructure structure) {
        var detail = new EmployeeSalaryDetail();
        detail.setId(id);
        detail.setEmployee(employee);
        detail.setSalaryStructure(structure);
        detail.setCtc(new BigDecimal("1200000.00"));
        detail.setBasicSalary(new BigDecimal("50000.00"));
        detail.setEffectiveDate(LocalDate.of(2024, 4, 1));
        detail.setNotes("Initial assignment");
        detail.setActive(true);
        detail.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        detail.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return detail;
    }

    @Test
    void getAll_shouldReturnPageOfDetails() {
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var details = List.of(createDetail(1L, employee, structure));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(details, pageable, 1);
        when(employeeSalaryDetailRepository.findAll(pageable)).thenReturn(page);

        var result = employeeSalaryDetailService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).employeeName());
    }

    @Test
    void getById_shouldReturnDetail() {
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var detail = createDetail(1L, employee, structure);
        when(employeeSalaryDetailRepository.findById(1L)).thenReturn(Optional.of(detail));

        var result = employeeSalaryDetailService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("John Doe", result.employeeName());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("Standard", result.salaryStructureName());
        assertEquals(new BigDecimal("1200000.00"), result.ctc());
        assertEquals(new BigDecimal("50000.00"), result.basicSalary());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(employeeSalaryDetailRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeSalaryDetailService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnDetails() {
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var details = List.of(createDetail(1L, employee, structure));
        when(employeeSalaryDetailRepository.findByEmployeeIdOrderByEffectiveDateDesc(1L)).thenReturn(details);

        var result = employeeSalaryDetailService.getByEmployeeId(1L);

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).employeeName());
    }

    @Test
    void create_shouldCreateAndReturnDetail() {
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var request = new EmployeeSalaryDetailRequest(1L, 1L,
                new BigDecimal("1200000.00"), new BigDecimal("50000.00"),
                LocalDate.of(2024, 4, 1), "Initial");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryStructureRepository.findById(1L)).thenReturn(Optional.of(structure));
        when(employeeSalaryDetailRepository.save(any(EmployeeSalaryDetail.class))).thenAnswer(invocation -> {
            EmployeeSalaryDetail d = invocation.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(LocalDateTime.now());
            d.setUpdatedAt(LocalDateTime.now());
            return d;
        });

        var result = employeeSalaryDetailService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("John Doe", result.employeeName());
        assertEquals(new BigDecimal("1200000.00"), result.ctc());
        verify(employeeSalaryDetailRepository).save(any(EmployeeSalaryDetail.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var request = new EmployeeSalaryDetailRequest(999L, 1L,
                new BigDecimal("1200000.00"), new BigDecimal("50000.00"),
                LocalDate.of(2024, 4, 1), null);

        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeSalaryDetailService.create(request));
        verify(employeeSalaryDetailRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenStructureNotFound() {
        var employee = createEmployee(1L);
        var request = new EmployeeSalaryDetailRequest(1L, 999L,
                new BigDecimal("1200000.00"), new BigDecimal("50000.00"),
                LocalDate.of(2024, 4, 1), null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryStructureRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeSalaryDetailService.create(request));
        verify(employeeSalaryDetailRepository, never()).save(any());
    }

    @Test
    void revise_shouldDeactivateOldAndCreateNew() {
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var existingDetail = createDetail(1L, employee, structure);
        var request = new SalaryRevisionRequest(1L, new BigDecimal("1500000.00"),
                new BigDecimal("60000.00"), LocalDate.of(2025, 4, 1), "Promotion revision");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryStructureRepository.findById(1L)).thenReturn(Optional.of(structure));
        when(employeeSalaryDetailRepository.findByEmployeeIdOrderByEffectiveDateDesc(1L))
                .thenReturn(List.of(existingDetail));
        when(employeeSalaryDetailRepository.save(any(EmployeeSalaryDetail.class))).thenAnswer(invocation -> {
            EmployeeSalaryDetail d = invocation.getArgument(0);
            if (d.getId() == null) {
                d.setId(2L);
                d.setCreatedAt(LocalDateTime.now());
                d.setUpdatedAt(LocalDateTime.now());
            }
            return d;
        });

        var result = employeeSalaryDetailService.revise(1L, request);

        assertNotNull(result);
        assertEquals(new BigDecimal("1500000.00"), result.ctc());
        assertEquals(new BigDecimal("60000.00"), result.basicSalary());
        assertFalse(existingDetail.isActive());
    }

    @Test
    void revise_shouldThrowWhenEmployeeNotFound() {
        var request = new SalaryRevisionRequest(1L, new BigDecimal("1500000.00"),
                new BigDecimal("60000.00"), LocalDate.of(2025, 4, 1), null);

        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeSalaryDetailService.revise(999L, request));
    }

    @Test
    void delete_shouldDeleteDetail() {
        var employee = createEmployee(1L);
        var structure = createSalaryStructure(1L);
        var detail = createDetail(1L, employee, structure);
        when(employeeSalaryDetailRepository.findById(1L)).thenReturn(Optional.of(detail));

        employeeSalaryDetailService.delete(1L);

        verify(employeeSalaryDetailRepository).delete(detail);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(employeeSalaryDetailRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeSalaryDetailService.delete(999L));
        verify(employeeSalaryDetailRepository, never()).delete(any());
    }
}
