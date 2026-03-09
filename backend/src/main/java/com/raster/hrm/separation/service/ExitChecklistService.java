package com.raster.hrm.separation.service;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.separation.dto.ExitChecklistRequest;
import com.raster.hrm.separation.dto.ExitChecklistResponse;
import com.raster.hrm.separation.entity.ExitChecklist;
import com.raster.hrm.separation.repository.ExitChecklistRepository;
import com.raster.hrm.separation.repository.SeparationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ExitChecklistService {

    private static final Logger log = LoggerFactory.getLogger(ExitChecklistService.class);

    private final ExitChecklistRepository exitChecklistRepository;
    private final SeparationRepository separationRepository;

    public ExitChecklistService(ExitChecklistRepository exitChecklistRepository,
                                SeparationRepository separationRepository) {
        this.exitChecklistRepository = exitChecklistRepository;
        this.separationRepository = separationRepository;
    }

    @Transactional(readOnly = true)
    public List<ExitChecklistResponse> getBySeparationId(Long separationId) {
        return exitChecklistRepository.findBySeparationId(separationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ExitChecklistResponse create(ExitChecklistRequest request) {
        var separation = separationRepository.findById(request.separationId())
                .orElseThrow(() -> new ResourceNotFoundException("Separation", "id", request.separationId()));

        var exitChecklist = new ExitChecklist();
        exitChecklist.setSeparation(separation);
        exitChecklist.setItemName(request.itemName());
        exitChecklist.setDepartment(request.department());
        exitChecklist.setNotes(request.notes());

        var saved = exitChecklistRepository.save(exitChecklist);
        log.info("Created exit checklist item with id: {} for separation id: {}", saved.getId(), separation.getId());
        return mapToResponse(saved);
    }

    public ExitChecklistResponse clearItem(Long id, String clearedBy) {
        var exitChecklist = exitChecklistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExitChecklist", "id", id));

        if (exitChecklist.isCleared()) {
            throw new BadRequestException("Exit checklist item is already cleared");
        }

        exitChecklist.setCleared(true);
        exitChecklist.setClearedBy(clearedBy);
        exitChecklist.setClearedAt(LocalDateTime.now());

        var saved = exitChecklistRepository.save(exitChecklist);
        log.info("Cleared exit checklist item with id: {} by {}", saved.getId(), clearedBy);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var exitChecklist = exitChecklistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExitChecklist", "id", id));

        if (exitChecklist.isCleared()) {
            throw new BadRequestException("Cannot delete a cleared exit checklist item");
        }

        exitChecklistRepository.delete(exitChecklist);
        log.info("Deleted exit checklist item with id: {}", id);
    }

    private ExitChecklistResponse mapToResponse(ExitChecklist exitChecklist) {
        return new ExitChecklistResponse(
                exitChecklist.getId(),
                exitChecklist.getSeparation().getId(),
                exitChecklist.getItemName(),
                exitChecklist.getDepartment(),
                exitChecklist.isCleared(),
                exitChecklist.getClearedBy(),
                exitChecklist.getClearedAt(),
                exitChecklist.getNotes(),
                exitChecklist.getCreatedAt(),
                exitChecklist.getUpdatedAt()
        );
    }
}
