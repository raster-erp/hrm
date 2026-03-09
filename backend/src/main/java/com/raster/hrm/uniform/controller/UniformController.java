package com.raster.hrm.uniform.controller;

import com.raster.hrm.uniform.dto.UniformRequest;
import com.raster.hrm.uniform.dto.UniformResponse;
import com.raster.hrm.uniform.service.UniformService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/uniforms")
public class UniformController {

    private final UniformService uniformService;

    public UniformController(UniformService uniformService) {
        this.uniformService = uniformService;
    }

    @GetMapping
    public ResponseEntity<Page<UniformResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(uniformService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniformResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(uniformService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UniformResponse> create(@Valid @RequestBody UniformRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(uniformService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UniformResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody UniformRequest request) {
        return ResponseEntity.ok(uniformService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        uniformService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
