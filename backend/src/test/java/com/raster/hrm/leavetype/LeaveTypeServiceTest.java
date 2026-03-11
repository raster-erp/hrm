package com.raster.hrm.leavetype;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leavetype.dto.LeaveTypeRequest;
import com.raster.hrm.leavetype.entity.LeaveType;
import com.raster.hrm.leavetype.entity.LeaveTypeCategory;
import com.raster.hrm.leavetype.repository.LeaveTypeRepository;
import com.raster.hrm.leavetype.service.LeaveTypeService;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveTypeServiceTest {

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @InjectMocks
    private LeaveTypeService leaveTypeService;

    private LeaveType createLeaveType(Long id, String code, String name, LeaveTypeCategory category) {
        var leaveType = new LeaveType();
        leaveType.setId(id);
        leaveType.setCode(code);
        leaveType.setName(name);
        leaveType.setCategory(category);
        leaveType.setDescription("Test leave type");
        leaveType.setActive(true);
        leaveType.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        leaveType.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return leaveType;
    }

    private LeaveTypeRequest createRequest() {
        return new LeaveTypeRequest(
                "AL",
                "Annual Leave",
                "PAID",
                "Standard annual leave"
        );
    }

    @Test
    void getAll_shouldReturnPageOfLeaveTypes() {
        var leaveTypes = List.of(
                createLeaveType(1L, "AL", "Annual Leave", LeaveTypeCategory.PAID),
                createLeaveType(2L, "SL", "Sick Leave", LeaveTypeCategory.PAID)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(leaveTypes, pageable, 2);
        when(leaveTypeRepository.findAll(pageable)).thenReturn(page);

        var result = leaveTypeService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Annual Leave", result.getContent().get(0).name());
        assertEquals("Sick Leave", result.getContent().get(1).name());
        verify(leaveTypeRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<LeaveType>(List.of(), pageable, 0);
        when(leaveTypeRepository.findAll(pageable)).thenReturn(page);

        var result = leaveTypeService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnLeaveType() {
        var leaveType = createLeaveType(1L, "AL", "Annual Leave", LeaveTypeCategory.PAID);
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        var result = leaveTypeService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("AL", result.code());
        assertEquals("Annual Leave", result.name());
        assertEquals("PAID", result.category());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(leaveTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveTypeService.getById(999L));
    }

    @Test
    void getByCategory_shouldReturnLeaveTypes() {
        var leaveTypes = List.of(createLeaveType(1L, "AL", "Annual Leave", LeaveTypeCategory.PAID));
        when(leaveTypeRepository.findByCategory(LeaveTypeCategory.PAID)).thenReturn(leaveTypes);

        var result = leaveTypeService.getByCategory(LeaveTypeCategory.PAID);

        assertEquals(1, result.size());
        assertEquals("Annual Leave", result.get(0).name());
        assertEquals("PAID", result.get(0).category());
    }

    @Test
    void getByCategory_shouldReturnEmptyListWhenNoLeaveTypes() {
        when(leaveTypeRepository.findByCategory(LeaveTypeCategory.SPECIAL)).thenReturn(List.of());

        var result = leaveTypeService.getByCategory(LeaveTypeCategory.SPECIAL);

        assertEquals(0, result.size());
    }

    @Test
    void getActive_shouldReturnActiveLeaveTypes() {
        var leaveTypes = List.of(createLeaveType(1L, "AL", "Annual Leave", LeaveTypeCategory.PAID));
        when(leaveTypeRepository.findByActive(true)).thenReturn(leaveTypes);

        var result = leaveTypeService.getActive();

        assertEquals(1, result.size());
        assertTrue(result.get(0).active());
    }

    @Test
    void create_shouldCreateAndReturnLeaveType() {
        var request = createRequest();
        when(leaveTypeRepository.existsByCode("AL")).thenReturn(false);
        when(leaveTypeRepository.save(any(LeaveType.class))).thenAnswer(invocation -> {
            LeaveType lt = invocation.getArgument(0);
            lt.setId(1L);
            lt.setCreatedAt(LocalDateTime.now());
            lt.setUpdatedAt(LocalDateTime.now());
            return lt;
        });

        var result = leaveTypeService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("AL", result.code());
        assertEquals("Annual Leave", result.name());
        assertEquals("PAID", result.category());
        assertEquals("Standard annual leave", result.description());
        verify(leaveTypeRepository).save(any(LeaveType.class));
    }

    @Test
    void create_shouldThrowWhenCodeExists() {
        var request = createRequest();
        when(leaveTypeRepository.existsByCode("AL")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> leaveTypeService.create(request));
        assertTrue(ex.getMessage().contains("AL"));
        verify(leaveTypeRepository, never()).save(any());
    }

    @Test
    void create_shouldUseDefaultsWhenNullableFieldsAreNull() {
        var request = new LeaveTypeRequest("CL", "Casual Leave", "PAID", null);
        when(leaveTypeRepository.existsByCode("CL")).thenReturn(false);
        when(leaveTypeRepository.save(any(LeaveType.class))).thenAnswer(invocation -> {
            LeaveType lt = invocation.getArgument(0);
            lt.setId(1L);
            lt.setCreatedAt(LocalDateTime.now());
            lt.setUpdatedAt(LocalDateTime.now());
            return lt;
        });

        var result = leaveTypeService.create(request);

        assertNull(result.description());
        assertTrue(result.active());
    }

    @Test
    void update_shouldUpdateAndReturnLeaveType() {
        var leaveType = createLeaveType(1L, "AL", "Annual Leave", LeaveTypeCategory.PAID);
        var request = new LeaveTypeRequest("AL", "Updated Annual Leave", "STATUTORY", "Updated description");
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveTypeRepository.save(any(LeaveType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leaveTypeService.update(1L, request);

        assertNotNull(result);
        assertEquals("AL", result.code());
        assertEquals("Updated Annual Leave", result.name());
        assertEquals("STATUTORY", result.category());
        assertEquals("Updated description", result.description());
        verify(leaveTypeRepository).save(any(LeaveType.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var request = createRequest();
        when(leaveTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveTypeService.update(999L, request));
        verify(leaveTypeRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNewCodeAlreadyExists() {
        var leaveType = createLeaveType(1L, "AL", "Annual Leave", LeaveTypeCategory.PAID);
        var request = new LeaveTypeRequest("SL", "Sick Leave", "PAID", null);
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveTypeRepository.existsByCode("SL")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> leaveTypeService.update(1L, request));
        assertTrue(ex.getMessage().contains("SL"));
        verify(leaveTypeRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameCode() {
        var leaveType = createLeaveType(1L, "AL", "Annual Leave", LeaveTypeCategory.PAID);
        var request = new LeaveTypeRequest("AL", "Updated Annual Leave", "PAID", "Updated");
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveTypeRepository.save(any(LeaveType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leaveTypeService.update(1L, request);

        assertEquals("AL", result.code());
        verify(leaveTypeRepository).save(any(LeaveType.class));
    }

    @Test
    void updateActive_shouldUpdateActiveStatus() {
        var leaveType = createLeaveType(1L, "AL", "Annual Leave", LeaveTypeCategory.PAID);
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveTypeRepository.save(any(LeaveType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leaveTypeService.updateActive(1L, false);

        assertFalse(result.active());
        verify(leaveTypeRepository).save(any(LeaveType.class));
    }

    @Test
    void updateActive_shouldThrowWhenNotFound() {
        when(leaveTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveTypeService.updateActive(999L, false));
        verify(leaveTypeRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteLeaveType() {
        var leaveType = createLeaveType(1L, "AL", "Annual Leave", LeaveTypeCategory.PAID);
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        leaveTypeService.delete(1L);

        verify(leaveTypeRepository).delete(leaveType);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(leaveTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveTypeService.delete(999L));
        verify(leaveTypeRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapAllResponseFieldsCorrectly() {
        var leaveType = createLeaveType(1L, "AL", "Annual Leave", LeaveTypeCategory.PAID);
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        var result = leaveTypeService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("AL", result.code());
        assertEquals("Annual Leave", result.name());
        assertEquals("PAID", result.category());
        assertEquals("Test leave type", result.description());
        assertTrue(result.active());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.updatedAt());
    }
}
