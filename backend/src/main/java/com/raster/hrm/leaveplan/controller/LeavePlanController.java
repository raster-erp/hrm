package com.raster.hrm.leaveplan.controller;

import com.raster.hrm.leaveplan.dto.LeavePlanRequest;
import com.raster.hrm.leaveplan.dto.LeavePlanResponse;
import com.raster.hrm.leaveplan.entity.LeavePlanStatus;
import com.raster.hrm.leaveplan.service.LeavePlanService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-plans")
public class LeavePlanController {

    private final LeavePlanService leavePlanService;

    public LeavePlanController(LeavePlanService leavePlanService) {
        this.leavePlanService = leavePlanService;
    }

    @PostMapping
    public ResponseEntity<LeavePlanResponse> create(@Valid @RequestBody LeavePlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leavePlanService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeavePlanResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody LeavePlanRequest request) {
        return ResponseEntity.ok(leavePlanService.update(id, request));
    }

    @GetMapping
    public ResponseEntity<Page<LeavePlanResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(leavePlanService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeavePlanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leavePlanService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<LeavePlanResponse>> getByEmployee(
            @PathVariable Long employeeId, Pageable pageable) {
        return ResponseEntity.ok(leavePlanService.getByEmployee(employeeId, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<LeavePlanResponse>> getByStatus(
            @PathVariable LeavePlanStatus status, Pageable pageable) {
        return ResponseEntity.ok(leavePlanService.getByStatus(status, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<LeavePlanResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(leavePlanService.getByDateRange(start, end));
    }

    @GetMapping("/employee/{employeeId}/date-range")
    public ResponseEntity<List<LeavePlanResponse>> getByEmployeeAndDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(leavePlanService.getByEmployeeAndDateRange(employeeId, start, end));
    }

    @GetMapping("/department/{departmentId}/date-range")
    public ResponseEntity<List<LeavePlanResponse>> getByDepartmentAndDateRange(
            @PathVariable Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(leavePlanService.getByDepartmentAndDateRange(departmentId, start, end));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<LeavePlanResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(leavePlanService.cancel(id));
    }

    @PatchMapping("/{id}/convert")
    public ResponseEntity<LeavePlanResponse> convertToApplication(@PathVariable Long id) {
        return ResponseEntity.ok(leavePlanService.convertToApplication(id));
    }
}
