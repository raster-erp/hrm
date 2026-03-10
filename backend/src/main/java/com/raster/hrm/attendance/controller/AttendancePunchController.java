package com.raster.hrm.attendance.controller;

import com.raster.hrm.attendance.dto.AttendancePunchRequest;
import com.raster.hrm.attendance.dto.AttendancePunchResponse;
import com.raster.hrm.attendance.dto.PunchSyncRequest;
import com.raster.hrm.attendance.dto.PunchSyncResponse;
import com.raster.hrm.attendance.service.AttendancePunchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/attendance-punches")
public class AttendancePunchController {

    private final AttendancePunchService punchService;

    public AttendancePunchController(AttendancePunchService punchService) {
        this.punchService = punchService;
    }

    @GetMapping
    public ResponseEntity<Page<AttendancePunchResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(punchService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendancePunchResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(punchService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<AttendancePunchResponse>> getByEmployee(@PathVariable Long employeeId,
                                                                       Pageable pageable) {
        return ResponseEntity.ok(punchService.getByEmployeeId(employeeId, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<AttendancePunchResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable) {
        return ResponseEntity.ok(punchService.getByDateRange(from, to, pageable));
    }

    @GetMapping("/employee/{employeeId}/date-range")
    public ResponseEntity<Page<AttendancePunchResponse>> getByEmployeeAndDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable) {
        return ResponseEntity.ok(punchService.getByEmployeeAndDateRange(employeeId, from, to, pageable));
    }

    @PostMapping
    public ResponseEntity<AttendancePunchResponse> create(@Valid @RequestBody AttendancePunchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(punchService.create(request));
    }

    @PostMapping("/sync")
    public ResponseEntity<PunchSyncResponse> syncPunches(@Valid @RequestBody PunchSyncRequest request) {
        return ResponseEntity.ok(punchService.syncPunches(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        punchService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
