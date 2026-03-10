package com.raster.hrm.rotationpattern.controller;

import com.raster.hrm.rotationpattern.dto.RotationPatternRequest;
import com.raster.hrm.rotationpattern.dto.RotationPatternResponse;
import com.raster.hrm.rotationpattern.service.RotationPatternService;
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
@RequestMapping("/api/v1/rotation-patterns")
public class RotationPatternController {

    private final RotationPatternService rotationPatternService;

    public RotationPatternController(RotationPatternService rotationPatternService) {
        this.rotationPatternService = rotationPatternService;
    }

    @GetMapping
    public ResponseEntity<Page<RotationPatternResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(rotationPatternService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RotationPatternResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(rotationPatternService.getById(id));
    }

    @PostMapping
    public ResponseEntity<RotationPatternResponse> create(@Valid @RequestBody RotationPatternRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rotationPatternService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RotationPatternResponse> update(@PathVariable Long id,
                                                           @Valid @RequestBody RotationPatternRequest request) {
        return ResponseEntity.ok(rotationPatternService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rotationPatternService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
