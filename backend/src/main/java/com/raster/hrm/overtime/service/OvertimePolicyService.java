package com.raster.hrm.overtime.service;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.overtime.dto.OvertimePolicyRequest;
import com.raster.hrm.overtime.dto.OvertimePolicyResponse;
import com.raster.hrm.overtime.entity.OvertimePolicy;
import com.raster.hrm.overtime.entity.OvertimePolicyType;
import com.raster.hrm.overtime.repository.OvertimePolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OvertimePolicyService {

    private static final Logger log = LoggerFactory.getLogger(OvertimePolicyService.class);

    private final OvertimePolicyRepository overtimePolicyRepository;

    public OvertimePolicyService(OvertimePolicyRepository overtimePolicyRepository) {
        this.overtimePolicyRepository = overtimePolicyRepository;
    }

    @Transactional(readOnly = true)
    public Page<OvertimePolicyResponse> getAll(Pageable pageable) {
        return overtimePolicyRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public OvertimePolicyResponse getById(Long id) {
        var policy = overtimePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OvertimePolicy", "id", id));
        return mapToResponse(policy);
    }

    @Transactional(readOnly = true)
    public List<OvertimePolicyResponse> getByType(OvertimePolicyType type) {
        return overtimePolicyRepository.findByType(type).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OvertimePolicyResponse> getActive() {
        return overtimePolicyRepository.findByActive(true).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OvertimePolicyResponse create(OvertimePolicyRequest request) {
        if (overtimePolicyRepository.existsByName(request.name())) {
            throw new BadRequestException("Overtime policy with name '" + request.name() + "' already exists");
        }

        var policy = new OvertimePolicy();
        policy.setName(request.name());
        policy.setType(OvertimePolicyType.valueOf(request.type()));
        policy.setRateMultiplier(request.rateMultiplier());
        policy.setMinOvertimeMinutes(request.minOvertimeMinutes() != null ? request.minOvertimeMinutes() : 0);
        policy.setMaxOvertimeMinutesPerDay(request.maxOvertimeMinutesPerDay());
        policy.setMaxOvertimeMinutesPerMonth(request.maxOvertimeMinutesPerMonth());
        policy.setRequiresApproval(request.requiresApproval() != null ? request.requiresApproval() : true);
        policy.setDescription(request.description());

        var saved = overtimePolicyRepository.save(policy);
        log.info("Created overtime policy with id: {} name: {}", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    public OvertimePolicyResponse update(Long id, OvertimePolicyRequest request) {
        var policy = overtimePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OvertimePolicy", "id", id));

        if (!policy.getName().equals(request.name()) && overtimePolicyRepository.existsByName(request.name())) {
            throw new BadRequestException("Overtime policy with name '" + request.name() + "' already exists");
        }

        policy.setName(request.name());
        policy.setType(OvertimePolicyType.valueOf(request.type()));
        policy.setRateMultiplier(request.rateMultiplier());
        policy.setMinOvertimeMinutes(request.minOvertimeMinutes() != null ? request.minOvertimeMinutes() : 0);
        policy.setMaxOvertimeMinutesPerDay(request.maxOvertimeMinutesPerDay());
        policy.setMaxOvertimeMinutesPerMonth(request.maxOvertimeMinutesPerMonth());
        policy.setRequiresApproval(request.requiresApproval() != null ? request.requiresApproval() : true);
        policy.setDescription(request.description());

        var saved = overtimePolicyRepository.save(policy);
        log.info("Updated overtime policy with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public OvertimePolicyResponse updateActive(Long id, boolean active) {
        var policy = overtimePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OvertimePolicy", "id", id));

        policy.setActive(active);
        var saved = overtimePolicyRepository.save(policy);
        log.info("Updated active status of overtime policy id: {} to {}", id, active);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var policy = overtimePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OvertimePolicy", "id", id));
        overtimePolicyRepository.delete(policy);
        log.info("Deleted overtime policy with id: {}", id);
    }

    private OvertimePolicyResponse mapToResponse(OvertimePolicy policy) {
        return new OvertimePolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getType().name(),
                policy.getRateMultiplier(),
                policy.getMinOvertimeMinutes(),
                policy.getMaxOvertimeMinutesPerDay(),
                policy.getMaxOvertimeMinutesPerMonth(),
                policy.isRequiresApproval(),
                policy.isActive(),
                policy.getDescription(),
                policy.getCreatedAt(),
                policy.getUpdatedAt()
        );
    }
}
