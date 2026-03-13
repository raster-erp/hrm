package com.raster.hrm.salarystructure.controller;

import com.raster.hrm.salarystructure.dto.SalaryStructureCloneRequest;
import com.raster.hrm.salarystructure.dto.SalaryStructureRequest;
import com.raster.hrm.salarystructure.dto.SalaryStructureResponse;
import com.raster.hrm.salarystructure.service.SalaryStructureService;
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
@RequestMapping("/api/v1/salary-structures")
public class SalaryStructureController {

    private final SalaryStructureService salaryStructureService;

    public SalaryStructureController(SalaryStructureService salaryStructureService) {
        this.salaryStructureService = salaryStructureService;
    }

    @GetMapping
    public ResponseEntity<Page<SalaryStructureResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(salaryStructureService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaryStructureResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(salaryStructureService.getById(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<SalaryStructureResponse>> getActive() {
        return ResponseEntity.ok(salaryStructureService.getActive());
    }

    @PostMapping
    public ResponseEntity<SalaryStructureResponse> create(@Valid @RequestBody SalaryStructureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salaryStructureService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaryStructureResponse> update(@PathVariable Long id,
                                                           @Valid @RequestBody SalaryStructureRequest request) {
        return ResponseEntity.ok(salaryStructureService.update(id, request));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<SalaryStructureResponse> clone(@PathVariable Long id,
                                                          @Valid @RequestBody SalaryStructureCloneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(salaryStructureService.clone(id, request.newCode(), request.newName()));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<SalaryStructureResponse> updateActive(@PathVariable Long id,
                                                                 @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(salaryStructureService.updateActive(id, body.get("active")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        salaryStructureService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
