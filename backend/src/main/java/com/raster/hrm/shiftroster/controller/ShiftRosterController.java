package com.raster.hrm.shiftroster.controller;

import com.raster.hrm.shiftroster.dto.BulkShiftRosterRequest;
import com.raster.hrm.shiftroster.dto.ShiftRosterRequest;
import com.raster.hrm.shiftroster.dto.ShiftRosterResponse;
import com.raster.hrm.shiftroster.service.ShiftRosterService;
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
@RequestMapping("/api/v1/shift-rosters")
public class ShiftRosterController {

    private final ShiftRosterService shiftRosterService;

    public ShiftRosterController(ShiftRosterService shiftRosterService) {
        this.shiftRosterService = shiftRosterService;
    }

    @GetMapping
    public ResponseEntity<Page<ShiftRosterResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(shiftRosterService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftRosterResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shiftRosterService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ShiftRosterResponse>> getByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(shiftRosterService.getByEmployeeId(employeeId));
    }

    @PostMapping
    public ResponseEntity<ShiftRosterResponse> create(@Valid @RequestBody ShiftRosterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftRosterService.create(request));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ShiftRosterResponse>> bulkCreate(@Valid @RequestBody BulkShiftRosterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftRosterService.bulkCreate(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftRosterResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody ShiftRosterRequest request) {
        return ResponseEntity.ok(shiftRosterService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shiftRosterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
