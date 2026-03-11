package com.raster.hrm.leavetype.controller;

import com.raster.hrm.leavetype.dto.LeaveTypeRequest;
import com.raster.hrm.leavetype.dto.LeaveTypeResponse;
import com.raster.hrm.leavetype.entity.LeaveTypeCategory;
import com.raster.hrm.leavetype.service.LeaveTypeService;
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
@RequestMapping("/api/v1/leave-types")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @GetMapping
    public ResponseEntity<Page<LeaveTypeResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(leaveTypeService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveTypeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveTypeService.getById(id));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<LeaveTypeResponse>> getByCategory(@PathVariable LeaveTypeCategory category) {
        return ResponseEntity.ok(leaveTypeService.getByCategory(category));
    }

    @GetMapping("/active")
    public ResponseEntity<List<LeaveTypeResponse>> getActive() {
        return ResponseEntity.ok(leaveTypeService.getActive());
    }

    @PostMapping
    public ResponseEntity<LeaveTypeResponse> create(@Valid @RequestBody LeaveTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveTypeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveTypeResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody LeaveTypeRequest request) {
        return ResponseEntity.ok(leaveTypeService.update(id, request));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<LeaveTypeResponse> updateActive(@PathVariable Long id,
                                                           @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(leaveTypeService.updateActive(id, body.get("active")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leaveTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
