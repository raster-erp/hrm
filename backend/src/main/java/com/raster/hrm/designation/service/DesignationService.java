package com.raster.hrm.designation.service;

import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.designation.dto.DesignationRequest;
import com.raster.hrm.designation.dto.DesignationResponse;
import com.raster.hrm.designation.entity.Designation;
import com.raster.hrm.designation.repository.DesignationRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final DepartmentRepository departmentRepository;

    public DesignationService(DesignationRepository designationRepository,
                              DepartmentRepository departmentRepository) {
        this.designationRepository = designationRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DesignationResponse> getAll() {
        return designationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DesignationResponse getById(Long id) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));
        return mapToResponse(designation);
    }

    @Transactional(readOnly = true)
    public List<DesignationResponse> getByDepartmentId(Long departmentId) {
        return designationRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DesignationResponse create(DesignationRequest request) {
        if (designationRepository.existsByCode(request.code())) {
            throw new BadRequestException("Designation with code '" + request.code() + "' already exists");
        }

        Designation designation = new Designation();
        designation.setTitle(request.title());
        designation.setCode(request.code());
        designation.setLevel(request.level());
        designation.setGrade(request.grade());
        designation.setDescription(request.description());
        designation.setActive(request.active() != null ? request.active() : true);

        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.departmentId()));
            designation.setDepartment(department);
        }

        Designation saved = designationRepository.save(designation);
        return mapToResponse(saved);
    }

    public DesignationResponse update(Long id, DesignationRequest request) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));

        Optional<Designation> existingWithCode = designationRepository.findByCode(request.code());
        if (existingWithCode.isPresent() && !existingWithCode.get().getId().equals(id)) {
            throw new BadRequestException("Designation with code '" + request.code() + "' already exists");
        }

        designation.setTitle(request.title());
        designation.setCode(request.code());
        designation.setLevel(request.level());
        designation.setGrade(request.grade());
        designation.setDescription(request.description());
        designation.setActive(request.active() != null ? request.active() : designation.getActive());

        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.departmentId()));
            designation.setDepartment(department);
        } else {
            designation.setDepartment(null);
        }

        Designation saved = designationRepository.save(designation);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));

        if (designationRepository.countEmployeesByDesignationId(id) > 0) {
            throw new BadRequestException("Cannot delete designation with existing employees");
        }

        designationRepository.delete(designation);
    }

    private DesignationResponse mapToResponse(Designation designation) {
        return new DesignationResponse(
                designation.getId(),
                designation.getTitle(),
                designation.getCode(),
                designation.getLevel(),
                designation.getGrade(),
                designation.getDepartment() != null ? designation.getDepartment().getId() : null,
                designation.getDepartment() != null ? designation.getDepartment().getName() : null,
                designation.getDescription(),
                designation.getActive()
        );
    }
}
