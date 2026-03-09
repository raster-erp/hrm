package com.raster.hrm.idcard.controller;

import com.raster.hrm.idcard.dto.IdCardRequest;
import com.raster.hrm.idcard.dto.IdCardResponse;
import com.raster.hrm.idcard.entity.IdCardStatus;
import com.raster.hrm.idcard.service.IdCardService;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/id-cards")
public class IdCardController {

    private final IdCardService idCardService;

    public IdCardController(IdCardService idCardService) {
        this.idCardService = idCardService;
    }

    @GetMapping
    public ResponseEntity<Page<IdCardResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(idCardService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IdCardResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(idCardService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<IdCardResponse>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(idCardService.getByEmployeeId(employeeId));
    }

    @PostMapping
    public ResponseEntity<IdCardResponse> create(@Valid @RequestBody IdCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(idCardService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IdCardResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody IdCardRequest request) {
        return ResponseEntity.ok(idCardService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<IdCardResponse> updateStatus(@PathVariable Long id,
                                                        @RequestBody Map<String, String> body) {
        var status = IdCardStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(idCardService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        idCardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
