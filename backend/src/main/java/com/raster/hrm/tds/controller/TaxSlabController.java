package com.raster.hrm.tds.controller;

import com.raster.hrm.tds.dto.TaxSlabRequest;
import com.raster.hrm.tds.dto.TaxSlabResponse;
import com.raster.hrm.tds.service.TaxSlabService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tax-slabs")
public class TaxSlabController {

    private final TaxSlabService taxSlabService;

    public TaxSlabController(TaxSlabService taxSlabService) {
        this.taxSlabService = taxSlabService;
    }

    @GetMapping
    public ResponseEntity<Page<TaxSlabResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(taxSlabService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaxSlabResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taxSlabService.getById(id));
    }

    @GetMapping("/regime/{regime}/year/{financialYear}")
    public ResponseEntity<List<TaxSlabResponse>> getByRegimeAndYear(@PathVariable String regime,
                                                                     @PathVariable String financialYear) {
        return ResponseEntity.ok(taxSlabService.getByRegimeAndYear(regime, financialYear));
    }

    @PostMapping
    public ResponseEntity<TaxSlabResponse> create(@Valid @RequestBody TaxSlabRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taxSlabService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaxSlabResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody TaxSlabRequest request) {
        return ResponseEntity.ok(taxSlabService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taxSlabService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
