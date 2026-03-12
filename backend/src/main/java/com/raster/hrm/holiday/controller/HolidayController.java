package com.raster.hrm.holiday.controller;

import com.raster.hrm.holiday.dto.HolidayRequest;
import com.raster.hrm.holiday.dto.HolidayResponse;
import com.raster.hrm.holiday.entity.HolidayType;
import com.raster.hrm.holiday.service.HolidayService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/holidays")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @PostMapping
    public ResponseEntity<HolidayResponse> create(@Valid @RequestBody HolidayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(holidayService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HolidayResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody HolidayRequest request) {
        return ResponseEntity.ok(holidayService.update(id, request));
    }

    @GetMapping
    public ResponseEntity<Page<HolidayResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(holidayService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HolidayResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(holidayService.getById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<HolidayResponse>> getByType(
            @PathVariable HolidayType type, Pageable pageable) {
        return ResponseEntity.ok(holidayService.getByType(type, pageable));
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<Page<HolidayResponse>> getByRegion(
            @PathVariable String region, Pageable pageable) {
        return ResponseEntity.ok(holidayService.getByRegion(region, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<HolidayResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(holidayService.getByDateRange(start, end));
    }

    @GetMapping("/active")
    public ResponseEntity<List<HolidayResponse>> getActiveByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String region) {
        if (region != null) {
            return ResponseEntity.ok(holidayService.getActiveByRegionAndDateRange(region, start, end));
        }
        return ResponseEntity.ok(holidayService.getActiveByDateRange(start, end));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<HolidayResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(holidayService.deactivate(id));
    }
}
