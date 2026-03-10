package com.raster.hrm.overtime.controller;

import com.raster.hrm.overtime.dto.OvertimeApprovalRequest;
import com.raster.hrm.overtime.dto.OvertimeRecordRequest;
import com.raster.hrm.overtime.dto.OvertimeRecordResponse;
import com.raster.hrm.overtime.dto.OvertimeSummaryResponse;
import com.raster.hrm.overtime.entity.OvertimeStatus;
import com.raster.hrm.overtime.service.OvertimeRecordService;
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
@RequestMapping("/api/v1/overtime-records")
public class OvertimeRecordController {

    private final OvertimeRecordService overtimeRecordService;

    public OvertimeRecordController(OvertimeRecordService overtimeRecordService) {
        this.overtimeRecordService = overtimeRecordService;
    }

    @GetMapping
    public ResponseEntity<Page<OvertimeRecordResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(overtimeRecordService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OvertimeRecordResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(overtimeRecordService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<OvertimeRecordResponse>> getByEmployee(@PathVariable Long employeeId,
                                                                       Pageable pageable) {
        return ResponseEntity.ok(overtimeRecordService.getByEmployeeId(employeeId, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OvertimeRecordResponse>> getByStatus(@PathVariable OvertimeStatus status,
                                                                     Pageable pageable) {
        return ResponseEntity.ok(overtimeRecordService.getByStatus(status, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<OvertimeRecordResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        return ResponseEntity.ok(overtimeRecordService.getByDateRange(startDate, endDate, pageable));
    }

    @PostMapping
    public ResponseEntity<OvertimeRecordResponse> create(@Valid @RequestBody OvertimeRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(overtimeRecordService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OvertimeRecordResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody OvertimeRecordRequest request) {
        return ResponseEntity.ok(overtimeRecordService.update(id, request));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<OvertimeRecordResponse> approve(@PathVariable Long id,
                                                           @Valid @RequestBody OvertimeApprovalRequest request) {
        return ResponseEntity.ok(overtimeRecordService.approve(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        overtimeRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/detect")
    public ResponseEntity<List<OvertimeRecordResponse>> detectOvertime(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long policyId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(overtimeRecordService.detectOvertime(employeeId, date, policyId));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<OvertimeSummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(overtimeRecordService.getSummary(startDate, endDate));
    }
}
