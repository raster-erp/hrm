package com.raster.hrm.salarystructure.service;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.salarycomponent.entity.SalaryComputationType;
import com.raster.hrm.salarycomponent.repository.SalaryComponentRepository;
import com.raster.hrm.salarystructure.dto.SalaryStructureComponentRequest;
import com.raster.hrm.salarystructure.dto.SalaryStructureComponentResponse;
import com.raster.hrm.salarystructure.dto.SalaryStructureRequest;
import com.raster.hrm.salarystructure.dto.SalaryStructureResponse;
import com.raster.hrm.salarystructure.entity.SalaryStructure;
import com.raster.hrm.salarystructure.entity.SalaryStructureComponent;
import com.raster.hrm.salarystructure.repository.SalaryStructureComponentRepository;
import com.raster.hrm.salarystructure.repository.SalaryStructureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SalaryStructureService {

    private static final Logger log = LoggerFactory.getLogger(SalaryStructureService.class);

    private final SalaryStructureRepository salaryStructureRepository;
    private final SalaryStructureComponentRepository structureComponentRepository;
    private final SalaryComponentRepository salaryComponentRepository;

    public SalaryStructureService(SalaryStructureRepository salaryStructureRepository,
                                  SalaryStructureComponentRepository structureComponentRepository,
                                  SalaryComponentRepository salaryComponentRepository) {
        this.salaryStructureRepository = salaryStructureRepository;
        this.structureComponentRepository = structureComponentRepository;
        this.salaryComponentRepository = salaryComponentRepository;
    }

    @Transactional(readOnly = true)
    public Page<SalaryStructureResponse> getAll(Pageable pageable) {
        return salaryStructureRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public SalaryStructureResponse getById(Long id) {
        var structure = salaryStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "id", id));
        return mapToResponse(structure);
    }

    @Transactional(readOnly = true)
    public List<SalaryStructureResponse> getActive() {
        return salaryStructureRepository.findByActive(true).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SalaryStructureResponse create(SalaryStructureRequest request) {
        if (salaryStructureRepository.existsByCode(request.code())) {
            throw new BadRequestException("Salary structure with code '" + request.code() + "' already exists");
        }

        var structure = new SalaryStructure();
        structure.setCode(request.code());
        structure.setName(request.name());
        structure.setDescription(request.description());

        if (request.components() != null) {
            for (var compReq : request.components()) {
                var structureComponent = createStructureComponent(structure, compReq);
                structure.getComponents().add(structureComponent);
            }
        }

        var saved = salaryStructureRepository.save(structure);
        log.info("Created salary structure with id: {} code: {}", saved.getId(), saved.getCode());
        return mapToResponse(saved);
    }

    public SalaryStructureResponse update(Long id, SalaryStructureRequest request) {
        var structure = salaryStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "id", id));

        if (!structure.getCode().equals(request.code()) && salaryStructureRepository.existsByCode(request.code())) {
            throw new BadRequestException("Salary structure with code '" + request.code() + "' already exists");
        }

        structure.setCode(request.code());
        structure.setName(request.name());
        structure.setDescription(request.description());

        structure.getComponents().clear();
        if (request.components() != null) {
            for (var compReq : request.components()) {
                var structureComponent = createStructureComponent(structure, compReq);
                structure.getComponents().add(structureComponent);
            }
        }

        var saved = salaryStructureRepository.save(structure);
        log.info("Updated salary structure with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public SalaryStructureResponse clone(Long id, String newCode, String newName) {
        var source = salaryStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "id", id));

        if (salaryStructureRepository.existsByCode(newCode)) {
            throw new BadRequestException("Salary structure with code '" + newCode + "' already exists");
        }

        var cloned = new SalaryStructure();
        cloned.setCode(newCode);
        cloned.setName(newName);
        cloned.setDescription(source.getDescription());

        for (var sourceComp : source.getComponents()) {
            var clonedComp = new SalaryStructureComponent();
            clonedComp.setSalaryStructure(cloned);
            clonedComp.setSalaryComponent(sourceComp.getSalaryComponent());
            clonedComp.setComputationType(sourceComp.getComputationType());
            clonedComp.setPercentageValue(sourceComp.getPercentageValue());
            clonedComp.setFixedAmount(sourceComp.getFixedAmount());
            clonedComp.setSortOrder(sourceComp.getSortOrder());
            cloned.getComponents().add(clonedComp);
        }

        var saved = salaryStructureRepository.save(cloned);
        log.info("Cloned salary structure id: {} to new id: {} code: {}", id, saved.getId(), saved.getCode());
        return mapToResponse(saved);
    }

    public SalaryStructureResponse updateActive(Long id, boolean active) {
        var structure = salaryStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "id", id));

        structure.setActive(active);
        var saved = salaryStructureRepository.save(structure);
        log.info("Updated active status of salary structure id: {} to {}", id, active);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var structure = salaryStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "id", id));
        salaryStructureRepository.delete(structure);
        log.info("Deleted salary structure with id: {}", id);
    }

    private SalaryStructureComponent createStructureComponent(SalaryStructure structure,
                                                               SalaryStructureComponentRequest request) {
        var salaryComponent = salaryComponentRepository.findById(request.salaryComponentId())
                .orElseThrow(() -> new ResourceNotFoundException("SalaryComponent", "id", request.salaryComponentId()));

        var structureComponent = new SalaryStructureComponent();
        structureComponent.setSalaryStructure(structure);
        structureComponent.setSalaryComponent(salaryComponent);
        structureComponent.setComputationType(SalaryComputationType.valueOf(request.computationType()));
        structureComponent.setPercentageValue(request.percentageValue());
        structureComponent.setFixedAmount(request.fixedAmount());
        structureComponent.setSortOrder(request.sortOrder());
        return structureComponent;
    }

    private SalaryStructureResponse mapToResponse(SalaryStructure structure) {
        var componentResponses = structure.getComponents().stream()
                .map(this::mapComponentToResponse)
                .toList();

        return new SalaryStructureResponse(
                structure.getId(),
                structure.getCode(),
                structure.getName(),
                structure.getDescription(),
                structure.isActive(),
                componentResponses,
                structure.getCreatedAt(),
                structure.getUpdatedAt()
        );
    }

    private SalaryStructureComponentResponse mapComponentToResponse(SalaryStructureComponent comp) {
        return new SalaryStructureComponentResponse(
                comp.getId(),
                comp.getSalaryComponent().getId(),
                comp.getSalaryComponent().getCode(),
                comp.getSalaryComponent().getName(),
                comp.getSalaryComponent().getType().name(),
                comp.getComputationType().name(),
                comp.getPercentageValue(),
                comp.getFixedAmount(),
                comp.getSortOrder(),
                comp.getCreatedAt()
        );
    }
}
