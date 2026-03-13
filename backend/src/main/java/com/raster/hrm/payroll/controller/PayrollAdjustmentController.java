package com.raster.hrm.payroll.controller;

import com.raster.hrm.payroll.dto.PayrollAdjustmentRequest;
import com.raster.hrm.payroll.dto.PayrollAdjustmentResponse;
import com.raster.hrm.payroll.service.PayrollAdjustmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payroll-adjustments")
public class PayrollAdjustmentController {

    private final PayrollAdjustmentService payrollAdjustmentService;

    public PayrollAdjustmentController(PayrollAdjustmentService payrollAdjustmentService) {
        this.payrollAdjustmentService = payrollAdjustmentService;
    }

    @GetMapping("/run/{runId}")
    public ResponseEntity<List<PayrollAdjustmentResponse>> getByRunId(@PathVariable Long runId) {
        return ResponseEntity.ok(payrollAdjustmentService.getByRunId(runId));
    }

    @PostMapping
    public ResponseEntity<PayrollAdjustmentResponse> create(@Valid @RequestBody PayrollAdjustmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollAdjustmentService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        payrollAdjustmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
