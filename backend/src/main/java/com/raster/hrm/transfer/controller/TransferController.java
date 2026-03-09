package com.raster.hrm.transfer.controller;

import com.raster.hrm.transfer.dto.TransferRequest;
import com.raster.hrm.transfer.dto.TransferResponse;
import com.raster.hrm.transfer.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping
    public ResponseEntity<Page<TransferResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(transferService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<TransferResponse>> getByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(transferService.getByEmployeeId(employeeId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<TransferResponse>> getPendingTransfers() {
        return ResponseEntity.ok(transferService.getPendingTransfers());
    }

    @PostMapping
    public ResponseEntity<TransferResponse> create(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferService.create(request));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<TransferResponse> approve(@PathVariable Long id,
                                                    @RequestParam Long approvedById) {
        return ResponseEntity.ok(transferService.approve(id, approvedById));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<TransferResponse> reject(@PathVariable Long id,
                                                   @RequestParam Long approvedById) {
        return ResponseEntity.ok(transferService.reject(id, approvedById));
    }

    @PutMapping("/{id}/execute")
    public ResponseEntity<TransferResponse> execute(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.execute(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transferService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
