package com.raster.hrm.overtime.controller;

import com.raster.hrm.overtime.dto.OvertimePolicyRequest;
import com.raster.hrm.overtime.dto.OvertimePolicyResponse;
import com.raster.hrm.overtime.entity.OvertimePolicyType;
import com.raster.hrm.overtime.service.OvertimePolicyService;
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
@RequestMapping("/api/v1/overtime-policies")
public class OvertimePolicyController {

    private final OvertimePolicyService overtimePolicyService;

    public OvertimePolicyController(OvertimePolicyService overtimePolicyService) {
        this.overtimePolicyService = overtimePolicyService;
    }

    @GetMapping
    public ResponseEntity<Page<OvertimePolicyResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(overtimePolicyService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OvertimePolicyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(overtimePolicyService.getById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<OvertimePolicyResponse>> getByType(@PathVariable OvertimePolicyType type) {
        return ResponseEntity.ok(overtimePolicyService.getByType(type));
    }

    @GetMapping("/active")
    public ResponseEntity<List<OvertimePolicyResponse>> getActive() {
        return ResponseEntity.ok(overtimePolicyService.getActive());
    }

    @PostMapping
    public ResponseEntity<OvertimePolicyResponse> create(@Valid @RequestBody OvertimePolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(overtimePolicyService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OvertimePolicyResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody OvertimePolicyRequest request) {
        return ResponseEntity.ok(overtimePolicyService.update(id, request));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<OvertimePolicyResponse> updateActive(@PathVariable Long id,
                                                                @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(overtimePolicyService.updateActive(id, body.get("active")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        overtimePolicyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
