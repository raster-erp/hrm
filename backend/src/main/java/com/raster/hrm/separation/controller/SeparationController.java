package com.raster.hrm.separation.controller;

import com.raster.hrm.separation.dto.SeparationRequest;
import com.raster.hrm.separation.dto.SeparationResponse;
import com.raster.hrm.separation.service.SeparationService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/separations")
public class SeparationController {

    private final SeparationService separationService;

    public SeparationController(SeparationService separationService) {
        this.separationService = separationService;
    }

    @GetMapping
    public ResponseEntity<Page<SeparationResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(separationService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeparationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(separationService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<SeparationResponse>> getByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(separationService.getByEmployeeId(employeeId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<SeparationResponse>> getPendingSeparations() {
        return ResponseEntity.ok(separationService.getPendingSeparations());
    }

    @PostMapping
    public ResponseEntity<SeparationResponse> create(@Valid @RequestBody SeparationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(separationService.create(request));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<SeparationResponse> approve(@PathVariable Long id,
                                                      @RequestParam Long approvedById) {
        return ResponseEntity.ok(separationService.approve(id, approvedById));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<SeparationResponse> reject(@PathVariable Long id,
                                                     @RequestParam Long approvedById) {
        return ResponseEntity.ok(separationService.reject(id, approvedById));
    }

    @PutMapping("/{id}/finalize")
    public ResponseEntity<SeparationResponse> finalizeSeparation(@PathVariable Long id) {
        return ResponseEntity.ok(separationService.finalizeSeparation(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        separationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
