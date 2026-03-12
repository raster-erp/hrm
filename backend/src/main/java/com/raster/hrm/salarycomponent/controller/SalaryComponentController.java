package com.raster.hrm.salarycomponent.controller;

import com.raster.hrm.salarycomponent.dto.SalaryComponentRequest;
import com.raster.hrm.salarycomponent.dto.SalaryComponentResponse;
import com.raster.hrm.salarycomponent.entity.SalaryComponentType;
import com.raster.hrm.salarycomponent.service.SalaryComponentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/salary-components")
public class SalaryComponentController {

    private final SalaryComponentService salaryComponentService;

    public SalaryComponentController(SalaryComponentService salaryComponentService) {
        this.salaryComponentService = salaryComponentService;
    }

    @GetMapping
    public ResponseEntity<Page<SalaryComponentResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(salaryComponentService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaryComponentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(salaryComponentService.getById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<SalaryComponentResponse>> getByType(@PathVariable SalaryComponentType type) {
        return ResponseEntity.ok(salaryComponentService.getByType(type));
    }

    @GetMapping("/active")
    public ResponseEntity<List<SalaryComponentResponse>> getActive() {
        return ResponseEntity.ok(salaryComponentService.getActive());
    }

    @PostMapping
    public ResponseEntity<SalaryComponentResponse> create(@Valid @RequestBody SalaryComponentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salaryComponentService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaryComponentResponse> update(@PathVariable Long id,
                                                           @Valid @RequestBody SalaryComponentRequest request) {
        return ResponseEntity.ok(salaryComponentService.update(id, request));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<SalaryComponentResponse> updateActive(@PathVariable Long id,
                                                                 @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(salaryComponentService.updateActive(id, body.get("active")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        salaryComponentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
