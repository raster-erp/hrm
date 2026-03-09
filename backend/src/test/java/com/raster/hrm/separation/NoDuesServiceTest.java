package com.raster.hrm.separation;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.separation.dto.NoDuesRequest;
import com.raster.hrm.separation.entity.NoDues;
import com.raster.hrm.separation.entity.Separation;
import com.raster.hrm.separation.entity.SeparationStatus;
import com.raster.hrm.separation.repository.NoDuesRepository;
import com.raster.hrm.separation.repository.SeparationRepository;
import com.raster.hrm.separation.service.NoDuesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class NoDuesServiceTest {

    @Mock
    private NoDuesRepository noDuesRepository;

    @Mock
    private SeparationRepository separationRepository;

    @InjectMocks
    private NoDuesService noDuesService;

    private Separation createSeparation(Long id) {
        var separation = new Separation();
        separation.setId(id);
        separation.setStatus(SeparationStatus.PENDING);
        return separation;
    }

    private NoDues createNoDues(Long id, Separation separation, boolean cleared) {
        var noDues = new NoDues();
        noDues.setId(id);
        noDues.setSeparation(separation);
        noDues.setDepartment("Finance");
        noDues.setAmountDue(new BigDecimal("500.00"));
        noDues.setCleared(cleared);
        noDues.setNotes("Pending reimbursement");
        noDues.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        noDues.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        if (cleared) {
            noDues.setClearedBy("Admin");
            noDues.setClearedAt(LocalDateTime.of(2024, 2, 1, 10, 0));
        }
        return noDues;
    }

    private NoDuesRequest createRequest() {
        return new NoDuesRequest(1L, "Finance", new BigDecimal("500.00"), "Pending reimbursement");
    }

    @Test
    void getBySeparationId_shouldReturnNoDuesItems() {
        var separation = createSeparation(1L);
        var items = List.of(
                createNoDues(1L, separation, false),
                createNoDues(2L, separation, true)
        );
        when(noDuesRepository.findBySeparationId(1L)).thenReturn(items);

        var result = noDuesService.getBySeparationId(1L);

        assertEquals(2, result.size());
        assertEquals("Finance", result.get(0).department());
        assertFalse(result.get(0).cleared());
        assertTrue(result.get(1).cleared());
    }

    @Test
    void getBySeparationId_shouldReturnEmptyList() {
        when(noDuesRepository.findBySeparationId(1L)).thenReturn(List.of());

        var result = noDuesService.getBySeparationId(1L);

        assertEquals(0, result.size());
    }

    @Test
    void create_shouldCreateAndReturnNoDuesRecord() {
        var separation = createSeparation(1L);
        var request = createRequest();
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));
        when(noDuesRepository.save(any(NoDues.class))).thenAnswer(invocation -> {
            NoDues n = invocation.getArgument(0);
            n.setId(1L);
            n.setCreatedAt(LocalDateTime.now());
            n.setUpdatedAt(LocalDateTime.now());
            return n;
        });

        var result = noDuesService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.separationId());
        assertEquals("Finance", result.department());
        assertFalse(result.cleared());
        assertNull(result.clearedBy());
        assertNull(result.clearedAt());
        assertEquals(new BigDecimal("500.00"), result.amountDue());
        assertEquals("Pending reimbursement", result.notes());
        verify(noDuesRepository).save(any(NoDues.class));
    }

    @Test
    void create_shouldDefaultAmountDueToZeroWhenNull() {
        var separation = createSeparation(1L);
        var request = new NoDuesRequest(1L, "HR", null, "No dues");
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));
        when(noDuesRepository.save(any(NoDues.class))).thenAnswer(invocation -> {
            NoDues n = invocation.getArgument(0);
            n.setId(1L);
            n.setCreatedAt(LocalDateTime.now());
            n.setUpdatedAt(LocalDateTime.now());
            return n;
        });

        var result = noDuesService.create(request);

        assertEquals(BigDecimal.ZERO, result.amountDue());
    }

    @Test
    void create_shouldThrowWhenSeparationNotFound() {
        var request = createRequest();
        when(separationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> noDuesService.create(request));
        verify(noDuesRepository, never()).save(any());
    }

    @Test
    void clearDepartment_shouldSetClearedFields() {
        var separation = createSeparation(1L);
        var noDues = createNoDues(1L, separation, false);
        when(noDuesRepository.findById(1L)).thenReturn(Optional.of(noDues));
        when(noDuesRepository.save(any(NoDues.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = noDuesService.clearDepartment(1L, "Admin User");

        assertTrue(result.cleared());
        assertEquals("Admin User", result.clearedBy());
        assertNotNull(result.clearedAt());
        verify(noDuesRepository).save(any(NoDues.class));
    }

    @Test
    void clearDepartment_shouldThrowWhenNotFound() {
        when(noDuesRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> noDuesService.clearDepartment(999L, "Admin"));
        verify(noDuesRepository, never()).save(any());
    }

    @Test
    void clearDepartment_shouldThrowWhenAlreadyCleared() {
        var separation = createSeparation(1L);
        var noDues = createNoDues(1L, separation, true);
        when(noDuesRepository.findById(1L)).thenReturn(Optional.of(noDues));

        assertThrows(BadRequestException.class,
                () -> noDuesService.clearDepartment(1L, "Admin"));
        verify(noDuesRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteUnclearedRecord() {
        var separation = createSeparation(1L);
        var noDues = createNoDues(1L, separation, false);
        when(noDuesRepository.findById(1L)).thenReturn(Optional.of(noDues));

        noDuesService.delete(1L);

        verify(noDuesRepository).delete(noDues);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(noDuesRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> noDuesService.delete(999L));
        verify(noDuesRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowWhenCleared() {
        var separation = createSeparation(1L);
        var noDues = createNoDues(1L, separation, true);
        when(noDuesRepository.findById(1L)).thenReturn(Optional.of(noDues));

        assertThrows(BadRequestException.class,
                () -> noDuesService.delete(1L));
        verify(noDuesRepository, never()).delete(any());
    }

    @Test
    void create_shouldMapAllFieldsCorrectly() {
        var separation = createSeparation(1L);
        var request = new NoDuesRequest(1L, "IT", new BigDecimal("1000.00"), "Equipment charges");
        when(separationRepository.findById(1L)).thenReturn(Optional.of(separation));
        when(noDuesRepository.save(any(NoDues.class))).thenAnswer(invocation -> {
            NoDues n = invocation.getArgument(0);
            n.setId(2L);
            n.setCreatedAt(LocalDateTime.now());
            n.setUpdatedAt(LocalDateTime.now());
            return n;
        });

        var result = noDuesService.create(request);

        assertEquals("IT", result.department());
        assertEquals(new BigDecimal("1000.00"), result.amountDue());
        assertEquals("Equipment charges", result.notes());
    }
}
