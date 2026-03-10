package com.raster.hrm.shift;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.shift.dto.ShiftRequest;
import com.raster.hrm.shift.entity.Shift;
import com.raster.hrm.shift.entity.ShiftType;
import com.raster.hrm.shift.repository.ShiftRepository;
import com.raster.hrm.shift.service.ShiftService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private ShiftService shiftService;

    private Shift createShift(Long id, String name, ShiftType type) {
        var shift = new Shift();
        shift.setId(id);
        shift.setName(name);
        shift.setType(type);
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(17, 0));
        shift.setBreakDurationMinutes(60);
        shift.setGracePeriodMinutes(15);
        shift.setDescription("Test shift");
        shift.setActive(true);
        shift.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        shift.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return shift;
    }

    private ShiftRequest createRequest() {
        return new ShiftRequest(
                "Morning Shift",
                "MORNING",
                LocalTime.of(6, 0),
                LocalTime.of(14, 0),
                30,
                10,
                "Morning shift description"
        );
    }

    @Test
    void getAll_shouldReturnPageOfShifts() {
        var shifts = List.of(
                createShift(1L, "Morning", ShiftType.MORNING),
                createShift(2L, "Evening", ShiftType.EVENING)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(shifts, pageable, 2);
        when(shiftRepository.findAll(pageable)).thenReturn(page);

        var result = shiftService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Morning", result.getContent().get(0).name());
        assertEquals("Evening", result.getContent().get(1).name());
        verify(shiftRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<Shift>(List.of(), pageable, 0);
        when(shiftRepository.findAll(pageable)).thenReturn(page);

        var result = shiftService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnShift() {
        var shift = createShift(1L, "Morning", ShiftType.MORNING);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));

        var result = shiftService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Morning", result.name());
        assertEquals("MORNING", result.type());
        assertEquals(LocalTime.of(9, 0), result.startTime());
        assertEquals(LocalTime.of(17, 0), result.endTime());
        assertEquals(60, result.breakDurationMinutes());
        assertEquals(15, result.gracePeriodMinutes());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shiftService.getById(999L));
    }

    @Test
    void getByType_shouldReturnShifts() {
        var shifts = List.of(createShift(1L, "Morning", ShiftType.MORNING));
        when(shiftRepository.findByType(ShiftType.MORNING)).thenReturn(shifts);

        var result = shiftService.getByType(ShiftType.MORNING);

        assertEquals(1, result.size());
        assertEquals("Morning", result.get(0).name());
        assertEquals("MORNING", result.get(0).type());
    }

    @Test
    void getByType_shouldReturnEmptyListWhenNoShifts() {
        when(shiftRepository.findByType(ShiftType.NIGHT)).thenReturn(List.of());

        var result = shiftService.getByType(ShiftType.NIGHT);

        assertEquals(0, result.size());
    }

    @Test
    void getActive_shouldReturnActiveShifts() {
        var shifts = List.of(createShift(1L, "Morning", ShiftType.MORNING));
        when(shiftRepository.findByActive(true)).thenReturn(shifts);

        var result = shiftService.getActive();

        assertEquals(1, result.size());
        assertTrue(result.get(0).active());
    }

    @Test
    void create_shouldCreateAndReturnShift() {
        var request = createRequest();
        when(shiftRepository.existsByName("Morning Shift")).thenReturn(false);
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> {
            Shift s = invocation.getArgument(0);
            s.setId(1L);
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        var result = shiftService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Morning Shift", result.name());
        assertEquals("MORNING", result.type());
        assertEquals(LocalTime.of(6, 0), result.startTime());
        assertEquals(LocalTime.of(14, 0), result.endTime());
        assertEquals(30, result.breakDurationMinutes());
        assertEquals(10, result.gracePeriodMinutes());
        verify(shiftRepository).save(any(Shift.class));
    }

    @Test
    void create_shouldThrowWhenNameExists() {
        var request = createRequest();
        when(shiftRepository.existsByName("Morning Shift")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> shiftService.create(request));
        assertTrue(ex.getMessage().contains("Morning Shift"));
        verify(shiftRepository, never()).save(any());
    }

    @Test
    void create_shouldUseDefaultBreakAndGraceWhenNull() {
        var request = new ShiftRequest("General", "GENERAL",
                LocalTime.of(9, 0), LocalTime.of(17, 0), null, null, null);
        when(shiftRepository.existsByName("General")).thenReturn(false);
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> {
            Shift s = invocation.getArgument(0);
            s.setId(1L);
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        var result = shiftService.create(request);

        assertEquals(0, result.breakDurationMinutes());
        assertEquals(0, result.gracePeriodMinutes());
    }

    @Test
    void update_shouldUpdateAndReturnShift() {
        var shift = createShift(1L, "Morning", ShiftType.MORNING);
        var request = new ShiftRequest("Updated Morning", "MORNING",
                LocalTime.of(7, 0), LocalTime.of(15, 0), 45, 20, "Updated");
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = shiftService.update(1L, request);

        assertNotNull(result);
        assertEquals("Updated Morning", result.name());
        assertEquals(LocalTime.of(7, 0), result.startTime());
        assertEquals(LocalTime.of(15, 0), result.endTime());
        assertEquals(45, result.breakDurationMinutes());
        assertEquals(20, result.gracePeriodMinutes());
        verify(shiftRepository).save(any(Shift.class));
    }

    @Test
    void update_shouldThrowWhenShiftNotFound() {
        var request = createRequest();
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shiftService.update(999L, request));
        verify(shiftRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNewNameExists() {
        var shift = createShift(1L, "Morning", ShiftType.MORNING);
        var request = new ShiftRequest("Evening", "EVENING",
                LocalTime.of(14, 0), LocalTime.of(22, 0), 30, 10, null);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRepository.existsByName("Evening")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> shiftService.update(1L, request));
        assertTrue(ex.getMessage().contains("Evening"));
        verify(shiftRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameName() {
        var shift = createShift(1L, "Morning", ShiftType.MORNING);
        var request = new ShiftRequest("Morning", "MORNING",
                LocalTime.of(7, 0), LocalTime.of(15, 0), 45, 20, "Updated");
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = shiftService.update(1L, request);

        assertEquals("Morning", result.name());
        verify(shiftRepository).save(any(Shift.class));
    }

    @Test
    void updateActive_shouldUpdateActiveStatus() {
        var shift = createShift(1L, "Morning", ShiftType.MORNING);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = shiftService.updateActive(1L, false);

        assertFalse(result.active());
        verify(shiftRepository).save(any(Shift.class));
    }

    @Test
    void updateActive_shouldThrowWhenNotFound() {
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shiftService.updateActive(999L, false));
        verify(shiftRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteShift() {
        var shift = createShift(1L, "Morning", ShiftType.MORNING);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));

        shiftService.delete(1L);

        verify(shiftRepository).delete(shift);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shiftService.delete(999L));
        verify(shiftRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var shift = createShift(1L, "Morning", ShiftType.MORNING);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));

        var result = shiftService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("Morning", result.name());
        assertEquals("MORNING", result.type());
        assertEquals(LocalTime.of(9, 0), result.startTime());
        assertEquals(LocalTime.of(17, 0), result.endTime());
        assertEquals(60, result.breakDurationMinutes());
        assertEquals(15, result.gracePeriodMinutes());
        assertEquals("Test shift", result.description());
        assertTrue(result.active());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.updatedAt());
    }
}
