package com.raster.hrm.rotationpattern.service;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.rotationpattern.dto.RotationPatternRequest;
import com.raster.hrm.rotationpattern.dto.RotationPatternResponse;
import com.raster.hrm.rotationpattern.entity.RotationPattern;
import com.raster.hrm.rotationpattern.repository.RotationPatternRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RotationPatternService {

    private static final Logger log = LoggerFactory.getLogger(RotationPatternService.class);

    private final RotationPatternRepository rotationPatternRepository;

    public RotationPatternService(RotationPatternRepository rotationPatternRepository) {
        this.rotationPatternRepository = rotationPatternRepository;
    }

    @Transactional(readOnly = true)
    public Page<RotationPatternResponse> getAll(Pageable pageable) {
        return rotationPatternRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public RotationPatternResponse getById(Long id) {
        var pattern = rotationPatternRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RotationPattern", "id", id));
        return mapToResponse(pattern);
    }

    public RotationPatternResponse create(RotationPatternRequest request) {
        if (rotationPatternRepository.existsByName(request.name())) {
            throw new BadRequestException("Rotation pattern with name '" + request.name() + "' already exists");
        }

        var pattern = new RotationPattern();
        pattern.setName(request.name());
        pattern.setDescription(request.description());
        pattern.setRotationDays(request.rotationDays());
        pattern.setShiftSequence(request.shiftSequence());

        var saved = rotationPatternRepository.save(pattern);
        log.info("Created rotation pattern with id: {} name: {}", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    public RotationPatternResponse update(Long id, RotationPatternRequest request) {
        var pattern = rotationPatternRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RotationPattern", "id", id));

        if (!pattern.getName().equals(request.name()) && rotationPatternRepository.existsByName(request.name())) {
            throw new BadRequestException("Rotation pattern with name '" + request.name() + "' already exists");
        }

        pattern.setName(request.name());
        pattern.setDescription(request.description());
        pattern.setRotationDays(request.rotationDays());
        pattern.setShiftSequence(request.shiftSequence());

        var saved = rotationPatternRepository.save(pattern);
        log.info("Updated rotation pattern with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var pattern = rotationPatternRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RotationPattern", "id", id));
        rotationPatternRepository.delete(pattern);
        log.info("Deleted rotation pattern with id: {}", id);
    }

    private RotationPatternResponse mapToResponse(RotationPattern pattern) {
        return new RotationPatternResponse(
                pattern.getId(),
                pattern.getName(),
                pattern.getDescription(),
                pattern.getRotationDays(),
                pattern.getShiftSequence(),
                pattern.getCreatedAt(),
                pattern.getUpdatedAt()
        );
    }
}
