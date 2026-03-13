package com.raster.hrm.tds.controller;

import com.raster.hrm.tds.dto.Form16DataResponse;
import com.raster.hrm.tds.dto.TaxComputationRequest;
import com.raster.hrm.tds.dto.TaxComputationResponse;
import com.raster.hrm.tds.service.TaxComputationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tax-computations")
public class TaxComputationController {

    private final TaxComputationService taxComputationService;

    public TaxComputationController(TaxComputationService taxComputationService) {
        this.taxComputationService = taxComputationService;
    }

    @PostMapping("/compute")
    public ResponseEntity<TaxComputationResponse> computeMonthlyTds(@Valid @RequestBody TaxComputationRequest request) {
        return ResponseEntity.ok(taxComputationService.computeMonthlyTds(request));
    }

    @GetMapping("/employee/{employeeId}/year/{financialYear}")
    public ResponseEntity<List<TaxComputationResponse>> getByEmployeeAndYear(@PathVariable Long employeeId,
                                                                              @PathVariable String financialYear) {
        return ResponseEntity.ok(taxComputationService.getByEmployeeAndYear(employeeId, financialYear));
    }

    @GetMapping("/employee/{employeeId}/year/{financialYear}/month/{month}")
    public ResponseEntity<TaxComputationResponse> getByEmployeeYearMonth(@PathVariable Long employeeId,
                                                                          @PathVariable String financialYear,
                                                                          @PathVariable int month) {
        return ResponseEntity.ok(taxComputationService.getByEmployeeYearMonth(employeeId, financialYear, month));
    }

    @GetMapping("/form16/employee/{employeeId}/year/{financialYear}")
    public ResponseEntity<Form16DataResponse> generateForm16Data(@PathVariable Long employeeId,
                                                                  @PathVariable String financialYear) {
        return ResponseEntity.ok(taxComputationService.generateForm16Data(employeeId, financialYear));
    }
}
