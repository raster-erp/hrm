package com.raster.hrm.uniform.service;

import com.raster.hrm.uniform.dto.UniformRequest;
import com.raster.hrm.uniform.dto.UniformResponse;
import com.raster.hrm.uniform.entity.Uniform;
import com.raster.hrm.uniform.repository.UniformRepository;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UniformService {

    private static final Logger log = LoggerFactory.getLogger(UniformService.class);

    private final UniformRepository uniformRepository;

    public UniformService(UniformRepository uniformRepository) {
        this.uniformRepository = uniformRepository;
    }

    @Transactional(readOnly = true)
    public Page<UniformResponse> getAll(Pageable pageable) {
        return uniformRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public UniformResponse getById(Long id) {
        var uniform = uniformRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Uniform", "id", id));
        return mapToResponse(uniform);
    }

    public UniformResponse create(UniformRequest request) {
        var uniform = new Uniform();
        mapRequestToEntity(request, uniform);

        var saved = uniformRepository.save(uniform);
        log.info("Created uniform with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public UniformResponse update(Long id, UniformRequest request) {
        var uniform = uniformRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Uniform", "id", id));

        mapRequestToEntity(request, uniform);

        var saved = uniformRepository.save(uniform);
        log.info("Updated uniform with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public void deactivate(Long id) {
        var uniform = uniformRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Uniform", "id", id));

        uniform.setActive(false);
        uniformRepository.save(uniform);
        log.info("Deactivated uniform with id: {}", id);
    }

    private void mapRequestToEntity(UniformRequest request, Uniform uniform) {
        uniform.setName(request.name());
        uniform.setType(request.type());
        uniform.setSize(request.size());
        uniform.setDescription(request.description());
    }

    private UniformResponse mapToResponse(Uniform uniform) {
        return new UniformResponse(
                uniform.getId(),
                uniform.getName(),
                uniform.getType(),
                uniform.getSize(),
                uniform.getDescription(),
                uniform.isActive(),
                uniform.getCreatedAt(),
                uniform.getUpdatedAt()
        );
    }
}
