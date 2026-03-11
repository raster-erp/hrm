package com.raster.hrm.leavepolicy.service;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leavepolicy.dto.LeavePolicyRequest;
import com.raster.hrm.leavepolicy.dto.LeavePolicyResponse;
import com.raster.hrm.leavepolicy.entity.AccrualFrequency;
import com.raster.hrm.leavepolicy.entity.LeavePolicy;
import com.raster.hrm.leavepolicy.repository.LeavePolicyRepository;
import com.raster.hrm.leavetype.entity.LeaveType;
import com.raster.hrm.leavetype.repository.LeaveTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LeavePolicyService {

    private static final Logger log = LoggerFactory.getLogger(LeavePolicyService.class);

    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public LeavePolicyService(LeavePolicyRepository leavePolicyRepository,
                              LeaveTypeRepository leaveTypeRepository) {
        this.leavePolicyRepository = leavePolicyRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @Transactional(readOnly = true)
    public Page<LeavePolicyResponse> getAll(Pageable pageable) {
        return leavePolicyRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public LeavePolicyResponse getById(Long id) {
        var policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicy", "id", id));
        return mapToResponse(policy);
    }

    @Transactional(readOnly = true)
    public List<LeavePolicyResponse> getByLeaveTypeId(Long leaveTypeId) {
        return leavePolicyRepository.findByLeaveTypeId(leaveTypeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeavePolicyResponse> getActive() {
        return leavePolicyRepository.findByActive(true).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public LeavePolicyResponse create(LeavePolicyRequest request) {
        if (leavePolicyRepository.existsByName(request.name())) {
            throw new BadRequestException("Leave policy with name '" + request.name() + "' already exists");
        }

        var leaveType = leaveTypeRepository.findById(request.leaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", request.leaveTypeId()));

        var policy = new LeavePolicy();
        applyRequestToPolicy(request, policy, leaveType);

        var saved = leavePolicyRepository.save(policy);
        log.info("Created leave policy with id: {} name: {}", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    public LeavePolicyResponse update(Long id, LeavePolicyRequest request) {
        var policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicy", "id", id));

        if (!policy.getName().equals(request.name()) && leavePolicyRepository.existsByName(request.name())) {
            throw new BadRequestException("Leave policy with name '" + request.name() + "' already exists");
        }

        var leaveType = leaveTypeRepository.findById(request.leaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", request.leaveTypeId()));

        applyRequestToPolicy(request, policy, leaveType);

        var saved = leavePolicyRepository.save(policy);
        log.info("Updated leave policy with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public LeavePolicyResponse updateActive(Long id, boolean active) {
        var policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicy", "id", id));

        policy.setActive(active);
        var saved = leavePolicyRepository.save(policy);
        log.info("Updated active status of leave policy id: {} to {}", id, active);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicy", "id", id));
        leavePolicyRepository.delete(policy);
        log.info("Deleted leave policy with id: {}", id);
    }

    private void applyRequestToPolicy(LeavePolicyRequest request, LeavePolicy policy, LeaveType leaveType) {
        policy.setName(request.name());
        policy.setLeaveType(leaveType);
        policy.setAccrualFrequency(AccrualFrequency.valueOf(request.accrualFrequency()));
        policy.setAccrualDays(request.accrualDays());
        policy.setMaxAccumulation(request.maxAccumulation());
        policy.setCarryForwardLimit(request.carryForwardLimit());
        policy.setProRataForNewJoiners(request.proRataForNewJoiners() != null ? request.proRataForNewJoiners() : false);
        policy.setMinServiceDaysRequired(request.minServiceDaysRequired() != null ? request.minServiceDaysRequired() : 0);
        policy.setDescription(request.description());
    }

    private LeavePolicyResponse mapToResponse(LeavePolicy policy) {
        return new LeavePolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getLeaveType().getId(),
                policy.getLeaveType().getName(),
                policy.getLeaveType().getCode(),
                policy.getAccrualFrequency().name(),
                policy.getAccrualDays(),
                policy.getMaxAccumulation(),
                policy.getCarryForwardLimit(),
                policy.isProRataForNewJoiners(),
                policy.getMinServiceDaysRequired(),
                policy.isActive(),
                policy.getDescription(),
                policy.getCreatedAt(),
                policy.getUpdatedAt()
        );
    }
}
