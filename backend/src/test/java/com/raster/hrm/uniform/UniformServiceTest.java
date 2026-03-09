package com.raster.hrm.uniform;

import com.raster.hrm.uniform.dto.UniformRequest;
import com.raster.hrm.uniform.entity.Uniform;
import com.raster.hrm.uniform.repository.UniformRepository;
import com.raster.hrm.uniform.service.UniformService;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
class UniformServiceTest {

    @Mock
    private UniformRepository uniformRepository;

    @InjectMocks
    private UniformService uniformService;

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

    private UniformRequest createRequest() {
        return new UniformRequest(
                "Safety Vest", "PPE", "L", "High-visibility vest"
        );
    }

    @Test
    void getAll_shouldReturnPageOfUniforms() {
        var uniforms = List.of(
                createUniform(1L, "Safety Vest", "PPE"),
                createUniform(2L, "Work Boots", "Footwear")
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(uniforms, pageable, 2);
        when(uniformRepository.findAll(pageable)).thenReturn(page);

        var result = uniformService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Safety Vest", result.getContent().get(0).name());
        assertEquals("Work Boots", result.getContent().get(1).name());
        verify(uniformRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<Uniform>(List.of(), pageable, 0);
        when(uniformRepository.findAll(pageable)).thenReturn(page);

        var result = uniformService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnUniform() {
        var uniform = createUniform(1L, "Safety Vest", "PPE");
        when(uniformRepository.findById(1L)).thenReturn(Optional.of(uniform));

        var result = uniformService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Safety Vest", result.name());
        assertEquals("PPE", result.type());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(uniformRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> uniformService.getById(999L));
    }

    @Test
    void create_shouldCreateAndReturnUniform() {
        var request = createRequest();
        when(uniformRepository.save(any(Uniform.class))).thenAnswer(invocation -> {
            Uniform u = invocation.getArgument(0);
            u.setId(1L);
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            return u;
        });

        var result = uniformService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Safety Vest", result.name());
        assertEquals("PPE", result.type());
        assertEquals("L", result.size());
        assertEquals("High-visibility vest", result.description());
        verify(uniformRepository).save(any(Uniform.class));
    }

    @Test
    void update_shouldUpdateAndReturnUniform() {
        var uniform = createUniform(1L, "Old Vest", "PPE");
        var request = createRequest();
        when(uniformRepository.findById(1L)).thenReturn(Optional.of(uniform));
        when(uniformRepository.save(any(Uniform.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = uniformService.update(1L, request);

        assertNotNull(result);
        assertEquals("Safety Vest", result.name());
        assertEquals("PPE", result.type());
        assertEquals("L", result.size());
        assertEquals("High-visibility vest", result.description());
        verify(uniformRepository).save(any(Uniform.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var request = createRequest();
        when(uniformRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> uniformService.update(999L, request));
        verify(uniformRepository, never()).save(any());
    }

    @Test
    void deactivate_shouldDeactivateUniform() {
        var uniform = createUniform(1L, "Safety Vest", "PPE");
        when(uniformRepository.findById(1L)).thenReturn(Optional.of(uniform));
        when(uniformRepository.save(any(Uniform.class))).thenAnswer(invocation -> invocation.getArgument(0));

        uniformService.deactivate(1L);

        assertFalse(uniform.isActive());
        verify(uniformRepository).save(uniform);
    }

    @Test
    void deactivate_shouldThrowWhenNotFound() {
        when(uniformRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> uniformService.deactivate(999L));
        verify(uniformRepository, never()).save(any());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var uniform = createUniform(1L, "Safety Vest", "PPE");
        when(uniformRepository.findById(1L)).thenReturn(Optional.of(uniform));

        var result = uniformService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("Safety Vest", result.name());
        assertEquals("PPE", result.type());
        assertEquals("M", result.size());
        assertEquals("Standard uniform", result.description());
        assertTrue(result.active());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.updatedAt());
    }

    @Test
    void create_shouldMapAllFieldsCorrectly() {
        var request = new UniformRequest(
                "Work Boots", "Footwear", "42", "Steel-toe boots"
        );
        when(uniformRepository.save(any(Uniform.class))).thenAnswer(invocation -> {
            Uniform u = invocation.getArgument(0);
            u.setId(1L);
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            return u;
        });

        var result = uniformService.create(request);

        assertEquals("Work Boots", result.name());
        assertEquals("Footwear", result.type());
        assertEquals("42", result.size());
        assertEquals("Steel-toe boots", result.description());
    }
}
