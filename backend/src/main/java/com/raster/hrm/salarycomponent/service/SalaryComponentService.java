package com.raster.hrm.salarycomponent.service;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.salarycomponent.dto.SalaryComponentRequest;
import com.raster.hrm.salarycomponent.dto.SalaryComponentResponse;
import com.raster.hrm.salarycomponent.entity.SalaryComponent;
import com.raster.hrm.salarycomponent.entity.SalaryComponentType;
import com.raster.hrm.salarycomponent.entity.SalaryComputationType;
import com.raster.hrm.salarycomponent.repository.SalaryComponentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SalaryComponentService {

    private static final Logger log = LoggerFactory.getLogger(SalaryComponentService.class);

    private final SalaryComponentRepository salaryComponentRepository;

    public SalaryComponentService(SalaryComponentRepository salaryComponentRepository) {
        this.salaryComponentRepository = salaryComponentRepository;
    }

    @Transactional(readOnly = true)
    public Page<SalaryComponentResponse> getAll(Pageable pageable) {
        return salaryComponentRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public SalaryComponentResponse getById(Long id) {
        var component = salaryComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryComponent", "id", id));
        return mapToResponse(component);
    }

    @Transactional(readOnly = true)
    public List<SalaryComponentResponse> getByType(SalaryComponentType type) {
        return salaryComponentRepository.findByType(type).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SalaryComponentResponse> getActive() {
        return salaryComponentRepository.findByActive(true).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SalaryComponentResponse create(SalaryComponentRequest request) {
        if (salaryComponentRepository.existsByCode(request.code())) {
            throw new BadRequestException("Salary component with code '" + request.code() + "' already exists");
        }

        var component = new SalaryComponent();
        component.setCode(request.code());
        component.setName(request.name());
        component.setType(SalaryComponentType.valueOf(request.type()));
        component.setComputationType(SalaryComputationType.valueOf(request.computationType()));
        component.setPercentageValue(request.percentageValue());
        component.setTaxable(request.taxable());
        component.setMandatory(request.mandatory());
        component.setDescription(request.description());

        var saved = salaryComponentRepository.save(component);
        log.info("Created salary component with id: {} code: {}", saved.getId(), saved.getCode());
        return mapToResponse(saved);
    }

    public SalaryComponentResponse update(Long id, SalaryComponentRequest request) {
        var component = salaryComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryComponent", "id", id));

        if (!component.getCode().equals(request.code()) && salaryComponentRepository.existsByCode(request.code())) {
            throw new BadRequestException("Salary component with code '" + request.code() + "' already exists");
        }

        component.setCode(request.code());
        component.setName(request.name());
        component.setType(SalaryComponentType.valueOf(request.type()));
        component.setComputationType(SalaryComputationType.valueOf(request.computationType()));
        component.setPercentageValue(request.percentageValue());
        component.setTaxable(request.taxable());
        component.setMandatory(request.mandatory());
        component.setDescription(request.description());

        var saved = salaryComponentRepository.save(component);
        log.info("Updated salary component with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public SalaryComponentResponse updateActive(Long id, boolean active) {
        var component = salaryComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryComponent", "id", id));

        component.setActive(active);
        var saved = salaryComponentRepository.save(component);
        log.info("Updated active status of salary component id: {} to {}", id, active);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var component = salaryComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryComponent", "id", id));
        salaryComponentRepository.delete(component);
        log.info("Deleted salary component with id: {}", id);
    }

    private SalaryComponentResponse mapToResponse(SalaryComponent component) {
        return new SalaryComponentResponse(
                component.getId(),
                component.getCode(),
                component.getName(),
                component.getType().name(),
                component.getComputationType().name(),
                component.getPercentageValue(),
                component.isTaxable(),
                component.isMandatory(),
                component.getDescription(),
                component.isActive(),
                component.getCreatedAt(),
                component.getUpdatedAt()
        );
    }
}
