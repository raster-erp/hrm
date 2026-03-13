package com.raster.hrm.payroll.controller;

import com.raster.hrm.payroll.dto.PayrollDetailResponse;
import com.raster.hrm.payroll.dto.PayrollRunRequest;
import com.raster.hrm.payroll.dto.PayrollRunResponse;
import com.raster.hrm.payroll.service.PayrollRunService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payroll-runs")
public class PayrollRunController {

    private final PayrollRunService payrollRunService;

    public PayrollRunController(PayrollRunService payrollRunService) {
        this.payrollRunService = payrollRunService;
    }

    @GetMapping
    public ResponseEntity<Page<PayrollRunResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(payrollRunService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollRunResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(payrollRunService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PayrollRunResponse> initialize(@Valid @RequestBody PayrollRunRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollRunService.initialize(request));
    }

    @PostMapping("/{id}/compute")
    public ResponseEntity<PayrollRunResponse> computePayroll(@PathVariable Long id) {
        return ResponseEntity.ok(payrollRunService.computePayroll(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<Page<PayrollDetailResponse>> getDetails(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(payrollRunService.getDetails(id, pageable));
    }

    @GetMapping("/{id}/details/employee/{employeeId}")
    public ResponseEntity<PayrollDetailResponse> getDetailByEmployee(@PathVariable Long id,
                                                                      @PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollRunService.getDetailByEmployee(id, employeeId));
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<PayrollRunResponse> verify(@PathVariable Long id) {
        return ResponseEntity.ok(payrollRunService.verify(id));
    }

    @PostMapping("/{id}/finalize")
    public ResponseEntity<PayrollRunResponse> finalizeRun(@PathVariable Long id) {
        return ResponseEntity.ok(payrollRunService.finalizeRun(id));
    }

    @PostMapping("/{id}/reverse")
    public ResponseEntity<PayrollRunResponse> reverse(@PathVariable Long id) {
        return ResponseEntity.ok(payrollRunService.reverse(id));
    }
}
