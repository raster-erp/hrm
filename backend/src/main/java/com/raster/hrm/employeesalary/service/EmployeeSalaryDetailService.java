package com.raster.hrm.employeesalary.service;

import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.employeesalary.dto.EmployeeSalaryDetailRequest;
import com.raster.hrm.employeesalary.dto.EmployeeSalaryDetailResponse;
import com.raster.hrm.employeesalary.dto.SalaryRevisionRequest;
import com.raster.hrm.employeesalary.entity.EmployeeSalaryDetail;
import com.raster.hrm.employeesalary.repository.EmployeeSalaryDetailRepository;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.salarystructure.repository.SalaryStructureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeSalaryDetailService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeSalaryDetailService.class);

    private final EmployeeSalaryDetailRepository employeeSalaryDetailRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryStructureRepository salaryStructureRepository;

    public EmployeeSalaryDetailService(EmployeeSalaryDetailRepository employeeSalaryDetailRepository,
                                       EmployeeRepository employeeRepository,
                                       SalaryStructureRepository salaryStructureRepository) {
        this.employeeSalaryDetailRepository = employeeSalaryDetailRepository;
        this.employeeRepository = employeeRepository;
        this.salaryStructureRepository = salaryStructureRepository;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeSalaryDetailResponse> getAll(Pageable pageable) {
        return employeeSalaryDetailRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeSalaryDetailResponse getById(Long id) {
        var detail = employeeSalaryDetailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryDetail", "id", id));
        return mapToResponse(detail);
    }

    @Transactional(readOnly = true)
    public List<EmployeeSalaryDetailResponse> getByEmployeeId(Long employeeId) {
        return employeeSalaryDetailRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public EmployeeSalaryDetailResponse create(EmployeeSalaryDetailRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var structure = salaryStructureRepository.findById(request.salaryStructureId())
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "id", request.salaryStructureId()));

        var detail = new EmployeeSalaryDetail();
        detail.setEmployee(employee);
        detail.setSalaryStructure(structure);
        detail.setCtc(request.ctc());
        detail.setBasicSalary(request.basicSalary());
        detail.setEffectiveDate(request.effectiveDate());
        detail.setNotes(request.notes());

        var saved = employeeSalaryDetailRepository.save(detail);
        log.info("Created salary assignment for employee id: {} with structure id: {}", employee.getId(), structure.getId());
        return mapToResponse(saved);
    }

    public EmployeeSalaryDetailResponse revise(Long employeeId, SalaryRevisionRequest request) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        var structure = salaryStructureRepository.findById(request.salaryStructureId())
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "id", request.salaryStructureId()));

        var existingDetails = employeeSalaryDetailRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
        for (var existing : existingDetails) {
            if (existing.isActive()) {
                existing.setActive(false);
                employeeSalaryDetailRepository.save(existing);
            }
        }

        var newDetail = new EmployeeSalaryDetail();
        newDetail.setEmployee(employee);
        newDetail.setSalaryStructure(structure);
        newDetail.setCtc(request.ctc());
        newDetail.setBasicSalary(request.basicSalary());
        newDetail.setEffectiveDate(request.effectiveDate());
        newDetail.setNotes(request.notes());

        var saved = employeeSalaryDetailRepository.save(newDetail);
        log.info("Created salary revision for employee id: {} effective: {}", employeeId, request.effectiveDate());
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var detail = employeeSalaryDetailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryDetail", "id", id));
        employeeSalaryDetailRepository.delete(detail);
        log.info("Deleted employee salary detail with id: {}", id);
    }

    private EmployeeSalaryDetailResponse mapToResponse(EmployeeSalaryDetail detail) {
        return new EmployeeSalaryDetailResponse(
                detail.getId(),
                detail.getEmployee().getId(),
                detail.getEmployee().getFirstName() + " " + detail.getEmployee().getLastName(),
                detail.getEmployee().getEmployeeCode(),
                detail.getSalaryStructure().getId(),
                detail.getSalaryStructure().getName(),
                detail.getCtc(),
                detail.getBasicSalary(),
                detail.getEffectiveDate(),
                detail.getNotes(),
                detail.isActive(),
                detail.getCreatedAt(),
                detail.getUpdatedAt()
        );
    }
}
