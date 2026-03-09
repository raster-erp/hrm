package com.raster.hrm.separation.controller;

import com.raster.hrm.separation.dto.ExitChecklistRequest;
import com.raster.hrm.separation.dto.ExitChecklistResponse;
import com.raster.hrm.separation.service.ExitChecklistService;
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
@RequestMapping("/api/v1/exit-checklists")
public class ExitChecklistController {

    private final ExitChecklistService exitChecklistService;

    public ExitChecklistController(ExitChecklistService exitChecklistService) {
        this.exitChecklistService = exitChecklistService;
    }

    @GetMapping("/separation/{separationId}")
    public ResponseEntity<List<ExitChecklistResponse>> getBySeparationId(@PathVariable Long separationId) {
        return ResponseEntity.ok(exitChecklistService.getBySeparationId(separationId));
    }

    @PostMapping
    public ResponseEntity<ExitChecklistResponse> create(@Valid @RequestBody ExitChecklistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exitChecklistService.create(request));
    }

    @PutMapping("/{id}/clear")
    public ResponseEntity<ExitChecklistResponse> clearItem(@PathVariable Long id,
                                                           @RequestParam String clearedBy) {
        return ResponseEntity.ok(exitChecklistService.clearItem(id, clearedBy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exitChecklistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
