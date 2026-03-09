package com.raster.hrm.separation.controller;

import com.raster.hrm.separation.dto.NoDuesRequest;
import com.raster.hrm.separation.dto.NoDuesResponse;
import com.raster.hrm.separation.service.NoDuesService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/no-dues")
public class NoDuesController {

    private final NoDuesService noDuesService;

    public NoDuesController(NoDuesService noDuesService) {
        this.noDuesService = noDuesService;
    }

    @GetMapping("/separation/{separationId}")
    public ResponseEntity<List<NoDuesResponse>> getBySeparationId(@PathVariable Long separationId) {
        return ResponseEntity.ok(noDuesService.getBySeparationId(separationId));
    }

    @PostMapping
    public ResponseEntity<NoDuesResponse> create(@Valid @RequestBody NoDuesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noDuesService.create(request));
    }

    @PutMapping("/{id}/clear")
    public ResponseEntity<NoDuesResponse> clearDepartment(@PathVariable Long id,
                                                          @RequestParam String clearedBy) {
        return ResponseEntity.ok(noDuesService.clearDepartment(id, clearedBy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noDuesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
