package com.raster.hrm.device.controller;

import com.raster.hrm.device.dto.DeviceRequest;
import com.raster.hrm.device.dto.DeviceResponse;
import com.raster.hrm.device.entity.DeviceStatus;
import com.raster.hrm.device.entity.DeviceType;
import com.raster.hrm.device.service.DeviceService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<Page<DeviceResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(deviceService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getById(id));
    }

    @GetMapping("/serial/{serialNumber}")
    public ResponseEntity<DeviceResponse> getBySerialNumber(@PathVariable String serialNumber) {
        return ResponseEntity.ok(deviceService.getBySerialNumber(serialNumber));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeviceResponse>> getByStatus(@PathVariable DeviceStatus status) {
        return ResponseEntity.ok(deviceService.getByStatus(status));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<DeviceResponse>> getByType(@PathVariable DeviceType type) {
        return ResponseEntity.ok(deviceService.getByType(type));
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody DeviceRequest request) {
        return ResponseEntity.ok(deviceService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeviceResponse> updateStatus(@PathVariable Long id,
                                                        @RequestBody Map<String, String> body) {
        var status = DeviceStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(deviceService.updateStatus(id, status));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<DeviceResponse> recordSync(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.recordSync(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
