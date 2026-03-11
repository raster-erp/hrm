package com.raster.hrm.leavepolicy.controller;

import com.raster.hrm.leavepolicy.dto.LeavePolicyRequest;
import com.raster.hrm.leavepolicy.dto.LeavePolicyResponse;
import com.raster.hrm.leavepolicy.service.LeavePolicyService;
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
@RequestMapping("/api/v1/leave-policies")
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    public LeavePolicyController(LeavePolicyService leavePolicyService) {
        this.leavePolicyService = leavePolicyService;
    }

    @GetMapping
    public ResponseEntity<Page<LeavePolicyResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(leavePolicyService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeavePolicyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leavePolicyService.getById(id));
    }

    @GetMapping("/leave-type/{leaveTypeId}")
    public ResponseEntity<List<LeavePolicyResponse>> getByLeaveTypeId(@PathVariable Long leaveTypeId) {
        return ResponseEntity.ok(leavePolicyService.getByLeaveTypeId(leaveTypeId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<LeavePolicyResponse>> getActive() {
        return ResponseEntity.ok(leavePolicyService.getActive());
    }

    @PostMapping
    public ResponseEntity<LeavePolicyResponse> create(@Valid @RequestBody LeavePolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leavePolicyService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeavePolicyResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody LeavePolicyRequest request) {
        return ResponseEntity.ok(leavePolicyService.update(id, request));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<LeavePolicyResponse> updateActive(@PathVariable Long id,
                                                             @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(leavePolicyService.updateActive(id, body.get("active")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leavePolicyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
