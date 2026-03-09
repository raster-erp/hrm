package com.raster.hrm.separation;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.separation.dto.ExitChecklistRequest;
import com.raster.hrm.separation.dto.ExitChecklistResponse;
import com.raster.hrm.separation.entity.ExitChecklist;
import com.raster.hrm.separation.entity.Separation;
import com.raster.hrm.separation.entity.SeparationStatus;
import com.raster.hrm.separation.repository.ExitChecklistRepository;
import com.raster.hrm.separation.repository.SeparationRepository;
import com.raster.hrm.separation.service.ExitChecklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
class ExitChecklistServiceTest {

    @Mock
    private ExitChecklistRepository exitChecklistRepository;

    @Mock
    private SeparationRepository separationRepository;

    @InjectMocks
    private ExitChecklistService exitChecklistService;

    private Separation createSeparation(Long id) {
        var separation = new Separation();
        separation.setId(id);
        separation.setStatus(SeparationStatus.PENDING);
        return separation;
    }

    private ExitChecklist createExitChecklist(Long id, Separation separation, boolean cleared) {
        var checklist = new ExitChecklist();
        checklist.setId(id);
        checklist.setSeparation(separation);
        checklist.setItemName("Return laptop");
        checklist.setDepartment("IT");
        checklist.setCleared(cleared);
        checklist.setNotes("Company laptop");
        checklist.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        checklist.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        if (cleared) {
            checklist.setClearedBy("Admin");
            checklist.setClearedAt(LocalDateTime.of(2024, 2, 1, 10, 0));
        }
        return checklist;
    }

    private ExitChecklistRequest createRequest() {
        return new ExitChecklistRequest(1L, "Return laptop", "IT", "Company laptop");
    }

    @Test
    void getBySeparationId_shouldReturnChecklistItems() {
        var separation = createSeparation(1L);
        var items = List.of(
                createExitChecklist(1L, separation, false),
                createExitChecklist(2L, separation, true)
        );
        when(exitChecklistRepository.findBySeparationId(1L)).thenReturn(items);

        var result = exitChecklistService.getBySeparationId(1L);

        assertEquals(2, result.size());
        assertEquals("Return laptop", result.get(0).itemName());
        assertFalse(result.get(0).cleared());
        assertTrue(result.get(1).cleared());
    }

    @Test
    void getBySeparationId_shouldReturnEmptyList() {
        when(exitChecklistRepository.findBySeparationId(1L)).thenReturn(List.of());

        var result = exitChecklistService.getBySeparationId(1L);

        assertEquals(0, result.size());
    }

    @Test
    void create_shouldCreateAndReturnChecklistItem() {
        var separation = createSeparation(1L);
        var request = createRequest();
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));
        when(exitChecklistRepository.save(any(ExitChecklist.class))).thenAnswer(invocation -> {
            ExitChecklist c = invocation.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        var result = exitChecklistService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.separationId());
        assertEquals("Return laptop", result.itemName());
        assertEquals("IT", result.department());
        assertFalse(result.cleared());
        assertNull(result.clearedBy());
        assertNull(result.clearedAt());
        assertEquals("Company laptop", result.notes());
        verify(exitChecklistRepository).save(any(ExitChecklist.class));
    }

    @Test
    void create_shouldThrowWhenSeparationNotFound() {
        var request = createRequest();
        when(separationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exitChecklistService.create(request));
        verify(exitChecklistRepository, never()).save(any());
    }

    @Test
    void clearItem_shouldClearAndReturnItem() {
        var separation = createSeparation(1L);
        var checklist = createExitChecklist(1L, separation, false);
        when(exitChecklistRepository.findById(1L)).thenReturn(Optional.of(checklist));
        when(exitChecklistRepository.save(any(ExitChecklist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = exitChecklistService.clearItem(1L, "Admin User");

        assertTrue(result.cleared());
        assertEquals("Admin User", result.clearedBy());
        assertNotNull(result.clearedAt());
        verify(exitChecklistRepository).save(any(ExitChecklist.class));
    }

    @Test
    void clearItem_shouldThrowWhenNotFound() {
        when(exitChecklistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exitChecklistService.clearItem(999L, "Admin"));
        verify(exitChecklistRepository, never()).save(any());
    }

    @Test
    void clearItem_shouldThrowWhenAlreadyCleared() {
        var separation = createSeparation(1L);
        var checklist = createExitChecklist(1L, separation, true);
        when(exitChecklistRepository.findById(1L)).thenReturn(Optional.of(checklist));

        assertThrows(BadRequestException.class,
                () -> exitChecklistService.clearItem(1L, "Admin"));
        verify(exitChecklistRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteUnclearedItem() {
        var separation = createSeparation(1L);
        var checklist = createExitChecklist(1L, separation, false);
        when(exitChecklistRepository.findById(1L)).thenReturn(Optional.of(checklist));

        exitChecklistService.delete(1L);

        verify(exitChecklistRepository).delete(checklist);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(exitChecklistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exitChecklistService.delete(999L));
        verify(exitChecklistRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowWhenCleared() {
        var separation = createSeparation(1L);
        var checklist = createExitChecklist(1L, separation, true);
        when(exitChecklistRepository.findById(1L)).thenReturn(Optional.of(checklist));

        assertThrows(BadRequestException.class,
                () -> exitChecklistService.delete(1L));
        verify(exitChecklistRepository, never()).delete(any());
    }

    @Test
    void create_shouldMapAllFieldsCorrectly() {
        var separation = createSeparation(1L);
        var request = new ExitChecklistRequest(1L, "Return ID card", "HR", "Employee badge");
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));
        when(exitChecklistRepository.save(any(ExitChecklist.class))).thenAnswer(invocation -> {
            ExitChecklist c = invocation.getArgument(0);
            c.setId(2L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        var result = exitChecklistService.create(request);

        assertEquals("Return ID card", result.itemName());
        assertEquals("HR", result.department());
        assertEquals("Employee badge", result.notes());
    }
}
