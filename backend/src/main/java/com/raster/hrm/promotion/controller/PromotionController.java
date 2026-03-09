package com.raster.hrm.promotion.controller;

import com.raster.hrm.promotion.dto.PromotionRequest;
import com.raster.hrm.promotion.dto.PromotionResponse;
import com.raster.hrm.promotion.service.PromotionService;
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
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public ResponseEntity<Page<PromotionResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(promotionService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PromotionResponse>> getByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(promotionService.getByEmployeeId(employeeId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PromotionResponse>> getPendingPromotions() {
        return ResponseEntity.ok(promotionService.getPendingPromotions());
    }

    @PostMapping
    public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.create(request));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<PromotionResponse> approve(@PathVariable Long id,
                                                     @RequestParam Long approvedById) {
        return ResponseEntity.ok(promotionService.approve(id, approvedById));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<PromotionResponse> reject(@PathVariable Long id,
                                                    @RequestParam Long approvedById) {
        return ResponseEntity.ok(promotionService.reject(id, approvedById));
    }

    @PutMapping("/{id}/execute")
    public ResponseEntity<PromotionResponse> execute(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.execute(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
