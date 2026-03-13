package com.raster.hrm.tds;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.tds.dto.ProfessionalTaxSlabRequest;
import com.raster.hrm.tds.entity.ProfessionalTaxSlab;
import com.raster.hrm.tds.repository.ProfessionalTaxSlabRepository;
import com.raster.hrm.tds.service.ProfessionalTaxSlabService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalTaxSlabServiceTest {

    @Mock
    private ProfessionalTaxSlabRepository professionalTaxSlabRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ProfessionalTaxSlabService professionalTaxSlabService;

    private ProfessionalTaxSlab createPTSlab(Long id, String state, BigDecimal from, BigDecimal to,
                                              BigDecimal monthlyTax, BigDecimal febTax) {
        var slab = new ProfessionalTaxSlab();
        slab.setId(id);
        slab.setState(state);
        slab.setSlabFrom(from);
        slab.setSlabTo(to);
        slab.setMonthlyTax(monthlyTax);
        slab.setFebruaryTax(febTax);
        slab.setActive(true);
        slab.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        slab.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return slab;
    }

    private Employee createEmployee(Long id, String state, BigDecimal basicSalary) {
        var employee = new Employee();
        employee.setId(id);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setState(state);
        employee.setBasicSalary(basicSalary);
        return employee;
    }

    @Test
    void getAll_shouldReturnPageOfSlabs() {
        var slabs = List.of(
                createPTSlab(1L, "Karnataka", new BigDecimal("0"), new BigDecimal("15000"), new BigDecimal("0"), null),
                createPTSlab(2L, "Karnataka", new BigDecimal("15001"), new BigDecimal("25000"), new BigDecimal("200"), new BigDecimal("300"))
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(slabs, pageable, 2);
        when(professionalTaxSlabRepository.findAll(pageable)).thenReturn(page);

        var result = professionalTaxSlabService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Karnataka", result.getContent().get(0).state());
        verify(professionalTaxSlabRepository).findAll(pageable);
    }

    @Test
    void getById_shouldReturnSlab() {
        var slab = createPTSlab(1L, "Karnataka", new BigDecimal("15001"), new BigDecimal("25000"),
                new BigDecimal("200"), new BigDecimal("300"));
        when(professionalTaxSlabRepository.findById(1L)).thenReturn(Optional.of(slab));

        var result = professionalTaxSlabService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Karnataka", result.state());
        assertEquals(new BigDecimal("200"), result.monthlyTax());
        assertEquals(new BigDecimal("300"), result.februaryTax());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(professionalTaxSlabRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> professionalTaxSlabService.getById(999L));
    }

    @Test
    void getByState_shouldReturnSlabs() {
        var slabs = List.of(
                createPTSlab(1L, "Karnataka", new BigDecimal("0"), new BigDecimal("15000"), new BigDecimal("0"), null)
        );
        when(professionalTaxSlabRepository.findByState("Karnataka")).thenReturn(slabs);

        var result = professionalTaxSlabService.getByState("Karnataka");

        assertEquals(1, result.size());
        assertEquals("Karnataka", result.get(0).state());
    }

    @Test
    void create_shouldCreateAndReturnSlab() {
        var request = new ProfessionalTaxSlabRequest("Karnataka",
                new BigDecimal("15001"), new BigDecimal("25000"),
                new BigDecimal("200"), new BigDecimal("300"));
        when(professionalTaxSlabRepository.existsByStateAndSlabFrom("Karnataka", new BigDecimal("15001")))
                .thenReturn(false);
        when(professionalTaxSlabRepository.save(any(ProfessionalTaxSlab.class))).thenAnswer(invocation -> {
            ProfessionalTaxSlab s = invocation.getArgument(0);
            s.setId(1L);
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        var result = professionalTaxSlabService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Karnataka", result.state());
        assertEquals(new BigDecimal("200"), result.monthlyTax());
        verify(professionalTaxSlabRepository).save(any(ProfessionalTaxSlab.class));
    }

    @Test
    void create_shouldThrowWhenDuplicate() {
        var request = new ProfessionalTaxSlabRequest("Karnataka",
                new BigDecimal("15001"), new BigDecimal("25000"),
                new BigDecimal("200"), new BigDecimal("300"));
        when(professionalTaxSlabRepository.existsByStateAndSlabFrom("Karnataka", new BigDecimal("15001")))
                .thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> professionalTaxSlabService.create(request));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(professionalTaxSlabRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateAndReturnSlab() {
        var slab = createPTSlab(1L, "Karnataka", new BigDecimal("15001"), new BigDecimal("25000"),
                new BigDecimal("200"), new BigDecimal("300"));
        var request = new ProfessionalTaxSlabRequest("Karnataka",
                new BigDecimal("15001"), new BigDecimal("25000"),
                new BigDecimal("250"), new BigDecimal("350"));
        when(professionalTaxSlabRepository.findById(1L)).thenReturn(Optional.of(slab));
        when(professionalTaxSlabRepository.save(any(ProfessionalTaxSlab.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = professionalTaxSlabService.update(1L, request);

        assertNotNull(result);
        assertEquals(new BigDecimal("250"), result.monthlyTax());
        assertEquals(new BigDecimal("350"), result.februaryTax());
        verify(professionalTaxSlabRepository).save(any(ProfessionalTaxSlab.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var request = new ProfessionalTaxSlabRequest("Karnataka",
                new BigDecimal("15001"), new BigDecimal("25000"),
                new BigDecimal("200"), new BigDecimal("300"));
        when(professionalTaxSlabRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> professionalTaxSlabService.update(999L, request));
        verify(professionalTaxSlabRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteSlab() {
        var slab = createPTSlab(1L, "Karnataka", new BigDecimal("15001"), new BigDecimal("25000"),
                new BigDecimal("200"), new BigDecimal("300"));
        when(professionalTaxSlabRepository.findById(1L)).thenReturn(Optional.of(slab));

        professionalTaxSlabService.delete(1L);

        verify(professionalTaxSlabRepository).delete(slab);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(professionalTaxSlabRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> professionalTaxSlabService.delete(999L));
        verify(professionalTaxSlabRepository, never()).delete(any());
    }

    @Test
    void computeProfessionalTax_shouldReturnTaxForMatchingSlab() {
        var employee = createEmployee(1L, "Karnataka", new BigDecimal("20000"));
        var slab = createPTSlab(1L, "Karnataka", new BigDecimal("15001"), new BigDecimal("25000"),
                new BigDecimal("200"), new BigDecimal("300"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(professionalTaxSlabRepository.findByStateAndActiveOrderBySlabFromAsc("Karnataka", true))
                .thenReturn(List.of(slab));

        var result = professionalTaxSlabService.computeProfessionalTax(1L, 5);

        assertEquals(new BigDecimal("200.00"), result);
    }

    @Test
    void computeProfessionalTax_shouldReturnFebruaryTaxForFeb() {
        var employee = createEmployee(1L, "Karnataka", new BigDecimal("20000"));
        var slab = createPTSlab(1L, "Karnataka", new BigDecimal("15001"), new BigDecimal("25000"),
                new BigDecimal("200"), new BigDecimal("300"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(professionalTaxSlabRepository.findByStateAndActiveOrderBySlabFromAsc("Karnataka", true))
                .thenReturn(List.of(slab));

        var result = professionalTaxSlabService.computeProfessionalTax(1L, 2);

        assertEquals(new BigDecimal("300.00"), result);
    }

    @Test
    void computeProfessionalTax_shouldReturnZeroWhenNoSlabs() {
        var employee = createEmployee(1L, "Karnataka", new BigDecimal("20000"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(professionalTaxSlabRepository.findByStateAndActiveOrderBySlabFromAsc("Karnataka", true))
                .thenReturn(List.of());

        var result = professionalTaxSlabService.computeProfessionalTax(1L, 5);

        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void computeProfessionalTax_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> professionalTaxSlabService.computeProfessionalTax(999L, 5));
    }

    @Test
    void computeProfessionalTax_shouldThrowWhenStateNotSet() {
        var employee = createEmployee(1L, null, new BigDecimal("20000"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(BadRequestException.class,
                () -> professionalTaxSlabService.computeProfessionalTax(1L, 5));
    }
}
