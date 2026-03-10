package com.raster.hrm.rotationpattern;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.rotationpattern.dto.RotationPatternRequest;
import com.raster.hrm.rotationpattern.entity.RotationPattern;
import com.raster.hrm.rotationpattern.repository.RotationPatternRepository;
import com.raster.hrm.rotationpattern.service.RotationPatternService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RotationPatternServiceTest {

    @Mock
    private RotationPatternRepository rotationPatternRepository;

    @InjectMocks
    private RotationPatternService rotationPatternService;

    private RotationPattern createPattern(Long id, String name) {
        var pattern = new RotationPattern();
        pattern.setId(id);
        pattern.setName(name);
        pattern.setDescription("Test rotation pattern");
        pattern.setRotationDays(7);
        pattern.setShiftSequence("1,2,3,1,2,3,1");
        pattern.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        pattern.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return pattern;
    }

    private RotationPatternRequest createRequest() {
        return new RotationPatternRequest(
                "Weekly Rotation",
                "Rotates weekly",
                7,
                "1,2,3,1,2,3,1"
        );
    }

    @Test
    void getAll_shouldReturnPageOfPatterns() {
        var patterns = List.of(
                createPattern(1L, "Weekly Rotation"),
                createPattern(2L, "Bi-Weekly Rotation")
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(patterns, pageable, 2);
        when(rotationPatternRepository.findAll(pageable)).thenReturn(page);

        var result = rotationPatternService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Weekly Rotation", result.getContent().get(0).name());
        assertEquals("Bi-Weekly Rotation", result.getContent().get(1).name());
        verify(rotationPatternRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<RotationPattern>(List.of(), pageable, 0);
        when(rotationPatternRepository.findAll(pageable)).thenReturn(page);

        var result = rotationPatternService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnPattern() {
        var pattern = createPattern(1L, "Weekly Rotation");
        when(rotationPatternRepository.findById(1L)).thenReturn(Optional.of(pattern));

        var result = rotationPatternService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Weekly Rotation", result.name());
        assertEquals("Test rotation pattern", result.description());
        assertEquals(7, result.rotationDays());
        assertEquals("1,2,3,1,2,3,1", result.shiftSequence());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(rotationPatternRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rotationPatternService.getById(999L));
    }

    @Test
    void create_shouldCreateAndReturnPattern() {
        var request = createRequest();
        when(rotationPatternRepository.existsByName("Weekly Rotation")).thenReturn(false);
        when(rotationPatternRepository.save(any(RotationPattern.class))).thenAnswer(invocation -> {
            RotationPattern p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        var result = rotationPatternService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Weekly Rotation", result.name());
        assertEquals("Rotates weekly", result.description());
        assertEquals(7, result.rotationDays());
        verify(rotationPatternRepository).save(any(RotationPattern.class));
    }

    @Test
    void create_shouldThrowWhenNameExists() {
        var request = createRequest();
        when(rotationPatternRepository.existsByName("Weekly Rotation")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> rotationPatternService.create(request));
        assertTrue(ex.getMessage().contains("Weekly Rotation"));
        verify(rotationPatternRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateAndReturnPattern() {
        var pattern = createPattern(1L, "Weekly Rotation");
        var request = new RotationPatternRequest("Updated Pattern", "Updated desc", 14, "1,2,1,2");
        when(rotationPatternRepository.findById(1L)).thenReturn(Optional.of(pattern));
        when(rotationPatternRepository.save(any(RotationPattern.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = rotationPatternService.update(1L, request);

        assertNotNull(result);
        assertEquals("Updated Pattern", result.name());
        assertEquals("Updated desc", result.description());
        assertEquals(14, result.rotationDays());
        assertEquals("1,2,1,2", result.shiftSequence());
        verify(rotationPatternRepository).save(any(RotationPattern.class));
    }

    @Test
    void update_shouldThrowWhenPatternNotFound() {
        var request = createRequest();
        when(rotationPatternRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rotationPatternService.update(999L, request));
        verify(rotationPatternRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNewNameExists() {
        var pattern = createPattern(1L, "Weekly Rotation");
        var request = new RotationPatternRequest("Bi-Weekly", "desc", 14, "1,2");
        when(rotationPatternRepository.findById(1L)).thenReturn(Optional.of(pattern));
        when(rotationPatternRepository.existsByName("Bi-Weekly")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> rotationPatternService.update(1L, request));
        assertTrue(ex.getMessage().contains("Bi-Weekly"));
        verify(rotationPatternRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameName() {
        var pattern = createPattern(1L, "Weekly Rotation");
        var request = new RotationPatternRequest("Weekly Rotation", "Updated desc", 14, "1,2");
        when(rotationPatternRepository.findById(1L)).thenReturn(Optional.of(pattern));
        when(rotationPatternRepository.save(any(RotationPattern.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = rotationPatternService.update(1L, request);

        assertEquals("Weekly Rotation", result.name());
        verify(rotationPatternRepository).save(any(RotationPattern.class));
    }

    @Test
    void delete_shouldDeletePattern() {
        var pattern = createPattern(1L, "Weekly Rotation");
        when(rotationPatternRepository.findById(1L)).thenReturn(Optional.of(pattern));

        rotationPatternService.delete(1L);

        verify(rotationPatternRepository).delete(pattern);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(rotationPatternRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rotationPatternService.delete(999L));
        verify(rotationPatternRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var pattern = createPattern(1L, "Weekly Rotation");
        when(rotationPatternRepository.findById(1L)).thenReturn(Optional.of(pattern));

        var result = rotationPatternService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("Weekly Rotation", result.name());
        assertEquals("Test rotation pattern", result.description());
        assertEquals(7, result.rotationDays());
        assertEquals("1,2,3,1,2,3,1", result.shiftSequence());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.updatedAt());
    }
}
