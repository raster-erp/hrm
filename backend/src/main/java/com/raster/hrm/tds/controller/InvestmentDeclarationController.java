package com.raster.hrm.tds.controller;

import com.raster.hrm.tds.dto.InvestmentDeclarationRequest;
import com.raster.hrm.tds.dto.InvestmentDeclarationResponse;
import com.raster.hrm.tds.dto.ProofSubmissionRequest;
import com.raster.hrm.tds.dto.ProofVerificationRequest;
import com.raster.hrm.tds.service.InvestmentDeclarationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/investment-declarations")
public class InvestmentDeclarationController {

    private final InvestmentDeclarationService investmentDeclarationService;

    public InvestmentDeclarationController(InvestmentDeclarationService investmentDeclarationService) {
        this.investmentDeclarationService = investmentDeclarationService;
    }

    @GetMapping
    public ResponseEntity<Page<InvestmentDeclarationResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(investmentDeclarationService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentDeclarationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(investmentDeclarationService.getById(id));
    }

    @GetMapping("/employee/{employeeId}/year/{financialYear}")
    public ResponseEntity<InvestmentDeclarationResponse> getByEmployeeAndYear(@PathVariable Long employeeId,
                                                                              @PathVariable String financialYear) {
        return ResponseEntity.ok(investmentDeclarationService.getByEmployeeAndYear(employeeId, financialYear));
    }

    @GetMapping("/year/{financialYear}")
    public ResponseEntity<Page<InvestmentDeclarationResponse>> getByFinancialYear(@PathVariable String financialYear,
                                                                                   Pageable pageable) {
        return ResponseEntity.ok(investmentDeclarationService.getByFinancialYear(financialYear, pageable));
    }

    @PostMapping
    public ResponseEntity<InvestmentDeclarationResponse> create(@Valid @RequestBody InvestmentDeclarationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investmentDeclarationService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentDeclarationResponse> update(@PathVariable Long id,
                                                                 @Valid @RequestBody InvestmentDeclarationRequest request) {
        return ResponseEntity.ok(investmentDeclarationService.update(id, request));
    }

    @PatchMapping("/{id}/submit")
    public ResponseEntity<InvestmentDeclarationResponse> submit(@PathVariable Long id) {
        return ResponseEntity.ok(investmentDeclarationService.submit(id));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<InvestmentDeclarationResponse> verify(@PathVariable Long id,
                                                                 @RequestBody Map<String, Long> body) {
        return ResponseEntity.ok(investmentDeclarationService.verify(id, body.get("verifiedBy")));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<InvestmentDeclarationResponse> reject(@PathVariable Long id,
                                                                 @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(investmentDeclarationService.reject(id, body.get("remarks")));
    }

    @PostMapping("/proof/submit")
    public ResponseEntity<Void> submitProof(@Valid @RequestBody ProofSubmissionRequest request) {
        investmentDeclarationService.submitProof(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/proof/verify")
    public ResponseEntity<Void> verifyProof(@Valid @RequestBody ProofVerificationRequest request) {
        investmentDeclarationService.verifyProof(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        investmentDeclarationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
