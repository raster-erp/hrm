package com.raster.hrm.designation.controller;

import com.raster.hrm.designation.dto.DesignationRequest;
import com.raster.hrm.designation.dto.DesignationResponse;
import com.raster.hrm.designation.service.DesignationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/designations")
public class DesignationController {

    private final DesignationService designationService;

    public DesignationController(DesignationService designationService) {
        this.designationService = designationService;
    }

    @GetMapping
    public ResponseEntity<List<DesignationResponse>> getAll() {
        return ResponseEntity.ok(designationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(designationService.getById(id));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DesignationResponse>> getByDepartmentId(@PathVariable Long departmentId) {
        return ResponseEntity.ok(designationService.getByDepartmentId(departmentId));
    }

    @PostMapping
    public ResponseEntity<DesignationResponse> create(@Valid @RequestBody DesignationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(designationService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DesignationResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody DesignationRequest request) {
        return ResponseEntity.ok(designationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        designationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
