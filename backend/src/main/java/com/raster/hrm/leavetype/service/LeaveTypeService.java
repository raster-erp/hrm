package com.raster.hrm.leavetype.service;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leavetype.dto.LeaveTypeRequest;
import com.raster.hrm.leavetype.dto.LeaveTypeResponse;
import com.raster.hrm.leavetype.entity.LeaveType;
import com.raster.hrm.leavetype.entity.LeaveTypeCategory;
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
public class LeaveTypeService {

    private static final Logger log = LoggerFactory.getLogger(LeaveTypeService.class);

    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveTypeService(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @Transactional(readOnly = true)
    public Page<LeaveTypeResponse> getAll(Pageable pageable) {
        return leaveTypeRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public LeaveTypeResponse getById(Long id) {
        var leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));
        return mapToResponse(leaveType);
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getByCategory(LeaveTypeCategory category) {
        return leaveTypeRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getActive() {
        return leaveTypeRepository.findByActive(true).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public LeaveTypeResponse create(LeaveTypeRequest request) {
        if (leaveTypeRepository.existsByCode(request.code())) {
            throw new BadRequestException("Leave type with code '" + request.code() + "' already exists");
        }

        var leaveType = new LeaveType();
        leaveType.setCode(request.code());
        leaveType.setName(request.name());
        leaveType.setCategory(LeaveTypeCategory.valueOf(request.category()));
        leaveType.setDescription(request.description());

        var saved = leaveTypeRepository.save(leaveType);
        log.info("Created leave type with id: {} code: {}", saved.getId(), saved.getCode());
        return mapToResponse(saved);
    }

    public LeaveTypeResponse update(Long id, LeaveTypeRequest request) {
        var leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));

        if (!leaveType.getCode().equals(request.code()) && leaveTypeRepository.existsByCode(request.code())) {
            throw new BadRequestException("Leave type with code '" + request.code() + "' already exists");
        }

        leaveType.setCode(request.code());
        leaveType.setName(request.name());
        leaveType.setCategory(LeaveTypeCategory.valueOf(request.category()));
        leaveType.setDescription(request.description());

        var saved = leaveTypeRepository.save(leaveType);
        log.info("Updated leave type with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public LeaveTypeResponse updateActive(Long id, boolean active) {
        var leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));

        leaveType.setActive(active);
        var saved = leaveTypeRepository.save(leaveType);
        log.info("Updated active status of leave type id: {} to {}", id, active);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));
        leaveTypeRepository.delete(leaveType);
        log.info("Deleted leave type with id: {}", id);
    }

    private LeaveTypeResponse mapToResponse(LeaveType leaveType) {
        return new LeaveTypeResponse(
                leaveType.getId(),
                leaveType.getCode(),
                leaveType.getName(),
                leaveType.getCategory().name(),
                leaveType.getDescription(),
                leaveType.isActive(),
                leaveType.getCreatedAt(),
                leaveType.getUpdatedAt()
        );
    }
}
