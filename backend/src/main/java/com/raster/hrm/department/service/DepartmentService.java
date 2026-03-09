package com.raster.hrm.department.service;

import com.raster.hrm.department.dto.DepartmentRequest;
import com.raster.hrm.department.dto.DepartmentResponse;
import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return mapToResponse(department);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getByParentId(Long parentId) {
        return departmentRepository.findByParentId(parentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getRootDepartments() {
        return departmentRepository.findByParentIsNull().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByCode(request.code())) {
            throw new BadRequestException("Department with code '" + request.code() + "' already exists");
        }

        Department department = new Department();
        department.setName(request.name());
        department.setCode(request.code());
        department.setDescription(request.description());
        department.setActive(request.active() != null ? request.active() : true);

        if (request.parentId() != null) {
            Department parent = departmentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.parentId()));
            department.setParent(parent);
        }

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        Optional<Department> existingWithCode = departmentRepository.findByCode(request.code());
        if (existingWithCode.isPresent() && !existingWithCode.get().getId().equals(id)) {
            throw new BadRequestException("Department with code '" + request.code() + "' already exists");
        }

        department.setName(request.name());
        department.setCode(request.code());
        department.setDescription(request.description());
        department.setActive(request.active() != null ? request.active() : department.getActive());

        if (request.parentId() != null) {
            Department parent = departmentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.parentId()));
            department.setParent(parent);
        } else {
            department.setParent(null);
        }

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        if (departmentRepository.countEmployeesByDepartmentId(id) > 0) {
            throw new BadRequestException("Cannot delete department with existing employees");
        }

        departmentRepository.delete(department);
    }

    private DepartmentResponse mapToResponse(Department department) {
        List<DepartmentResponse> children = List.of();
        if (department.getChildren() != null && !department.getChildren().isEmpty()) {
            children = department.getChildren().stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getParent() != null ? department.getParent().getId() : null,
                department.getParent() != null ? department.getParent().getName() : null,
                department.getDescription(),
                department.getActive(),
                children
        );
    }
}
