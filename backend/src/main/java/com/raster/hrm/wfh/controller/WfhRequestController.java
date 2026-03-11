package com.raster.hrm.wfh.controller;

import com.raster.hrm.wfh.dto.WfhApprovalRequest;
import com.raster.hrm.wfh.dto.WfhDashboardResponse;
import com.raster.hrm.wfh.dto.WfhRequestCreateRequest;
import com.raster.hrm.wfh.dto.WfhRequestResponse;
import com.raster.hrm.wfh.entity.WfhStatus;
import com.raster.hrm.wfh.service.WfhRequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wfh-requests")
public class WfhRequestController {

    private final WfhRequestService wfhRequestService;

    public WfhRequestController(WfhRequestService wfhRequestService) {
        this.wfhRequestService = wfhRequestService;
    }

    @GetMapping
    public ResponseEntity<Page<WfhRequestResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(wfhRequestService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WfhRequestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(wfhRequestService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<WfhRequestResponse>> getByEmployee(@PathVariable Long employeeId,
                                                                    Pageable pageable) {
        return ResponseEntity.ok(wfhRequestService.getByEmployeeId(employeeId, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<WfhRequestResponse>> getByStatus(@PathVariable WfhStatus status,
                                                                  Pageable pageable) {
        return ResponseEntity.ok(wfhRequestService.getByStatus(status, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<WfhRequestResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        return ResponseEntity.ok(wfhRequestService.getByDateRange(startDate, endDate, pageable));
    }

    @PostMapping
    public ResponseEntity<WfhRequestResponse> create(@Valid @RequestBody WfhRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wfhRequestService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WfhRequestResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody WfhRequestCreateRequest request) {
        return ResponseEntity.ok(wfhRequestService.update(id, request));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<WfhRequestResponse> approve(@PathVariable Long id,
                                                        @Valid @RequestBody WfhApprovalRequest request) {
        return ResponseEntity.ok(wfhRequestService.approve(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        wfhRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<WfhDashboardResponse>> getDashboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(wfhRequestService.getDashboard(startDate, endDate));
    }
}
