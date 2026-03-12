package com.raster.hrm.compoff.controller;

import com.raster.hrm.compoff.dto.CompOffApprovalRequest;
import com.raster.hrm.compoff.dto.CompOffBalanceResponse;
import com.raster.hrm.compoff.dto.CompOffCreditRequest;
import com.raster.hrm.compoff.dto.CompOffCreditResponse;
import com.raster.hrm.compoff.entity.CompOffStatus;
import com.raster.hrm.compoff.service.CompOffCreditService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comp-off-credits")
public class CompOffCreditController {

    private final CompOffCreditService compOffCreditService;

    public CompOffCreditController(CompOffCreditService compOffCreditService) {
        this.compOffCreditService = compOffCreditService;
    }

    @PostMapping
    public ResponseEntity<CompOffCreditResponse> createRequest(
            @Valid @RequestBody CompOffCreditRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(compOffCreditService.createRequest(request));
    }

    @GetMapping
    public ResponseEntity<Page<CompOffCreditResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(compOffCreditService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompOffCreditResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(compOffCreditService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<CompOffCreditResponse>> getByEmployee(
            @PathVariable Long employeeId, Pageable pageable) {
        return ResponseEntity.ok(compOffCreditService.getByEmployee(employeeId, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<CompOffCreditResponse>> getByStatus(
            @PathVariable CompOffStatus status, Pageable pageable) {
        return ResponseEntity.ok(compOffCreditService.getByStatus(status, pageable));
    }

    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<CompOffBalanceResponse> getBalance(@PathVariable Long employeeId) {
        return ResponseEntity.ok(compOffCreditService.getBalance(employeeId));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<CompOffCreditResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody CompOffApprovalRequest request) {
        return ResponseEntity.ok(compOffCreditService.approve(id, request));
    }

    @PostMapping("/expire")
    public ResponseEntity<Integer> expireCredits() {
        return ResponseEntity.ok(compOffCreditService.expireCredits());
    }
}
