package com.raster.hrm.leaveanalytics.controller;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.leaveanalytics.dto.AbsenteeismRateReport;
import com.raster.hrm.leaveanalytics.dto.LeaveTrendReport;
import com.raster.hrm.leaveanalytics.dto.LeaveUtilizationReport;
import com.raster.hrm.leaveanalytics.service.LeaveAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/leave-analytics")
public class LeaveAnalyticsController {

    private final LeaveAnalyticsService leaveAnalyticsService;

    public LeaveAnalyticsController(LeaveAnalyticsService leaveAnalyticsService) {
        this.leaveAnalyticsService = leaveAnalyticsService;
    }

    @GetMapping("/trend")
    public ResponseEntity<LeaveTrendReport> getLeaveTrend(
            @RequestParam int startYear,
            @RequestParam int startMonth,
            @RequestParam int endYear,
            @RequestParam int endMonth,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(leaveAnalyticsService.generateLeaveTrend(
                startYear, startMonth, endYear, endMonth, departmentId));
    }

    @GetMapping("/absenteeism-rate")
    public ResponseEntity<AbsenteeismRateReport> getAbsenteeismRate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(leaveAnalyticsService.generateAbsenteeismRate(
                startDate, endDate, departmentId));
    }

    @GetMapping("/utilization")
    public ResponseEntity<LeaveUtilizationReport> getLeaveUtilization(
            @RequestParam int year,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(leaveAnalyticsService.generateLeaveUtilization(year, departmentId));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam String reportType,
            @RequestParam(defaultValue = "CSV") String format,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer startMonth,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) Integer endMonth,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long departmentId) {

        if (!"CSV".equalsIgnoreCase(format)) {
            throw new BadRequestException(format + " export is not yet supported. Only CSV is currently available.");
        }

        var params = new HashMap<String, String>();
        if (startYear != null) {
            params.put("startYear", String.valueOf(startYear));
        }
        if (startMonth != null) {
            params.put("startMonth", String.valueOf(startMonth));
        }
        if (endYear != null) {
            params.put("endYear", String.valueOf(endYear));
        }
        if (endMonth != null) {
            params.put("endMonth", String.valueOf(endMonth));
        }
        if (year != null) {
            params.put("year", String.valueOf(year));
        }
        if (startDate != null) {
            params.put("startDate", startDate.toString());
        }
        if (endDate != null) {
            params.put("endDate", endDate.toString());
        }
        if (departmentId != null) {
            params.put("departmentId", String.valueOf(departmentId));
        }

        var csvBytes = leaveAnalyticsService.exportReportAsCsv(reportType, params);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + reportType.toLowerCase() + "_report.csv\"");

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }
}
