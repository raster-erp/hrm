package com.raster.hrm.shift.controller;

import com.raster.hrm.shift.dto.ShiftRequest;
import com.raster.hrm.shift.dto.ShiftResponse;
import com.raster.hrm.shift.entity.ShiftType;
import com.raster.hrm.shift.service.ShiftService;
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
@RequestMapping("/api/v1/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public ResponseEntity<Page<ShiftResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(shiftService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.getById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ShiftResponse>> getByType(@PathVariable ShiftType type) {
        return ResponseEntity.ok(shiftService.getByType(type));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ShiftResponse>> getActive() {
        return ResponseEntity.ok(shiftService.getActive());
    }

    @PostMapping
    public ResponseEntity<ShiftResponse> create(@Valid @RequestBody ShiftRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody ShiftRequest request) {
        return ResponseEntity.ok(shiftService.update(id, request));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ShiftResponse> updateActive(@PathVariable Long id,
                                                       @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(shiftService.updateActive(id, body.get("active")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shiftService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
