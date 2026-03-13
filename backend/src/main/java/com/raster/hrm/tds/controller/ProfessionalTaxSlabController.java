package com.raster.hrm.tds.controller;

import com.raster.hrm.tds.dto.ProfessionalTaxSlabRequest;
import com.raster.hrm.tds.dto.ProfessionalTaxSlabResponse;
import com.raster.hrm.tds.service.ProfessionalTaxSlabService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/professional-tax-slabs")
public class ProfessionalTaxSlabController {

    private final ProfessionalTaxSlabService professionalTaxSlabService;

    public ProfessionalTaxSlabController(ProfessionalTaxSlabService professionalTaxSlabService) {
        this.professionalTaxSlabService = professionalTaxSlabService;
    }

    @GetMapping
    public ResponseEntity<Page<ProfessionalTaxSlabResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(professionalTaxSlabService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalTaxSlabResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(professionalTaxSlabService.getById(id));
    }

    @GetMapping("/state/{state}")
    public ResponseEntity<List<ProfessionalTaxSlabResponse>> getByState(@PathVariable String state) {
        return ResponseEntity.ok(professionalTaxSlabService.getByState(state));
    }

    @PostMapping
    public ResponseEntity<ProfessionalTaxSlabResponse> create(@Valid @RequestBody ProfessionalTaxSlabRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(professionalTaxSlabService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalTaxSlabResponse> update(@PathVariable Long id,
                                                               @Valid @RequestBody ProfessionalTaxSlabRequest request) {
        return ResponseEntity.ok(professionalTaxSlabService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        professionalTaxSlabService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/compute/employee/{employeeId}/month/{month}")
    public ResponseEntity<Map<String, BigDecimal>> computeProfessionalTax(@PathVariable Long employeeId,
                                                                          @PathVariable int month) {
        BigDecimal tax = professionalTaxSlabService.computeProfessionalTax(employeeId, month);
        return ResponseEntity.ok(Map.of("professionalTax", tax));
    }
}
