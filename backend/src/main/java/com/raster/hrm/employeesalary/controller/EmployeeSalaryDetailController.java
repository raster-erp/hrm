package com.raster.hrm.employeesalary.controller;

import com.raster.hrm.employeesalary.dto.EmployeeSalaryDetailRequest;
import com.raster.hrm.employeesalary.dto.EmployeeSalaryDetailResponse;
import com.raster.hrm.employeesalary.dto.SalaryRevisionRequest;
import com.raster.hrm.employeesalary.service.EmployeeSalaryDetailService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employee-salary-details")
public class EmployeeSalaryDetailController {

    private final EmployeeSalaryDetailService employeeSalaryDetailService;

    public EmployeeSalaryDetailController(EmployeeSalaryDetailService employeeSalaryDetailService) {
        this.employeeSalaryDetailService = employeeSalaryDetailService;
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeSalaryDetailResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(employeeSalaryDetailService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeSalaryDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeSalaryDetailService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeSalaryDetailResponse>> getByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeSalaryDetailService.getByEmployeeId(employeeId));
    }

    @PostMapping
    public ResponseEntity<EmployeeSalaryDetailResponse> create(@Valid @RequestBody EmployeeSalaryDetailRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeSalaryDetailService.create(request));
    }

    @PostMapping("/employee/{employeeId}/revise")
    public ResponseEntity<EmployeeSalaryDetailResponse> revise(@PathVariable Long employeeId,
                                                                @Valid @RequestBody SalaryRevisionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeSalaryDetailService.revise(employeeId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeSalaryDetailService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
