package com.raster.hrm.separation.service;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.separation.dto.NoDuesRequest;
import com.raster.hrm.separation.dto.NoDuesResponse;
import com.raster.hrm.separation.entity.NoDues;
import com.raster.hrm.separation.repository.NoDuesRepository;
import com.raster.hrm.separation.repository.SeparationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NoDuesService {

    private static final Logger log = LoggerFactory.getLogger(NoDuesService.class);

    private final NoDuesRepository noDuesRepository;
    private final SeparationRepository separationRepository;

    public NoDuesService(NoDuesRepository noDuesRepository,
                         SeparationRepository separationRepository) {
        this.noDuesRepository = noDuesRepository;
        this.separationRepository = separationRepository;
    }

    @Transactional(readOnly = true)
    public List<NoDuesResponse> getBySeparationId(Long separationId) {
        return noDuesRepository.findBySeparationId(separationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public NoDuesResponse create(NoDuesRequest request) {
        var separation = separationRepository.findById(request.separationId())
                .orElseThrow(() -> new ResourceNotFoundException("Separation", "id", request.separationId()));

        var noDues = new NoDues();
        noDues.setSeparation(separation);
        noDues.setDepartment(request.department());
        noDues.setAmountDue(request.amountDue() != null ? request.amountDue() : BigDecimal.ZERO);
        noDues.setNotes(request.notes());

        var saved = noDuesRepository.save(noDues);
        log.info("Created no-dues record with id: {} for separation id: {}", saved.getId(), separation.getId());
        return mapToResponse(saved);
    }

    public NoDuesResponse clearDepartment(Long id, String clearedBy) {
        var noDues = noDuesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NoDues", "id", id));

        if (noDues.isCleared()) {
            throw new BadRequestException("No-dues record is already cleared");
        }

        noDues.setCleared(true);
        noDues.setClearedBy(clearedBy);
        noDues.setClearedAt(LocalDateTime.now());

        var saved = noDuesRepository.save(noDues);
        log.info("Cleared no-dues record with id: {} by {}", saved.getId(), clearedBy);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var noDues = noDuesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NoDues", "id", id));

        if (noDues.isCleared()) {
            throw new BadRequestException("Cannot delete a cleared no-dues record");
        }

        noDuesRepository.delete(noDues);
        log.info("Deleted no-dues record with id: {}", id);
    }

    private NoDuesResponse mapToResponse(NoDues noDues) {
        return new NoDuesResponse(
                noDues.getId(),
                noDues.getSeparation().getId(),
                noDues.getDepartment(),
                noDues.isCleared(),
                noDues.getClearedBy(),
                noDues.getClearedAt(),
                noDues.getAmountDue(),
                noDues.getNotes(),
                noDues.getCreatedAt(),
                noDues.getUpdatedAt()
        );
    }
}
