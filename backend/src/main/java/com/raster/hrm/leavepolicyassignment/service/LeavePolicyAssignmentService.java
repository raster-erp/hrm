package com.raster.hrm.leavepolicyassignment.service;

import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leavepolicy.repository.LeavePolicyRepository;
import com.raster.hrm.leavepolicyassignment.dto.LeavePolicyAssignmentRequest;
import com.raster.hrm.leavepolicyassignment.dto.LeavePolicyAssignmentResponse;
import com.raster.hrm.leavepolicyassignment.entity.AssignmentType;
import com.raster.hrm.leavepolicyassignment.entity.LeavePolicyAssignment;
import com.raster.hrm.leavepolicyassignment.repository.LeavePolicyAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LeavePolicyAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(LeavePolicyAssignmentService.class);

    private final LeavePolicyAssignmentRepository leavePolicyAssignmentRepository;
    private final LeavePolicyRepository leavePolicyRepository;

    public LeavePolicyAssignmentService(LeavePolicyAssignmentRepository leavePolicyAssignmentRepository,
                                        LeavePolicyRepository leavePolicyRepository) {
        this.leavePolicyAssignmentRepository = leavePolicyAssignmentRepository;
        this.leavePolicyRepository = leavePolicyRepository;
    }

    @Transactional(readOnly = true)
    public Page<LeavePolicyAssignmentResponse> getAll(Pageable pageable) {
        return leavePolicyAssignmentRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public LeavePolicyAssignmentResponse getById(Long id) {
        var assignment = leavePolicyAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicyAssignment", "id", id));
        return mapToResponse(assignment);
    }

    @Transactional(readOnly = true)
    public List<LeavePolicyAssignmentResponse> getByPolicyId(Long policyId) {
        return leavePolicyAssignmentRepository.findByLeavePolicyId(policyId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeavePolicyAssignmentResponse> getByAssignmentType(AssignmentType assignmentType) {
        return leavePolicyAssignmentRepository.findByAssignmentType(assignmentType).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeavePolicyAssignmentResponse> getByDepartmentId(Long departmentId) {
        return leavePolicyAssignmentRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeavePolicyAssignmentResponse> getByDesignationId(Long designationId) {
        return leavePolicyAssignmentRepository.findByDesignationId(designationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeavePolicyAssignmentResponse> getByEmployeeId(Long employeeId) {
        return leavePolicyAssignmentRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public LeavePolicyAssignmentResponse create(LeavePolicyAssignmentRequest request) {
        var leavePolicy = leavePolicyRepository.findById(request.leavePolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicy", "id", request.leavePolicyId()));

        var assignment = new LeavePolicyAssignment();
        assignment.setLeavePolicy(leavePolicy);
        assignment.setAssignmentType(AssignmentType.valueOf(request.assignmentType()));
        assignment.setDepartmentId(request.departmentId());
        assignment.setDesignationId(request.designationId());
        assignment.setEmployeeId(request.employeeId());
        assignment.setEffectiveFrom(request.effectiveFrom());
        assignment.setEffectiveTo(request.effectiveTo());

        var saved = leavePolicyAssignmentRepository.save(assignment);
        log.info("Created leave policy assignment with id: {} for policy: {}", saved.getId(), leavePolicy.getName());
        return mapToResponse(saved);
    }

    public LeavePolicyAssignmentResponse update(Long id, LeavePolicyAssignmentRequest request) {
        var assignment = leavePolicyAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicyAssignment", "id", id));

        var leavePolicy = leavePolicyRepository.findById(request.leavePolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicy", "id", request.leavePolicyId()));

        assignment.setLeavePolicy(leavePolicy);
        assignment.setAssignmentType(AssignmentType.valueOf(request.assignmentType()));
        assignment.setDepartmentId(request.departmentId());
        assignment.setDesignationId(request.designationId());
        assignment.setEmployeeId(request.employeeId());
        assignment.setEffectiveFrom(request.effectiveFrom());
        assignment.setEffectiveTo(request.effectiveTo());

        var saved = leavePolicyAssignmentRepository.save(assignment);
        log.info("Updated leave policy assignment with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public LeavePolicyAssignmentResponse updateActive(Long id, boolean active) {
        var assignment = leavePolicyAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicyAssignment", "id", id));

        assignment.setActive(active);
        var saved = leavePolicyAssignmentRepository.save(assignment);
        log.info("Updated active status of leave policy assignment id: {} to {}", id, active);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var assignment = leavePolicyAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicyAssignment", "id", id));
        leavePolicyAssignmentRepository.delete(assignment);
        log.info("Deleted leave policy assignment with id: {}", id);
    }

    private LeavePolicyAssignmentResponse mapToResponse(LeavePolicyAssignment assignment) {
        return new LeavePolicyAssignmentResponse(
                assignment.getId(),
                assignment.getLeavePolicy().getId(),
                assignment.getLeavePolicy().getName(),
                assignment.getAssignmentType().name(),
                assignment.getDepartmentId(),
                assignment.getDesignationId(),
                assignment.getEmployeeId(),
                assignment.getEffectiveFrom(),
                assignment.getEffectiveTo(),
                assignment.isActive(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }
}
