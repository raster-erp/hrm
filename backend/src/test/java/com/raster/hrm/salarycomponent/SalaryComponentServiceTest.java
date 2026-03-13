package com.raster.hrm.salarycomponent;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.salarycomponent.dto.SalaryComponentRequest;
import com.raster.hrm.salarycomponent.entity.SalaryComponent;
import com.raster.hrm.salarycomponent.entity.SalaryComponentType;
import com.raster.hrm.salarycomponent.entity.SalaryComputationType;
import com.raster.hrm.salarycomponent.repository.SalaryComponentRepository;
import com.raster.hrm.salarycomponent.service.SalaryComponentService;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalaryComponentServiceTest {

    @Mock
    private SalaryComponentRepository salaryComponentRepository;

    @InjectMocks
    private SalaryComponentService salaryComponentService;

    private SalaryComponent createComponent(Long id, String code, String name,
                                             SalaryComponentType type, SalaryComputationType computationType) {
        var component = new SalaryComponent();
        component.setId(id);
        component.setCode(code);
        component.setName(name);
        component.setType(type);
        component.setComputationType(computationType);
        component.setPercentageValue(new BigDecimal("50.00"));
        component.setTaxable(true);
        component.setMandatory(false);
        component.setDescription("Test component");
        component.setActive(true);
        component.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        component.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return component;
    }

    private SalaryComponentRequest createRequest() {
        return new SalaryComponentRequest(
                "BASIC", "Basic Salary", "EARNING", "FIXED",
                null, true, true, "Basic salary component"
        );
    }

    @Test
    void getAll_shouldReturnPageOfComponents() {
        var components = List.of(
                createComponent(1L, "BASIC", "Basic Salary", SalaryComponentType.EARNING, SalaryComputationType.FIXED),
                createComponent(2L, "HRA", "House Rent Allowance", SalaryComponentType.EARNING, SalaryComputationType.PERCENTAGE_OF_BASIC)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(components, pageable, 2);
        when(salaryComponentRepository.findAll(pageable)).thenReturn(page);

        var result = salaryComponentService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Basic Salary", result.getContent().get(0).name());
        assertEquals("House Rent Allowance", result.getContent().get(1).name());
        verify(salaryComponentRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<SalaryComponent>(List.of(), pageable, 0);
        when(salaryComponentRepository.findAll(pageable)).thenReturn(page);

        var result = salaryComponentService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnComponent() {
        var component = createComponent(1L, "BASIC", "Basic Salary", SalaryComponentType.EARNING, SalaryComputationType.FIXED);
        when(salaryComponentRepository.findById(1L)).thenReturn(Optional.of(component));

        var result = salaryComponentService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("BASIC", result.code());
        assertEquals("Basic Salary", result.name());
        assertEquals("EARNING", result.type());
        assertEquals("FIXED", result.computationType());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(salaryComponentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salaryComponentService.getById(999L));
    }

    @Test
    void getByType_shouldReturnComponents() {
        var components = List.of(createComponent(1L, "BASIC", "Basic Salary", SalaryComponentType.EARNING, SalaryComputationType.FIXED));
        when(salaryComponentRepository.findByType(SalaryComponentType.EARNING)).thenReturn(components);

        var result = salaryComponentService.getByType(SalaryComponentType.EARNING);

        assertEquals(1, result.size());
        assertEquals("EARNING", result.get(0).type());
    }

    @Test
    void getByType_shouldReturnEmptyList() {
        when(salaryComponentRepository.findByType(SalaryComponentType.DEDUCTION)).thenReturn(List.of());

        var result = salaryComponentService.getByType(SalaryComponentType.DEDUCTION);

        assertEquals(0, result.size());
    }

    @Test
    void getActive_shouldReturnActiveComponents() {
        var components = List.of(createComponent(1L, "BASIC", "Basic Salary", SalaryComponentType.EARNING, SalaryComputationType.FIXED));
        when(salaryComponentRepository.findByActive(true)).thenReturn(components);

        var result = salaryComponentService.getActive();

        assertEquals(1, result.size());
        assertTrue(result.get(0).active());
    }

    @Test
    void create_shouldCreateAndReturnComponent() {
        var request = createRequest();
        when(salaryComponentRepository.existsByCode("BASIC")).thenReturn(false);
        when(salaryComponentRepository.save(any(SalaryComponent.class))).thenAnswer(invocation -> {
            SalaryComponent sc = invocation.getArgument(0);
            sc.setId(1L);
            sc.setCreatedAt(LocalDateTime.now());
            sc.setUpdatedAt(LocalDateTime.now());
            return sc;
        });

        var result = salaryComponentService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("BASIC", result.code());
        assertEquals("Basic Salary", result.name());
        assertEquals("EARNING", result.type());
        assertEquals("FIXED", result.computationType());
        assertTrue(result.taxable());
        assertTrue(result.mandatory());
        verify(salaryComponentRepository).save(any(SalaryComponent.class));
    }

    @Test
    void create_shouldThrowWhenCodeExists() {
        var request = createRequest();
        when(salaryComponentRepository.existsByCode("BASIC")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> salaryComponentService.create(request));
        assertTrue(ex.getMessage().contains("BASIC"));
        verify(salaryComponentRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateAndReturnComponent() {
        var component = createComponent(1L, "BASIC", "Basic Salary", SalaryComponentType.EARNING, SalaryComputationType.FIXED);
        var request = new SalaryComponentRequest("BASIC", "Updated Basic", "EARNING", "FIXED",
                null, true, true, "Updated");
        when(salaryComponentRepository.findById(1L)).thenReturn(Optional.of(component));
        when(salaryComponentRepository.save(any(SalaryComponent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = salaryComponentService.update(1L, request);

        assertNotNull(result);
        assertEquals("BASIC", result.code());
        assertEquals("Updated Basic", result.name());
        verify(salaryComponentRepository).save(any(SalaryComponent.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var request = createRequest();
        when(salaryComponentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salaryComponentService.update(999L, request));
        verify(salaryComponentRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNewCodeAlreadyExists() {
        var component = createComponent(1L, "BASIC", "Basic Salary", SalaryComponentType.EARNING, SalaryComputationType.FIXED);
        var request = new SalaryComponentRequest("HRA", "HRA", "EARNING", "PERCENTAGE_OF_BASIC",
                new BigDecimal("50.00"), true, false, null);
        when(salaryComponentRepository.findById(1L)).thenReturn(Optional.of(component));
        when(salaryComponentRepository.existsByCode("HRA")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> salaryComponentService.update(1L, request));
        assertTrue(ex.getMessage().contains("HRA"));
        verify(salaryComponentRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameCode() {
        var component = createComponent(1L, "BASIC", "Basic Salary", SalaryComponentType.EARNING, SalaryComputationType.FIXED);
        var request = new SalaryComponentRequest("BASIC", "Updated Basic", "EARNING", "FIXED",
                null, true, true, "Updated");
        when(salaryComponentRepository.findById(1L)).thenReturn(Optional.of(component));
        when(salaryComponentRepository.save(any(SalaryComponent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = salaryComponentService.update(1L, request);

        assertEquals("BASIC", result.code());
        verify(salaryComponentRepository).save(any(SalaryComponent.class));
    }

    @Test
    void updateActive_shouldUpdateActiveStatus() {
        var component = createComponent(1L, "BASIC", "Basic Salary", SalaryComponentType.EARNING, SalaryComputationType.FIXED);
        when(salaryComponentRepository.findById(1L)).thenReturn(Optional.of(component));
        when(salaryComponentRepository.save(any(SalaryComponent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = salaryComponentService.updateActive(1L, false);

        assertFalse(result.active());
        verify(salaryComponentRepository).save(any(SalaryComponent.class));
    }

    @Test
    void updateActive_shouldThrowWhenNotFound() {
        when(salaryComponentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salaryComponentService.updateActive(999L, false));
        verify(salaryComponentRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteComponent() {
        var component = createComponent(1L, "BASIC", "Basic Salary", SalaryComponentType.EARNING, SalaryComputationType.FIXED);
        when(salaryComponentRepository.findById(1L)).thenReturn(Optional.of(component));

        salaryComponentService.delete(1L);

        verify(salaryComponentRepository).delete(component);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(salaryComponentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salaryComponentService.delete(999L));
        verify(salaryComponentRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapAllResponseFieldsCorrectly() {
        var component = createComponent(1L, "BASIC", "Basic Salary", SalaryComponentType.EARNING, SalaryComputationType.FIXED);
        when(salaryComponentRepository.findById(1L)).thenReturn(Optional.of(component));

        var result = salaryComponentService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("BASIC", result.code());
        assertEquals("Basic Salary", result.name());
        assertEquals("EARNING", result.type());
        assertEquals("FIXED", result.computationType());
        assertEquals(new BigDecimal("50.00"), result.percentageValue());
        assertTrue(result.taxable());
        assertFalse(result.mandatory());
        assertEquals("Test component", result.description());
        assertTrue(result.active());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.updatedAt());
    }
}
