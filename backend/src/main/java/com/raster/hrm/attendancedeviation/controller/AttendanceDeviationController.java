package com.raster.hrm.attendancedeviation.controller;

import com.raster.hrm.attendancedeviation.dto.AttendanceDeviationRequest;
import com.raster.hrm.attendancedeviation.dto.AttendanceDeviationResponse;
import com.raster.hrm.attendancedeviation.dto.DeviationApprovalRequest;
import com.raster.hrm.attendancedeviation.dto.DeviationSummaryResponse;
import com.raster.hrm.attendancedeviation.entity.DeviationStatus;
import com.raster.hrm.attendancedeviation.entity.DeviationType;
import com.raster.hrm.attendancedeviation.service.AttendanceDeviationService;
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
@RequestMapping("/api/v1/attendance-deviations")
public class AttendanceDeviationController {

    private final AttendanceDeviationService attendanceDeviationService;

    public AttendanceDeviationController(AttendanceDeviationService attendanceDeviationService) {
        this.attendanceDeviationService = attendanceDeviationService;
    }

    @GetMapping
    public ResponseEntity<Page<AttendanceDeviationResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(attendanceDeviationService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceDeviationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceDeviationService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<AttendanceDeviationResponse>> getByEmployee(@PathVariable Long employeeId,
                                                                            Pageable pageable) {
        return ResponseEntity.ok(attendanceDeviationService.getByEmployeeId(employeeId, pageable));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<AttendanceDeviationResponse>> getByType(@PathVariable DeviationType type,
                                                                        Pageable pageable) {
        return ResponseEntity.ok(attendanceDeviationService.getByType(type, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<AttendanceDeviationResponse>> getByStatus(@PathVariable DeviationStatus status,
                                                                          Pageable pageable) {
        return ResponseEntity.ok(attendanceDeviationService.getByStatus(status, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<AttendanceDeviationResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        return ResponseEntity.ok(attendanceDeviationService.getByDateRange(startDate, endDate, pageable));
    }

    @PostMapping
    public ResponseEntity<AttendanceDeviationResponse> create(@Valid @RequestBody AttendanceDeviationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceDeviationService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceDeviationResponse> update(@PathVariable Long id,
                                                               @Valid @RequestBody AttendanceDeviationRequest request) {
        return ResponseEntity.ok(attendanceDeviationService.update(id, request));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<AttendanceDeviationResponse> approve(@PathVariable Long id,
                                                                @Valid @RequestBody DeviationApprovalRequest request) {
        return ResponseEntity.ok(attendanceDeviationService.approve(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attendanceDeviationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/detect")
    public ResponseEntity<List<AttendanceDeviationResponse>> detectDeviations(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceDeviationService.detectDeviations(employeeId, date));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<DeviationSummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceDeviationService.getSummary(startDate, endDate));
    }
}
