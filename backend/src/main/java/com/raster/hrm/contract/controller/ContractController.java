package com.raster.hrm.contract.controller;

import com.raster.hrm.contract.dto.ContractAmendmentRequest;
import com.raster.hrm.contract.dto.ContractAmendmentResponse;
import com.raster.hrm.contract.dto.ContractRequest;
import com.raster.hrm.contract.dto.ContractResponse;
import com.raster.hrm.contract.service.ContractService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public ResponseEntity<Page<ContractResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(contractService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ContractResponse>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(contractService.getByEmployee(employeeId));
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<ContractResponse>> getExpiringContracts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(contractService.getExpiringContracts(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<ContractResponse> create(@Valid @RequestBody ContractRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contractService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContractResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody ContractRequest request) {
        return ResponseEntity.ok(contractService.update(id, request));
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<ContractResponse> renewContract(@PathVariable Long id,
                                                           @Valid @RequestBody ContractRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contractService.renewContract(id, request));
    }

    @PostMapping("/{id}/amendments")
    public ResponseEntity<ContractAmendmentResponse> addAmendment(@PathVariable Long id,
                                                                   @Valid @RequestBody ContractAmendmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contractService.addAmendment(id, request));
    }

    @GetMapping("/{id}/amendments")
    public ResponseEntity<List<ContractAmendmentResponse>> getAmendments(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getAmendments(id));
    }
}
