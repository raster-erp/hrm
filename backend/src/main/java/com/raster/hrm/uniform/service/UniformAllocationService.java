package com.raster.hrm.uniform.service;

import com.raster.hrm.uniform.dto.UniformAllocationRequest;
import com.raster.hrm.uniform.dto.UniformAllocationResponse;
import com.raster.hrm.uniform.entity.AllocationStatus;
import com.raster.hrm.uniform.entity.UniformAllocation;
import com.raster.hrm.uniform.repository.UniformAllocationRepository;
import com.raster.hrm.uniform.repository.UniformRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class UniformAllocationService {

    private static final Logger log = LoggerFactory.getLogger(UniformAllocationService.class);

    private final UniformAllocationRepository allocationRepository;
    private final UniformRepository uniformRepository;
    private final EmployeeRepository employeeRepository;

    public UniformAllocationService(UniformAllocationRepository allocationRepository,
                                     UniformRepository uniformRepository,
                                     EmployeeRepository employeeRepository) {
        this.allocationRepository = allocationRepository;
        this.uniformRepository = uniformRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<UniformAllocationResponse> getAll(Pageable pageable) {
        return allocationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public UniformAllocationResponse getById(Long id) {
        var allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UniformAllocation", "id", id));
        return mapToResponse(allocation);
    }

    @Transactional(readOnly = true)
    public List<UniformAllocationResponse> getByEmployeeId(Long employeeId) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        return allocationRepository.findByEmployeeId(employee.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UniformAllocationResponse allocate(UniformAllocationRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var uniform = uniformRepository.findById(request.uniformId())
                .orElseThrow(() -> new ResourceNotFoundException("Uniform", "id", request.uniformId()));

        var allocation = new UniformAllocation();
        allocation.setEmployee(employee);
        allocation.setUniform(uniform);
        allocation.setAllocatedDate(request.allocatedDate());

        var saved = allocationRepository.save(allocation);
        log.info("Allocated uniform id: {} to employee: {}", uniform.getId(), employee.getEmployeeCode());
        return mapToResponse(saved);
    }

    public UniformAllocationResponse markReturned(Long id) {
        var allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UniformAllocation", "id", id));

        allocation.setReturnedDate(LocalDate.now());
        allocation.setStatus(AllocationStatus.RETURNED);

        var saved = allocationRepository.save(allocation);
        log.info("Marked uniform allocation id: {} as returned", id);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UniformAllocationResponse> getPendingReturns() {
        return allocationRepository.findByStatus(AllocationStatus.ALLOCATED).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private UniformAllocationResponse mapToResponse(UniformAllocation allocation) {
        var employee = allocation.getEmployee();
        var uniform = allocation.getUniform();
        return new UniformAllocationResponse(
                allocation.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                uniform.getId(),
                uniform.getName(),
                uniform.getType(),
                uniform.getSize(),
                allocation.getAllocatedDate(),
                allocation.getReturnedDate(),
                allocation.getStatus().name(),
                allocation.getCreatedAt(),
                allocation.getUpdatedAt()
        );
    }
}
