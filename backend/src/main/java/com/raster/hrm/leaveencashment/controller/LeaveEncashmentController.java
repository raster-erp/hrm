package com.raster.hrm.leaveencashment.controller;

import com.raster.hrm.leaveencashment.dto.EncashmentEligibilityResponse;
import com.raster.hrm.leaveencashment.dto.LeaveEncashmentApprovalRequest;
import com.raster.hrm.leaveencashment.dto.LeaveEncashmentRequest;
import com.raster.hrm.leaveencashment.dto.LeaveEncashmentResponse;
import com.raster.hrm.leaveencashment.entity.EncashmentStatus;
import com.raster.hrm.leaveencashment.service.LeaveEncashmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leave-encashments")
public class LeaveEncashmentController {

    private final LeaveEncashmentService leaveEncashmentService;

    public LeaveEncashmentController(LeaveEncashmentService leaveEncashmentService) {
        this.leaveEncashmentService = leaveEncashmentService;
    }

    @GetMapping("/eligibility")
    public ResponseEntity<EncashmentEligibilityResponse> checkEligibility(
            @RequestParam Long employeeId,
            @RequestParam Long leaveTypeId,
            @RequestParam int year) {
        return ResponseEntity.ok(leaveEncashmentService.checkEligibility(employeeId, leaveTypeId, year));
    }

    @PostMapping
    public ResponseEntity<LeaveEncashmentResponse> createRequest(
            @Valid @RequestBody LeaveEncashmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveEncashmentService.createRequest(request));
    }

    @GetMapping
    public ResponseEntity<Page<LeaveEncashmentResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(leaveEncashmentService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveEncashmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveEncashmentService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<LeaveEncashmentResponse>> getByEmployee(
            @PathVariable Long employeeId, Pageable pageable) {
        return ResponseEntity.ok(leaveEncashmentService.getByEmployee(employeeId, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<LeaveEncashmentResponse>> getByStatus(
            @PathVariable EncashmentStatus status, Pageable pageable) {
        return ResponseEntity.ok(leaveEncashmentService.getByStatus(status, pageable));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<LeaveEncashmentResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody LeaveEncashmentApprovalRequest request) {
        return ResponseEntity.ok(leaveEncashmentService.approve(id, request));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<LeaveEncashmentResponse> markAsPaid(
            @PathVariable Long id,
            @RequestParam(required = false) String approvedBy) {
        return ResponseEntity.ok(leaveEncashmentService.markAsPaid(id, approvedBy));
    }
}
