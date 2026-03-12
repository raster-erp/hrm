package com.raster.hrm.leaveapplication.controller;

import com.raster.hrm.leaveapplication.dto.LeaveApplicationRequest;
import com.raster.hrm.leaveapplication.dto.LeaveApplicationResponse;
import com.raster.hrm.leaveapplication.dto.LeaveApprovalRequest;
import com.raster.hrm.leaveapplication.entity.LeaveApplicationStatus;
import com.raster.hrm.leaveapplication.service.LeaveApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/leave-applications")
public class LeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;

    public LeaveApplicationController(LeaveApplicationService leaveApplicationService) {
        this.leaveApplicationService = leaveApplicationService;
    }

    @GetMapping
    public ResponseEntity<Page<LeaveApplicationResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(leaveApplicationService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveApplicationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveApplicationService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<LeaveApplicationResponse>> getByEmployee(
            @PathVariable Long employeeId, Pageable pageable) {
        return ResponseEntity.ok(leaveApplicationService.getByEmployee(employeeId, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<LeaveApplicationResponse>> getByStatus(
            @PathVariable LeaveApplicationStatus status, Pageable pageable) {
        return ResponseEntity.ok(leaveApplicationService.getByStatus(status, pageable));
    }

    @GetMapping("/leave-type/{leaveTypeId}")
    public ResponseEntity<Page<LeaveApplicationResponse>> getByLeaveType(
            @PathVariable Long leaveTypeId, Pageable pageable) {
        return ResponseEntity.ok(leaveApplicationService.getByLeaveType(leaveTypeId, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<LeaveApplicationResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable) {
        return ResponseEntity.ok(leaveApplicationService.getByDateRange(fromDate, toDate, pageable));
    }

    @GetMapping("/employee/{employeeId}/date-range")
    public ResponseEntity<Page<LeaveApplicationResponse>> getByEmployeeAndDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable) {
        return ResponseEntity.ok(leaveApplicationService.getByEmployeeAndDateRange(employeeId, fromDate, toDate, pageable));
    }

    @PostMapping
    public ResponseEntity<LeaveApplicationResponse> create(@Valid @RequestBody LeaveApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveApplicationService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveApplicationResponse> update(
            @PathVariable Long id, @Valid @RequestBody LeaveApplicationRequest request) {
        return ResponseEntity.ok(leaveApplicationService.update(id, request));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<LeaveApplicationResponse> approve(
            @PathVariable Long id, @Valid @RequestBody LeaveApprovalRequest request) {
        return ResponseEntity.ok(leaveApplicationService.approve(id, request));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<LeaveApplicationResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(leaveApplicationService.cancel(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leaveApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
