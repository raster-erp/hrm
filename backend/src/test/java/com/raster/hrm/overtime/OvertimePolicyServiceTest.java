package com.raster.hrm.overtime;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.overtime.dto.OvertimePolicyRequest;
import com.raster.hrm.overtime.entity.OvertimePolicy;
import com.raster.hrm.overtime.entity.OvertimePolicyType;
import com.raster.hrm.overtime.repository.OvertimePolicyRepository;
import com.raster.hrm.overtime.service.OvertimePolicyService;
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
class OvertimePolicyServiceTest {

    @Mock
    private OvertimePolicyRepository overtimePolicyRepository;

    @InjectMocks
    private OvertimePolicyService overtimePolicyService;

    private OvertimePolicy createPolicy(Long id, String name, OvertimePolicyType type) {
        var policy = new OvertimePolicy();
        policy.setId(id);
        policy.setName(name);
        policy.setType(type);
        policy.setRateMultiplier(new BigDecimal("1.50"));
        policy.setMinOvertimeMinutes(30);
        policy.setMaxOvertimeMinutesPerDay(480);
        policy.setMaxOvertimeMinutesPerMonth(2400);
        policy.setRequiresApproval(true);
        policy.setActive(true);
        policy.setDescription("Test policy");
        policy.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        policy.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return policy;
    }

    private OvertimePolicyRequest createRequest() {
        return new OvertimePolicyRequest(
                "Weekday Overtime",
                "WEEKDAY",
                new BigDecimal("1.50"),
                30, 480, 2400,
                true, "Weekday overtime policy"
        );
    }

    @Test
    void getAll_shouldReturnPageOfPolicies() {
        var policies = List.of(
                createPolicy(1L, "Weekday", OvertimePolicyType.WEEKDAY),
                createPolicy(2L, "Weekend", OvertimePolicyType.WEEKEND)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(policies, pageable, 2);
        when(overtimePolicyRepository.findAll(pageable)).thenReturn(page);

        var result = overtimePolicyService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Weekday", result.getContent().get(0).name());
        assertEquals("Weekend", result.getContent().get(1).name());
        verify(overtimePolicyRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<OvertimePolicy>(List.of(), pageable, 0);
        when(overtimePolicyRepository.findAll(pageable)).thenReturn(page);

        var result = overtimePolicyService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnPolicy() {
        var policy = createPolicy(1L, "Weekday", OvertimePolicyType.WEEKDAY);
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));

        var result = overtimePolicyService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Weekday", result.name());
        assertEquals("WEEKDAY", result.type());
        assertEquals(new BigDecimal("1.50"), result.rateMultiplier());
        assertEquals(30, result.minOvertimeMinutes());
        assertEquals(480, result.maxOvertimeMinutesPerDay());
        assertEquals(2400, result.maxOvertimeMinutesPerMonth());
        assertTrue(result.requiresApproval());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(overtimePolicyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> overtimePolicyService.getById(999L));
    }

    @Test
    void getByType_shouldReturnPolicies() {
        var policies = List.of(createPolicy(1L, "Weekday", OvertimePolicyType.WEEKDAY));
        when(overtimePolicyRepository.findByType(OvertimePolicyType.WEEKDAY)).thenReturn(policies);

        var result = overtimePolicyService.getByType(OvertimePolicyType.WEEKDAY);

        assertEquals(1, result.size());
        assertEquals("Weekday", result.get(0).name());
        assertEquals("WEEKDAY", result.get(0).type());
    }

    @Test
    void getByType_shouldReturnEmptyListWhenNoPolicies() {
        when(overtimePolicyRepository.findByType(OvertimePolicyType.HOLIDAY)).thenReturn(List.of());

        var result = overtimePolicyService.getByType(OvertimePolicyType.HOLIDAY);

        assertEquals(0, result.size());
    }

    @Test
    void getActive_shouldReturnActivePolicies() {
        var policies = List.of(createPolicy(1L, "Weekday", OvertimePolicyType.WEEKDAY));
        when(overtimePolicyRepository.findByActive(true)).thenReturn(policies);

        var result = overtimePolicyService.getActive();

        assertEquals(1, result.size());
        assertTrue(result.get(0).active());
    }

    @Test
    void create_shouldCreateAndReturnPolicy() {
        var request = createRequest();
        when(overtimePolicyRepository.existsByName("Weekday Overtime")).thenReturn(false);
        when(overtimePolicyRepository.save(any(OvertimePolicy.class))).thenAnswer(invocation -> {
            OvertimePolicy p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        var result = overtimePolicyService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Weekday Overtime", result.name());
        assertEquals("WEEKDAY", result.type());
        assertEquals(new BigDecimal("1.50"), result.rateMultiplier());
        assertEquals(30, result.minOvertimeMinutes());
        verify(overtimePolicyRepository).save(any(OvertimePolicy.class));
    }

    @Test
    void create_shouldThrowWhenNameExists() {
        var request = createRequest();
        when(overtimePolicyRepository.existsByName("Weekday Overtime")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> overtimePolicyService.create(request));
        assertTrue(ex.getMessage().contains("Weekday Overtime"));
        verify(overtimePolicyRepository, never()).save(any());
    }

    @Test
    void create_shouldUseDefaultsWhenNull() {
        var request = new OvertimePolicyRequest("General", "WEEKDAY",
                new BigDecimal("1.00"), null, null, null, null, null);
        when(overtimePolicyRepository.existsByName("General")).thenReturn(false);
        when(overtimePolicyRepository.save(any(OvertimePolicy.class))).thenAnswer(invocation -> {
            OvertimePolicy p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        var result = overtimePolicyService.create(request);

        assertEquals(0, result.minOvertimeMinutes());
        assertTrue(result.requiresApproval());
    }

    @Test
    void update_shouldUpdateAndReturnPolicy() {
        var policy = createPolicy(1L, "Weekday", OvertimePolicyType.WEEKDAY);
        var request = new OvertimePolicyRequest("Updated Weekday", "WEEKDAY",
                new BigDecimal("2.00"), 15, 600, 3000, false, "Updated");
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(overtimePolicyRepository.save(any(OvertimePolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = overtimePolicyService.update(1L, request);

        assertNotNull(result);
        assertEquals("Updated Weekday", result.name());
        assertEquals(new BigDecimal("2.00"), result.rateMultiplier());
        assertEquals(15, result.minOvertimeMinutes());
        assertEquals(600, result.maxOvertimeMinutesPerDay());
        assertEquals(3000, result.maxOvertimeMinutesPerMonth());
        assertFalse(result.requiresApproval());
        verify(overtimePolicyRepository).save(any(OvertimePolicy.class));
    }

    @Test
    void update_shouldThrowWhenPolicyNotFound() {
        var request = createRequest();
        when(overtimePolicyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> overtimePolicyService.update(999L, request));
        verify(overtimePolicyRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNewNameExists() {
        var policy = createPolicy(1L, "Weekday", OvertimePolicyType.WEEKDAY);
        var request = new OvertimePolicyRequest("Weekend", "WEEKEND",
                new BigDecimal("2.00"), 30, 480, 2400, true, null);
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(overtimePolicyRepository.existsByName("Weekend")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> overtimePolicyService.update(1L, request));
        assertTrue(ex.getMessage().contains("Weekend"));
        verify(overtimePolicyRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameName() {
        var policy = createPolicy(1L, "Weekday", OvertimePolicyType.WEEKDAY);
        var request = new OvertimePolicyRequest("Weekday", "WEEKDAY",
                new BigDecimal("2.00"), 15, 600, 3000, true, "Updated");
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(overtimePolicyRepository.save(any(OvertimePolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = overtimePolicyService.update(1L, request);

        assertEquals("Weekday", result.name());
        verify(overtimePolicyRepository).save(any(OvertimePolicy.class));
    }

    @Test
    void updateActive_shouldUpdateActiveStatus() {
        var policy = createPolicy(1L, "Weekday", OvertimePolicyType.WEEKDAY);
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(overtimePolicyRepository.save(any(OvertimePolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = overtimePolicyService.updateActive(1L, false);

        assertFalse(result.active());
        verify(overtimePolicyRepository).save(any(OvertimePolicy.class));
    }

    @Test
    void updateActive_shouldThrowWhenNotFound() {
        when(overtimePolicyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> overtimePolicyService.updateActive(999L, false));
        verify(overtimePolicyRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeletePolicy() {
        var policy = createPolicy(1L, "Weekday", OvertimePolicyType.WEEKDAY);
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));

        overtimePolicyService.delete(1L);

        verify(overtimePolicyRepository).delete(policy);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(overtimePolicyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> overtimePolicyService.delete(999L));
        verify(overtimePolicyRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var policy = createPolicy(1L, "Weekday", OvertimePolicyType.WEEKDAY);
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));

        var result = overtimePolicyService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("Weekday", result.name());
        assertEquals("WEEKDAY", result.type());
        assertEquals(new BigDecimal("1.50"), result.rateMultiplier());
        assertEquals(30, result.minOvertimeMinutes());
        assertEquals(480, result.maxOvertimeMinutesPerDay());
        assertEquals(2400, result.maxOvertimeMinutesPerMonth());
        assertTrue(result.requiresApproval());
        assertTrue(result.active());
        assertEquals("Test policy", result.description());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.updatedAt());
    }
}
