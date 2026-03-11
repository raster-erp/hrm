package com.raster.hrm.wfh.controller;

import com.raster.hrm.wfh.dto.WfhCheckInRequest;
import com.raster.hrm.wfh.dto.WfhCheckInResponse;
import com.raster.hrm.wfh.service.WfhActivityLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wfh-activity-logs")
public class WfhActivityLogController {

    private final WfhActivityLogService wfhActivityLogService;

    public WfhActivityLogController(WfhActivityLogService wfhActivityLogService) {
        this.wfhActivityLogService = wfhActivityLogService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<WfhCheckInResponse> checkIn(@Valid @RequestBody WfhCheckInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wfhActivityLogService.checkIn(request));
    }

    @PatchMapping("/{id}/check-out")
    public ResponseEntity<WfhCheckInResponse> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(wfhActivityLogService.checkOut(id));
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<WfhCheckInResponse>> getByRequestId(@PathVariable Long requestId) {
        return ResponseEntity.ok(wfhActivityLogService.getByRequestId(requestId));
    }

    @GetMapping("/request/{requestId}/active")
    public ResponseEntity<WfhCheckInResponse> getActiveSession(@PathVariable Long requestId) {
        return wfhActivityLogService.getActiveSession(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
