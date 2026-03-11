package com.raster.hrm.leavepolicyassignment;

import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leavepolicy.entity.LeavePolicy;
import com.raster.hrm.leavepolicy.repository.LeavePolicyRepository;
import com.raster.hrm.leavepolicyassignment.dto.LeavePolicyAssignmentRequest;
import com.raster.hrm.leavepolicyassignment.entity.AssignmentType;
import com.raster.hrm.leavepolicyassignment.entity.LeavePolicyAssignment;
import com.raster.hrm.leavepolicyassignment.repository.LeavePolicyAssignmentRepository;
import com.raster.hrm.leavepolicyassignment.service.LeavePolicyAssignmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
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
class LeavePolicyAssignmentServiceTest {

    @Mock
    private LeavePolicyAssignmentRepository leavePolicyAssignmentRepository;

    @Mock
    private LeavePolicyRepository leavePolicyRepository;

    @InjectMocks
    private LeavePolicyAssignmentService leavePolicyAssignmentService;

    private LeavePolicy createLeavePolicy(Long id, String name) {
        var policy = new LeavePolicy();
        policy.setId(id);
        policy.setName(name);
        return policy;
    }

    private LeavePolicyAssignment createAssignment(Long id, LeavePolicy leavePolicy, AssignmentType type) {
        var assignment = new LeavePolicyAssignment();
        assignment.setId(id);
        assignment.setLeavePolicy(leavePolicy);
        assignment.setAssignmentType(type);
        assignment.setDepartmentId(type == AssignmentType.DEPARTMENT ? 10L : null);
        assignment.setDesignationId(type == AssignmentType.DESIGNATION ? 20L : null);
        assignment.setEmployeeId(type == AssignmentType.INDIVIDUAL ? 30L : null);
        assignment.setEffectiveFrom(LocalDate.of(2024, 1, 1));
        assignment.setEffectiveTo(LocalDate.of(2024, 12, 31));
        assignment.setActive(true);
        assignment.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        assignment.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return assignment;
    }

    private LeavePolicyAssignmentRequest createRequest() {
        return new LeavePolicyAssignmentRequest(
                1L, "DEPARTMENT", 10L, null, null,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );
    }

    @Test
    void getAll_shouldReturnPageOfAssignments() {
        var policy = createLeavePolicy(1L, "Annual Leave");
        var assignments = List.of(
                createAssignment(1L, policy, AssignmentType.DEPARTMENT),
                createAssignment(2L, policy, AssignmentType.INDIVIDUAL)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(assignments, pageable, 2);
        when(leavePolicyAssignmentRepository.findAll(pageable)).thenReturn(page);

        var result = leavePolicyAssignmentService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Annual Leave", result.getContent().get(0).leavePolicyName());
        assertEquals("DEPARTMENT", result.getContent().get(0).assignmentType());
        assertEquals("INDIVIDUAL", result.getContent().get(1).assignmentType());
        verify(leavePolicyAssignmentRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<LeavePolicyAssignment>(List.of(), pageable, 0);
        when(leavePolicyAssignmentRepository.findAll(pageable)).thenReturn(page);

        var result = leavePolicyAssignmentService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnAssignment() {
        var policy = createLeavePolicy(1L, "Annual Leave");
        var assignment = createAssignment(1L, policy, AssignmentType.DEPARTMENT);
        when(leavePolicyAssignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        var result = leavePolicyAssignmentService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.leavePolicyId());
        assertEquals("Annual Leave", result.leavePolicyName());
        assertEquals("DEPARTMENT", result.assignmentType());
        assertEquals(10L, result.departmentId());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(leavePolicyAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leavePolicyAssignmentService.getById(999L));
    }

    @Test
    void getByPolicyId_shouldReturnAssignments() {
        var policy = createLeavePolicy(1L, "Annual Leave");
        var assignments = List.of(createAssignment(1L, policy, AssignmentType.DEPARTMENT));
        when(leavePolicyAssignmentRepository.findByLeavePolicyId(1L)).thenReturn(assignments);

        var result = leavePolicyAssignmentService.getByPolicyId(1L);

        assertEquals(1, result.size());
        assertEquals("Annual Leave", result.get(0).leavePolicyName());
    }

    @Test
    void getByPolicyId_shouldReturnEmptyList() {
        when(leavePolicyAssignmentRepository.findByLeavePolicyId(999L)).thenReturn(List.of());

        var result = leavePolicyAssignmentService.getByPolicyId(999L);

        assertEquals(0, result.size());
    }

    @Test
    void getByAssignmentType_shouldReturnAssignments() {
        var policy = createLeavePolicy(1L, "Annual Leave");
        var assignments = List.of(createAssignment(1L, policy, AssignmentType.DEPARTMENT));
        when(leavePolicyAssignmentRepository.findByAssignmentType(AssignmentType.DEPARTMENT)).thenReturn(assignments);

        var result = leavePolicyAssignmentService.getByAssignmentType(AssignmentType.DEPARTMENT);

        assertEquals(1, result.size());
        assertEquals("DEPARTMENT", result.get(0).assignmentType());
    }

    @Test
    void getByAssignmentType_shouldReturnEmptyList() {
        when(leavePolicyAssignmentRepository.findByAssignmentType(AssignmentType.DESIGNATION)).thenReturn(List.of());

        var result = leavePolicyAssignmentService.getByAssignmentType(AssignmentType.DESIGNATION);

        assertEquals(0, result.size());
    }

    @Test
    void getByDepartmentId_shouldReturnAssignments() {
        var policy = createLeavePolicy(1L, "Annual Leave");
        var assignments = List.of(createAssignment(1L, policy, AssignmentType.DEPARTMENT));
        when(leavePolicyAssignmentRepository.findByDepartmentId(10L)).thenReturn(assignments);

        var result = leavePolicyAssignmentService.getByDepartmentId(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).departmentId());
    }

    @Test
    void getByDepartmentId_shouldReturnEmptyList() {
        when(leavePolicyAssignmentRepository.findByDepartmentId(999L)).thenReturn(List.of());

        var result = leavePolicyAssignmentService.getByDepartmentId(999L);

        assertEquals(0, result.size());
    }

    @Test
    void getByDesignationId_shouldReturnAssignments() {
        var policy = createLeavePolicy(1L, "Annual Leave");
        var assignment = createAssignment(1L, policy, AssignmentType.DESIGNATION);
        when(leavePolicyAssignmentRepository.findByDesignationId(20L)).thenReturn(List.of(assignment));

        var result = leavePolicyAssignmentService.getByDesignationId(20L);

        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).designationId());
    }

    @Test
    void getByDesignationId_shouldReturnEmptyList() {
        when(leavePolicyAssignmentRepository.findByDesignationId(999L)).thenReturn(List.of());

        var result = leavePolicyAssignmentService.getByDesignationId(999L);

        assertEquals(0, result.size());
    }

    @Test
    void getByEmployeeId_shouldReturnAssignments() {
        var policy = createLeavePolicy(1L, "Annual Leave");
        var assignment = createAssignment(1L, policy, AssignmentType.INDIVIDUAL);
        when(leavePolicyAssignmentRepository.findByEmployeeId(30L)).thenReturn(List.of(assignment));

        var result = leavePolicyAssignmentService.getByEmployeeId(30L);

        assertEquals(1, result.size());
        assertEquals(30L, result.get(0).employeeId());
    }

    @Test
    void getByEmployeeId_shouldReturnEmptyList() {
        when(leavePolicyAssignmentRepository.findByEmployeeId(999L)).thenReturn(List.of());

        var result = leavePolicyAssignmentService.getByEmployeeId(999L);

        assertEquals(0, result.size());
    }

    @Test
    void create_shouldCreateAndReturnAssignment() {
        var request = createRequest();
        var policy = createLeavePolicy(1L, "Annual Leave");
        when(leavePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(leavePolicyAssignmentRepository.save(any(LeavePolicyAssignment.class))).thenAnswer(invocation -> {
            LeavePolicyAssignment a = invocation.getArgument(0);
            a.setId(1L);
            a.setCreatedAt(LocalDateTime.now());
            a.setUpdatedAt(LocalDateTime.now());
            return a;
        });

        var result = leavePolicyAssignmentService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.leavePolicyId());
        assertEquals("Annual Leave", result.leavePolicyName());
        assertEquals("DEPARTMENT", result.assignmentType());
        assertEquals(10L, result.departmentId());
        assertNull(result.designationId());
        assertNull(result.employeeId());
        assertEquals(LocalDate.of(2024, 1, 1), result.effectiveFrom());
        assertEquals(LocalDate.of(2024, 12, 31), result.effectiveTo());
        verify(leavePolicyAssignmentRepository).save(any(LeavePolicyAssignment.class));
    }

    @Test
    void create_shouldThrowWhenPolicyNotFound() {
        var request = createRequest();
        when(leavePolicyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leavePolicyAssignmentService.create(request));
        verify(leavePolicyAssignmentRepository, never()).save(any());
    }

    @Test
    void create_shouldCreateIndividualAssignment() {
        var request = new LeavePolicyAssignmentRequest(
                1L, "INDIVIDUAL", null, null, 30L,
                LocalDate.of(2024, 1, 1), null
        );
        var policy = createLeavePolicy(1L, "Annual Leave");
        when(leavePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(leavePolicyAssignmentRepository.save(any(LeavePolicyAssignment.class))).thenAnswer(invocation -> {
            LeavePolicyAssignment a = invocation.getArgument(0);
            a.setId(2L);
            a.setCreatedAt(LocalDateTime.now());
            a.setUpdatedAt(LocalDateTime.now());
            return a;
        });

        var result = leavePolicyAssignmentService.create(request);

        assertEquals("INDIVIDUAL", result.assignmentType());
        assertEquals(30L, result.employeeId());
        assertNull(result.departmentId());
        assertNull(result.designationId());
        assertNull(result.effectiveTo());
    }

    @Test
    void update_shouldUpdateAndReturnAssignment() {
        var policy = createLeavePolicy(1L, "Annual Leave");
        var assignment = createAssignment(1L, policy, AssignmentType.DEPARTMENT);
        var newPolicy = createLeavePolicy(2L, "Sick Leave");
        var request = new LeavePolicyAssignmentRequest(
                2L, "DESIGNATION", null, 25L, null,
                LocalDate.of(2024, 6, 1), LocalDate.of(2025, 5, 31)
        );
        when(leavePolicyAssignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(leavePolicyRepository.findById(2L)).thenReturn(Optional.of(newPolicy));
        when(leavePolicyAssignmentRepository.save(any(LeavePolicyAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leavePolicyAssignmentService.update(1L, request);

        assertNotNull(result);
        assertEquals(2L, result.leavePolicyId());
        assertEquals("Sick Leave", result.leavePolicyName());
        assertEquals("DESIGNATION", result.assignmentType());
        assertEquals(25L, result.designationId());
        assertNull(result.departmentId());
        assertEquals(LocalDate.of(2024, 6, 1), result.effectiveFrom());
        assertEquals(LocalDate.of(2025, 5, 31), result.effectiveTo());
        verify(leavePolicyAssignmentRepository).save(any(LeavePolicyAssignment.class));
    }

    @Test
    void update_shouldThrowWhenAssignmentNotFound() {
        var request = createRequest();
        when(leavePolicyAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leavePolicyAssignmentService.update(999L, request));
        verify(leavePolicyAssignmentRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenPolicyNotFound() {
        var policy = createLeavePolicy(1L, "Annual Leave");
        var assignment = createAssignment(1L, policy, AssignmentType.DEPARTMENT);
        var request = new LeavePolicyAssignmentRequest(
                999L, "DEPARTMENT", 10L, null, null,
                LocalDate.of(2024, 1, 1), null
        );
        when(leavePolicyAssignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(leavePolicyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leavePolicyAssignmentService.update(1L, request));
        verify(leavePolicyAssignmentRepository, never()).save(any());
    }

    @Test
    void updateActive_shouldUpdateActiveStatus() {
        var policy = createLeavePolicy(1L, "Annual Leave");
        var assignment = createAssignment(1L, policy, AssignmentType.DEPARTMENT);
        when(leavePolicyAssignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(leavePolicyAssignmentRepository.save(any(LeavePolicyAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leavePolicyAssignmentService.updateActive(1L, false);

        assertFalse(result.active());
        verify(leavePolicyAssignmentRepository).save(any(LeavePolicyAssignment.class));
    }

    @Test
    void updateActive_shouldThrowWhenNotFound() {
        when(leavePolicyAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leavePolicyAssignmentService.updateActive(999L, false));
        verify(leavePolicyAssignmentRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteAssignment() {
        var policy = createLeavePolicy(1L, "Annual Leave");
        var assignment = createAssignment(1L, policy, AssignmentType.DEPARTMENT);
        when(leavePolicyAssignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        leavePolicyAssignmentService.delete(1L);

        verify(leavePolicyAssignmentRepository).delete(assignment);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(leavePolicyAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leavePolicyAssignmentService.delete(999L));
        verify(leavePolicyAssignmentRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var policy = createLeavePolicy(1L, "Annual Leave");
        var assignment = createAssignment(1L, policy, AssignmentType.DEPARTMENT);
        when(leavePolicyAssignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        var result = leavePolicyAssignmentService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals(1L, result.leavePolicyId());
        assertEquals("Annual Leave", result.leavePolicyName());
        assertEquals("DEPARTMENT", result.assignmentType());
        assertEquals(10L, result.departmentId());
        assertNull(result.designationId());
        assertNull(result.employeeId());
        assertEquals(LocalDate.of(2024, 1, 1), result.effectiveFrom());
        assertEquals(LocalDate.of(2024, 12, 31), result.effectiveTo());
        assertTrue(result.active());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.updatedAt());
    }
}
