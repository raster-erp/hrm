package com.raster.hrm.employee.controller;

import com.raster.hrm.employee.dto.EmployeeDocumentResponse;
import com.raster.hrm.employee.dto.EmployeeRequest;
import com.raster.hrm.employee.dto.EmployeeResponse;
import com.raster.hrm.employee.dto.EmployeeSearchCriteria;
import com.raster.hrm.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate joiningDateFrom,
            @RequestParam(required = false) LocalDate joiningDateTo,
            Pageable pageable) {
        var criteria = new EmployeeSearchCriteria(name, departmentId, status, joiningDateFrom, joiningDateTo);
        return ResponseEntity.ok(employeeService.search(criteria, pageable));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        employeeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<EmployeeDocumentResponse>> getDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getDocuments(id));
    }
}
