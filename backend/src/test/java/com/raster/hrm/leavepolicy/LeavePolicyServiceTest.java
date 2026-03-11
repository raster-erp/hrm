package com.raster.hrm.leavepolicy;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leavepolicy.dto.LeavePolicyRequest;
import com.raster.hrm.leavepolicy.entity.AccrualFrequency;
import com.raster.hrm.leavepolicy.entity.LeavePolicy;
import com.raster.hrm.leavepolicy.repository.LeavePolicyRepository;
import com.raster.hrm.leavepolicy.service.LeavePolicyService;
import com.raster.hrm.leavetype.entity.LeaveType;
import com.raster.hrm.leavetype.repository.LeaveTypeRepository;
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
class LeavePolicyServiceTest {

    @Mock
    private LeavePolicyRepository leavePolicyRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @InjectMocks
    private LeavePolicyService leavePolicyService;

    private LeaveType createLeaveType(Long id, String name, String code) {
        var leaveType = new LeaveType();
        leaveType.setId(id);
        leaveType.setName(name);
        leaveType.setCode(code);
        return leaveType;
    }

    private LeavePolicy createPolicy(Long id, String name, LeaveType leaveType) {
        var policy = new LeavePolicy();
        policy.setId(id);
        policy.setName(name);
        policy.setLeaveType(leaveType);
        policy.setAccrualFrequency(AccrualFrequency.MONTHLY);
        policy.setAccrualDays(new BigDecimal("1.50"));
        policy.setMaxAccumulation(new BigDecimal("30.00"));
        policy.setCarryForwardLimit(new BigDecimal("5.00"));
        policy.setProRataForNewJoiners(true);
        policy.setMinServiceDaysRequired(90);
        policy.setActive(true);
        policy.setDescription("Test policy");
        policy.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        policy.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return policy;
    }

    private LeavePolicyRequest createRequest() {
        return new LeavePolicyRequest(
                "Annual Leave Policy",
                10L,
                "MONTHLY",
                new BigDecimal("1.50"),
                new BigDecimal("30.00"),
                new BigDecimal("5.00"),
                true,
                90,
                "Annual leave accrual policy"
        );
    }

    @Test
    void getAll_shouldReturnPageOfPolicies() {
        var leaveType = createLeaveType(10L, "Annual Leave", "AL");
        var policies = List.of(
                createPolicy(1L, "Policy A", leaveType),
                createPolicy(2L, "Policy B", leaveType)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(policies, pageable, 2);
        when(leavePolicyRepository.findAll(pageable)).thenReturn(page);

        var result = leavePolicyService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Policy A", result.getContent().get(0).name());
        assertEquals("Policy B", result.getContent().get(1).name());
        verify(leavePolicyRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<LeavePolicy>(List.of(), pageable, 0);
        when(leavePolicyRepository.findAll(pageable)).thenReturn(page);

        var result = leavePolicyService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnPolicy() {
        var leaveType = createLeaveType(10L, "Annual Leave", "AL");
        var policy = createPolicy(1L, "Policy A", leaveType);
        when(leavePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));

        var result = leavePolicyService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Policy A", result.name());
        assertEquals(10L, result.leaveTypeId());
        assertEquals("Annual Leave", result.leaveTypeName());
        assertEquals("AL", result.leaveTypeCode());
        assertEquals("MONTHLY", result.accrualFrequency());
        assertEquals(new BigDecimal("1.50"), result.accrualDays());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(leavePolicyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leavePolicyService.getById(999L));
    }

    @Test
    void getByLeaveTypeId_shouldReturnPolicies() {
        var leaveType = createLeaveType(10L, "Annual Leave", "AL");
        var policies = List.of(createPolicy(1L, "Policy A", leaveType));
        when(leavePolicyRepository.findByLeaveTypeId(10L)).thenReturn(policies);

        var result = leavePolicyService.getByLeaveTypeId(10L);

        assertEquals(1, result.size());
        assertEquals("Policy A", result.get(0).name());
        assertEquals(10L, result.get(0).leaveTypeId());
    }

    @Test
    void getByLeaveTypeId_shouldReturnEmptyListWhenNoPolicies() {
        when(leavePolicyRepository.findByLeaveTypeId(99L)).thenReturn(List.of());

        var result = leavePolicyService.getByLeaveTypeId(99L);

        assertEquals(0, result.size());
    }

    @Test
    void getActive_shouldReturnActivePolicies() {
        var leaveType = createLeaveType(10L, "Annual Leave", "AL");
        var policies = List.of(createPolicy(1L, "Policy A", leaveType));
        when(leavePolicyRepository.findByActive(true)).thenReturn(policies);

        var result = leavePolicyService.getActive();

        assertEquals(1, result.size());
        assertTrue(result.get(0).active());
    }

    @Test
    void create_shouldCreateAndReturnPolicy() {
        var request = createRequest();
        var leaveType = createLeaveType(10L, "Annual Leave", "AL");
        when(leavePolicyRepository.existsByName("Annual Leave Policy")).thenReturn(false);
        when(leaveTypeRepository.findById(10L)).thenReturn(Optional.of(leaveType));
        when(leavePolicyRepository.save(any(LeavePolicy.class))).thenAnswer(invocation -> {
            LeavePolicy p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        var result = leavePolicyService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Annual Leave Policy", result.name());
        assertEquals("MONTHLY", result.accrualFrequency());
        assertEquals(new BigDecimal("1.50"), result.accrualDays());
        assertEquals(10L, result.leaveTypeId());
        verify(leavePolicyRepository).save(any(LeavePolicy.class));
    }

    @Test
    void create_shouldThrowWhenNameExists() {
        var request = createRequest();
        when(leavePolicyRepository.existsByName("Annual Leave Policy")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> leavePolicyService.create(request));
        assertTrue(ex.getMessage().contains("Annual Leave Policy"));
        verify(leavePolicyRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenLeaveTypeNotFound() {
        var request = createRequest();
        when(leavePolicyRepository.existsByName("Annual Leave Policy")).thenReturn(false);
        when(leaveTypeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leavePolicyService.create(request));
        verify(leavePolicyRepository, never()).save(any());
    }

    @Test
    void create_shouldUseDefaultsWhenNull() {
        var request = new LeavePolicyRequest("General", 10L, "ANNUAL",
                new BigDecimal("1.00"), null, null, null, null, null);
        var leaveType = createLeaveType(10L, "Annual Leave", "AL");
        when(leavePolicyRepository.existsByName("General")).thenReturn(false);
        when(leaveTypeRepository.findById(10L)).thenReturn(Optional.of(leaveType));
        when(leavePolicyRepository.save(any(LeavePolicy.class))).thenAnswer(invocation -> {
            LeavePolicy p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        var result = leavePolicyService.create(request);

        assertEquals(0, result.minServiceDaysRequired());
        assertFalse(result.proRataForNewJoiners());
    }

    @Test
    void update_shouldUpdateAndReturnPolicy() {
        var leaveType = createLeaveType(10L, "Annual Leave", "AL");
        var policy = createPolicy(1L, "Old Policy", leaveType);
        var request = new LeavePolicyRequest("Updated Policy", 10L, "QUARTERLY",
                new BigDecimal("2.00"), new BigDecimal("40.00"), new BigDecimal("10.00"),
                false, 60, "Updated description");
        when(leavePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(leaveTypeRepository.findById(10L)).thenReturn(Optional.of(leaveType));
        when(leavePolicyRepository.save(any(LeavePolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leavePolicyService.update(1L, request);

        assertNotNull(result);
        assertEquals("Updated Policy", result.name());
        assertEquals("QUARTERLY", result.accrualFrequency());
        assertEquals(new BigDecimal("2.00"), result.accrualDays());
        assertEquals(new BigDecimal("40.00"), result.maxAccumulation());
        assertEquals(new BigDecimal("10.00"), result.carryForwardLimit());
        assertFalse(result.proRataForNewJoiners());
        assertEquals(60, result.minServiceDaysRequired());
        verify(leavePolicyRepository).save(any(LeavePolicy.class));
    }

    @Test
    void update_shouldThrowWhenPolicyNotFound() {
        var request = createRequest();
        when(leavePolicyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leavePolicyService.update(999L, request));
        verify(leavePolicyRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNewNameExists() {
        var leaveType = createLeaveType(10L, "Annual Leave", "AL");
        var policy = createPolicy(1L, "Policy A", leaveType);
        var request = new LeavePolicyRequest("Policy B", 10L, "MONTHLY",
                new BigDecimal("1.50"), null, null, true, 90, null);
        when(leavePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(leavePolicyRepository.existsByName("Policy B")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> leavePolicyService.update(1L, request));
        assertTrue(ex.getMessage().contains("Policy B"));
        verify(leavePolicyRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameName() {
        var leaveType = createLeaveType(10L, "Annual Leave", "AL");
        var policy = createPolicy(1L, "Policy A", leaveType);
        var request = new LeavePolicyRequest("Policy A", 10L, "MONTHLY",
                new BigDecimal("2.00"), null, null, true, 60, "Updated");
        when(leavePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(leaveTypeRepository.findById(10L)).thenReturn(Optional.of(leaveType));
        when(leavePolicyRepository.save(any(LeavePolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leavePolicyService.update(1L, request);

        assertEquals("Policy A", result.name());
        verify(leavePolicyRepository).save(any(LeavePolicy.class));
    }

    @Test
    void updateActive_shouldUpdateActiveStatus() {
        var leaveType = createLeaveType(10L, "Annual Leave", "AL");
        var policy = createPolicy(1L, "Policy A", leaveType);
        when(leavePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(leavePolicyRepository.save(any(LeavePolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leavePolicyService.updateActive(1L, false);

        assertFalse(result.active());
        verify(leavePolicyRepository).save(any(LeavePolicy.class));
    }

    @Test
    void updateActive_shouldThrowWhenNotFound() {
        when(leavePolicyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leavePolicyService.updateActive(999L, false));
        verify(leavePolicyRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeletePolicy() {
        var leaveType = createLeaveType(10L, "Annual Leave", "AL");
        var policy = createPolicy(1L, "Policy A", leaveType);
        when(leavePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));

        leavePolicyService.delete(1L);

        verify(leavePolicyRepository).delete(policy);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(leavePolicyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leavePolicyService.delete(999L));
        verify(leavePolicyRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var leaveType = createLeaveType(10L, "Annual Leave", "AL");
        var policy = createPolicy(1L, "Policy A", leaveType);
        when(leavePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));

        var result = leavePolicyService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("Policy A", result.name());
        assertEquals(10L, result.leaveTypeId());
        assertEquals("Annual Leave", result.leaveTypeName());
        assertEquals("AL", result.leaveTypeCode());
        assertEquals("MONTHLY", result.accrualFrequency());
        assertEquals(new BigDecimal("1.50"), result.accrualDays());
        assertEquals(new BigDecimal("30.00"), result.maxAccumulation());
        assertEquals(new BigDecimal("5.00"), result.carryForwardLimit());
        assertTrue(result.proRataForNewJoiners());
        assertEquals(90, result.minServiceDaysRequired());
        assertTrue(result.active());
        assertEquals("Test policy", result.description());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.updatedAt());
    }
}
