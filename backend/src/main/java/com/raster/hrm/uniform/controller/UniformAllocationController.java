package com.raster.hrm.uniform.controller;

import com.raster.hrm.uniform.dto.UniformAllocationRequest;
import com.raster.hrm.uniform.dto.UniformAllocationResponse;
import com.raster.hrm.uniform.service.UniformAllocationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/uniform-allocations")
public class UniformAllocationController {

    private final UniformAllocationService allocationService;

    public UniformAllocationController(UniformAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @GetMapping
    public ResponseEntity<Page<UniformAllocationResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(allocationService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniformAllocationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(allocationService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<UniformAllocationResponse>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(allocationService.getByEmployeeId(employeeId));
    }

    @GetMapping("/pending-returns")
    public ResponseEntity<List<UniformAllocationResponse>> getPendingReturns() {
        return ResponseEntity.ok(allocationService.getPendingReturns());
    }

    @PostMapping
    public ResponseEntity<UniformAllocationResponse> allocate(@Valid @RequestBody UniformAllocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(allocationService.allocate(request));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<UniformAllocationResponse> markReturned(@PathVariable Long id) {
        return ResponseEntity.ok(allocationService.markReturned(id));
    }
}
