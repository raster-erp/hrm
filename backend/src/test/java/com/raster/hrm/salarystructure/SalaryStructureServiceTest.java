package com.raster.hrm.salarystructure;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.salarycomponent.entity.SalaryComponent;
import com.raster.hrm.salarycomponent.entity.SalaryComponentType;
import com.raster.hrm.salarycomponent.entity.SalaryComputationType;
import com.raster.hrm.salarycomponent.repository.SalaryComponentRepository;
import com.raster.hrm.salarystructure.dto.SalaryStructureComponentRequest;
import com.raster.hrm.salarystructure.dto.SalaryStructureRequest;
import com.raster.hrm.salarystructure.entity.SalaryStructure;
import com.raster.hrm.salarystructure.entity.SalaryStructureComponent;
import com.raster.hrm.salarystructure.repository.SalaryStructureComponentRepository;
import com.raster.hrm.salarystructure.repository.SalaryStructureRepository;
import com.raster.hrm.salarystructure.service.SalaryStructureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
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
class SalaryStructureServiceTest {

    @Mock
    private SalaryStructureRepository salaryStructureRepository;

    @Mock
    private SalaryStructureComponentRepository structureComponentRepository;

    @Mock
    private SalaryComponentRepository salaryComponentRepository;

    @InjectMocks
    private SalaryStructureService salaryStructureService;

    private SalaryComponent createSalaryComponent(Long id, String code, String name) {
        var component = new SalaryComponent();
        component.setId(id);
        component.setCode(code);
        component.setName(name);
        component.setType(SalaryComponentType.EARNING);
        component.setComputationType(SalaryComputationType.FIXED);
        component.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        component.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return component;
    }

    private SalaryStructure createStructure(Long id, String code, String name) {
        var structure = new SalaryStructure();
        structure.setId(id);
        structure.setCode(code);
        structure.setName(name);
        structure.setDescription("Test structure");
        structure.setActive(true);
        structure.setComponents(new ArrayList<>());
        structure.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        structure.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return structure;
    }

    private SalaryStructure createStructureWithComponents(Long id, String code, String name) {
        var structure = createStructure(id, code, name);
        var salaryComponent = createSalaryComponent(1L, "BASIC", "Basic Salary");

        var structComp = new SalaryStructureComponent();
        structComp.setId(1L);
        structComp.setSalaryStructure(structure);
        structComp.setSalaryComponent(salaryComponent);
        structComp.setComputationType(SalaryComputationType.FIXED);
        structComp.setFixedAmount(new BigDecimal("10000.00"));
        structComp.setSortOrder(0);
        structComp.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));

        structure.getComponents().add(structComp);
        return structure;
    }

    @Test
    void getAll_shouldReturnPageOfStructures() {
        var structures = List.of(
                createStructure(1L, "STD", "Standard"),
                createStructure(2L, "MGR", "Manager")
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(structures, pageable, 2);
        when(salaryStructureRepository.findAll(pageable)).thenReturn(page);

        var result = salaryStructureService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Standard", result.getContent().get(0).name());
        verify(salaryStructureRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<SalaryStructure>(List.of(), pageable, 0);
        when(salaryStructureRepository.findAll(pageable)).thenReturn(page);

        var result = salaryStructureService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnStructure() {
        var structure = createStructure(1L, "STD", "Standard");
        when(salaryStructureRepository.findById(1L)).thenReturn(Optional.of(structure));

        var result = salaryStructureService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("STD", result.code());
        assertEquals("Standard", result.name());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(salaryStructureRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salaryStructureService.getById(999L));
    }

    @Test
    void getActive_shouldReturnActiveStructures() {
        var structures = List.of(createStructure(1L, "STD", "Standard"));
        when(salaryStructureRepository.findByActive(true)).thenReturn(structures);

        var result = salaryStructureService.getActive();

        assertEquals(1, result.size());
        assertTrue(result.get(0).active());
    }

    @Test
    void create_shouldCreateStructureWithoutComponents() {
        var request = new SalaryStructureRequest("STD", "Standard", "Test structure", null);
        when(salaryStructureRepository.existsByCode("STD")).thenReturn(false);
        when(salaryStructureRepository.save(any(SalaryStructure.class))).thenAnswer(invocation -> {
            SalaryStructure ss = invocation.getArgument(0);
            ss.setId(1L);
            ss.setCreatedAt(LocalDateTime.now());
            ss.setUpdatedAt(LocalDateTime.now());
            return ss;
        });

        var result = salaryStructureService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("STD", result.code());
        verify(salaryStructureRepository).save(any(SalaryStructure.class));
    }

    @Test
    void create_shouldCreateStructureWithComponents() {
        var compRequest = new SalaryStructureComponentRequest(1L, "FIXED", null, new BigDecimal("10000.00"), 0);
        var request = new SalaryStructureRequest("STD", "Standard", "Test", List.of(compRequest));
        var salaryComponent = createSalaryComponent(1L, "BASIC", "Basic Salary");

        when(salaryStructureRepository.existsByCode("STD")).thenReturn(false);
        when(salaryComponentRepository.findById(1L)).thenReturn(Optional.of(salaryComponent));
        when(salaryStructureRepository.save(any(SalaryStructure.class))).thenAnswer(invocation -> {
            SalaryStructure ss = invocation.getArgument(0);
            ss.setId(1L);
            ss.setCreatedAt(LocalDateTime.now());
            ss.setUpdatedAt(LocalDateTime.now());
            for (var comp : ss.getComponents()) {
                comp.setId(1L);
                comp.setCreatedAt(LocalDateTime.now());
            }
            return ss;
        });

        var result = salaryStructureService.create(request);

        assertNotNull(result);
        assertEquals(1, result.components().size());
        assertEquals("BASIC", result.components().get(0).salaryComponentCode());
    }

    @Test
    void create_shouldThrowWhenCodeExists() {
        var request = new SalaryStructureRequest("STD", "Standard", "Test", null);
        when(salaryStructureRepository.existsByCode("STD")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> salaryStructureService.create(request));
        assertTrue(ex.getMessage().contains("STD"));
        verify(salaryStructureRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateStructure() {
        var structure = createStructure(1L, "STD", "Standard");
        var request = new SalaryStructureRequest("STD", "Updated Standard", "Updated", null);
        when(salaryStructureRepository.findById(1L)).thenReturn(Optional.of(structure));
        when(salaryStructureRepository.save(any(SalaryStructure.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = salaryStructureService.update(1L, request);

        assertEquals("Updated Standard", result.name());
        verify(salaryStructureRepository).save(any(SalaryStructure.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var request = new SalaryStructureRequest("STD", "Standard", "Test", null);
        when(salaryStructureRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salaryStructureService.update(999L, request));
    }

    @Test
    void update_shouldThrowWhenNewCodeExists() {
        var structure = createStructure(1L, "STD", "Standard");
        var request = new SalaryStructureRequest("MGR", "Manager", "Test", null);
        when(salaryStructureRepository.findById(1L)).thenReturn(Optional.of(structure));
        when(salaryStructureRepository.existsByCode("MGR")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> salaryStructureService.update(1L, request));
        assertTrue(ex.getMessage().contains("MGR"));
    }

    @Test
    void clone_shouldCloneStructure() {
        var source = createStructureWithComponents(1L, "STD", "Standard");
        when(salaryStructureRepository.findById(1L)).thenReturn(Optional.of(source));
        when(salaryStructureRepository.existsByCode("STD2")).thenReturn(false);
        when(salaryStructureRepository.save(any(SalaryStructure.class))).thenAnswer(invocation -> {
            SalaryStructure ss = invocation.getArgument(0);
            ss.setId(2L);
            ss.setCreatedAt(LocalDateTime.now());
            ss.setUpdatedAt(LocalDateTime.now());
            for (var comp : ss.getComponents()) {
                comp.setId(2L);
                comp.setCreatedAt(LocalDateTime.now());
            }
            return ss;
        });

        var result = salaryStructureService.clone(1L, "STD2", "Standard Copy");

        assertNotNull(result);
        assertEquals("STD2", result.code());
        assertEquals("Standard Copy", result.name());
        assertEquals(1, result.components().size());
    }

    @Test
    void clone_shouldThrowWhenSourceNotFound() {
        when(salaryStructureRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salaryStructureService.clone(999L, "NEW", "New"));
    }

    @Test
    void clone_shouldThrowWhenNewCodeExists() {
        var source = createStructure(1L, "STD", "Standard");
        when(salaryStructureRepository.findById(1L)).thenReturn(Optional.of(source));
        when(salaryStructureRepository.existsByCode("STD")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> salaryStructureService.clone(1L, "STD", "Standard Copy"));
        assertTrue(ex.getMessage().contains("STD"));
    }

    @Test
    void updateActive_shouldUpdateActiveStatus() {
        var structure = createStructure(1L, "STD", "Standard");
        when(salaryStructureRepository.findById(1L)).thenReturn(Optional.of(structure));
        when(salaryStructureRepository.save(any(SalaryStructure.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = salaryStructureService.updateActive(1L, false);

        assertFalse(result.active());
        verify(salaryStructureRepository).save(any(SalaryStructure.class));
    }

    @Test
    void updateActive_shouldThrowWhenNotFound() {
        when(salaryStructureRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salaryStructureService.updateActive(999L, false));
    }

    @Test
    void delete_shouldDeleteStructure() {
        var structure = createStructure(1L, "STD", "Standard");
        when(salaryStructureRepository.findById(1L)).thenReturn(Optional.of(structure));

        salaryStructureService.delete(1L);

        verify(salaryStructureRepository).delete(structure);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(salaryStructureRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salaryStructureService.delete(999L));
        verify(salaryStructureRepository, never()).delete(any());
    }
}
