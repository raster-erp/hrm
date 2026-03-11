package com.raster.hrm.leavepolicyassignment.controller;

import com.raster.hrm.leavepolicyassignment.dto.LeavePolicyAssignmentRequest;
import com.raster.hrm.leavepolicyassignment.dto.LeavePolicyAssignmentResponse;
import com.raster.hrm.leavepolicyassignment.entity.AssignmentType;
import com.raster.hrm.leavepolicyassignment.service.LeavePolicyAssignmentService;
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
@RequestMapping("/api/v1/leave-policy-assignments")
public class LeavePolicyAssignmentController {

    private final LeavePolicyAssignmentService leavePolicyAssignmentService;

    public LeavePolicyAssignmentController(LeavePolicyAssignmentService leavePolicyAssignmentService) {
        this.leavePolicyAssignmentService = leavePolicyAssignmentService;
    }

    @GetMapping
    public ResponseEntity<Page<LeavePolicyAssignmentResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(leavePolicyAssignmentService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeavePolicyAssignmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leavePolicyAssignmentService.getById(id));
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<LeavePolicyAssignmentResponse>> getByPolicyId(@PathVariable Long policyId) {
        return ResponseEntity.ok(leavePolicyAssignmentService.getByPolicyId(policyId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<LeavePolicyAssignmentResponse>> getByAssignmentType(@PathVariable AssignmentType type) {
        return ResponseEntity.ok(leavePolicyAssignmentService.getByAssignmentType(type));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<LeavePolicyAssignmentResponse>> getByDepartmentId(@PathVariable Long departmentId) {
        return ResponseEntity.ok(leavePolicyAssignmentService.getByDepartmentId(departmentId));
    }

    @GetMapping("/designation/{designationId}")
    public ResponseEntity<List<LeavePolicyAssignmentResponse>> getByDesignationId(@PathVariable Long designationId) {
        return ResponseEntity.ok(leavePolicyAssignmentService.getByDesignationId(designationId));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeavePolicyAssignmentResponse>> getByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leavePolicyAssignmentService.getByEmployeeId(employeeId));
    }

    @PostMapping
    public ResponseEntity<LeavePolicyAssignmentResponse> create(@Valid @RequestBody LeavePolicyAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leavePolicyAssignmentService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeavePolicyAssignmentResponse> update(@PathVariable Long id,
                                                                 @Valid @RequestBody LeavePolicyAssignmentRequest request) {
        return ResponseEntity.ok(leavePolicyAssignmentService.update(id, request));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<LeavePolicyAssignmentResponse> updateActive(@PathVariable Long id,
                                                                       @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(leavePolicyAssignmentService.updateActive(id, body.get("active")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leavePolicyAssignmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
