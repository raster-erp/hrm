package com.raster.hrm.shift.service;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.shift.dto.ShiftRequest;
import com.raster.hrm.shift.dto.ShiftResponse;
import com.raster.hrm.shift.entity.Shift;
import com.raster.hrm.shift.entity.ShiftType;
import com.raster.hrm.shift.repository.ShiftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ShiftService {

    private static final Logger log = LoggerFactory.getLogger(ShiftService.class);

    private final ShiftRepository shiftRepository;

    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    @Transactional(readOnly = true)
    public Page<ShiftResponse> getAll(Pageable pageable) {
        return shiftRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ShiftResponse getById(Long id) {
        var shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", id));
        return mapToResponse(shift);
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> getByType(ShiftType type) {
        return shiftRepository.findByType(type).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> getActive() {
        return shiftRepository.findByActive(true).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ShiftResponse create(ShiftRequest request) {
        if (shiftRepository.existsByName(request.name())) {
            throw new BadRequestException("Shift with name '" + request.name() + "' already exists");
        }

        var shift = new Shift();
        shift.setName(request.name());
        shift.setType(ShiftType.valueOf(request.type()));
        shift.setStartTime(request.startTime());
        shift.setEndTime(request.endTime());
        shift.setBreakDurationMinutes(request.breakDurationMinutes() != null ? request.breakDurationMinutes() : 0);
        shift.setGracePeriodMinutes(request.gracePeriodMinutes() != null ? request.gracePeriodMinutes() : 0);
        shift.setDescription(request.description());

        var saved = shiftRepository.save(shift);
        log.info("Created shift with id: {} name: {}", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    public ShiftResponse update(Long id, ShiftRequest request) {
        var shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", id));

        if (!shift.getName().equals(request.name()) && shiftRepository.existsByName(request.name())) {
            throw new BadRequestException("Shift with name '" + request.name() + "' already exists");
        }

        shift.setName(request.name());
        shift.setType(ShiftType.valueOf(request.type()));
        shift.setStartTime(request.startTime());
        shift.setEndTime(request.endTime());
        shift.setBreakDurationMinutes(request.breakDurationMinutes() != null ? request.breakDurationMinutes() : 0);
        shift.setGracePeriodMinutes(request.gracePeriodMinutes() != null ? request.gracePeriodMinutes() : 0);
        shift.setDescription(request.description());

        var saved = shiftRepository.save(shift);
        log.info("Updated shift with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public ShiftResponse updateActive(Long id, boolean active) {
        var shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", id));

        shift.setActive(active);
        var saved = shiftRepository.save(shift);
        log.info("Updated active status of shift id: {} to {}", id, active);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", id));
        shiftRepository.delete(shift);
        log.info("Deleted shift with id: {}", id);
    }

    private ShiftResponse mapToResponse(Shift shift) {
        return new ShiftResponse(
                shift.getId(),
                shift.getName(),
                shift.getType().name(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getBreakDurationMinutes(),
                shift.getGracePeriodMinutes(),
                shift.getDescription(),
                shift.isActive(),
                shift.getCreatedAt(),
                shift.getUpdatedAt()
        );
    }
}
