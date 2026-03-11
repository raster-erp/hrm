package com.raster.hrm.attendanceregularization.controller;

import com.raster.hrm.attendanceregularization.dto.RegularizationApprovalRequest;
import com.raster.hrm.attendanceregularization.dto.RegularizationRequestRequest;
import com.raster.hrm.attendanceregularization.dto.RegularizationRequestResponse;
import com.raster.hrm.attendanceregularization.entity.RegularizationStatus;
import com.raster.hrm.attendanceregularization.entity.RegularizationType;
import com.raster.hrm.attendanceregularization.service.RegularizationRequestService;
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
@RequestMapping("/api/v1/regularization-requests")
public class RegularizationRequestController {

    private final RegularizationRequestService regularizationRequestService;

    public RegularizationRequestController(RegularizationRequestService regularizationRequestService) {
        this.regularizationRequestService = regularizationRequestService;
    }

    @GetMapping
    public ResponseEntity<Page<RegularizationRequestResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(regularizationRequestService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegularizationRequestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(regularizationRequestService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<RegularizationRequestResponse>> getByEmployee(@PathVariable Long employeeId,
                                                                              Pageable pageable) {
        return ResponseEntity.ok(regularizationRequestService.getByEmployeeId(employeeId, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<RegularizationRequestResponse>> getByStatus(@PathVariable RegularizationStatus status,
                                                                            Pageable pageable) {
        return ResponseEntity.ok(regularizationRequestService.getByStatus(status, pageable));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<RegularizationRequestResponse>> getByType(@PathVariable RegularizationType type,
                                                                          Pageable pageable) {
        return ResponseEntity.ok(regularizationRequestService.getByType(type, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<RegularizationRequestResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        return ResponseEntity.ok(regularizationRequestService.getByDateRange(startDate, endDate, pageable));
    }

    @GetMapping("/employee/{employeeId}/date-range")
    public ResponseEntity<List<RegularizationRequestResponse>> getByEmployeeAndDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(regularizationRequestService.getByEmployeeAndDateRange(employeeId, startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<RegularizationRequestResponse> create(@Valid @RequestBody RegularizationRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(regularizationRequestService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegularizationRequestResponse> update(@PathVariable Long id,
                                                                 @Valid @RequestBody RegularizationRequestRequest request) {
        return ResponseEntity.ok(regularizationRequestService.update(id, request));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<RegularizationRequestResponse> approve(@PathVariable Long id,
                                                                  @Valid @RequestBody RegularizationApprovalRequest request) {
        return ResponseEntity.ok(regularizationRequestService.approve(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        regularizationRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
