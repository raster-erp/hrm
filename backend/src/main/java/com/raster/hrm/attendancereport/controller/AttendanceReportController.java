package com.raster.hrm.attendancereport.controller;

import com.raster.hrm.attendancereport.dto.AbsenteeListReport;
import com.raster.hrm.attendancereport.dto.DailyMusterReport;
import com.raster.hrm.attendancereport.dto.MonthlySummaryReport;
import com.raster.hrm.attendancereport.dto.ReportScheduleRequest;
import com.raster.hrm.attendancereport.dto.ReportScheduleResponse;
import com.raster.hrm.attendancereport.service.AttendanceReportService;
import com.raster.hrm.attendancereport.service.ReportScheduleService;
import com.raster.hrm.exception.BadRequestException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import java.time.LocalDate;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/attendance-reports")
public class AttendanceReportController {

    private final AttendanceReportService attendanceReportService;
    private final ReportScheduleService reportScheduleService;

    public AttendanceReportController(AttendanceReportService attendanceReportService,
                                      ReportScheduleService reportScheduleService) {
        this.attendanceReportService = attendanceReportService;
        this.reportScheduleService = reportScheduleService;
    }

    @GetMapping("/daily-muster")
    public ResponseEntity<DailyMusterReport> getDailyMuster(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(attendanceReportService.generateDailyMuster(date, departmentId));
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryReport> getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(attendanceReportService.generateMonthlySummary(year, month, departmentId));
    }

    @GetMapping("/absentee-list")
    public ResponseEntity<AbsenteeListReport> getAbsenteeList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(attendanceReportService.generateAbsenteeList(startDate, endDate, departmentId));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam String reportType,
            @RequestParam(defaultValue = "CSV") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long departmentId) {

        if (!"CSV".equalsIgnoreCase(format)) {
            throw new BadRequestException(format + " export is not yet supported. Only CSV is currently available.");
        }

        var params = new HashMap<String, String>();
        if (date != null) {
            params.put("date", date.toString());
        }
        if (year != null) {
            params.put("year", String.valueOf(year));
        }
        if (month != null) {
            params.put("month", String.valueOf(month));
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

        var csvBytes = attendanceReportService.exportReportAsCsv(reportType, params);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + reportType.toLowerCase() + "_report.csv\"");

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    // Schedule CRUD endpoints

    @GetMapping("/schedules")
    public ResponseEntity<Page<ReportScheduleResponse>> getAllSchedules(Pageable pageable) {
        return ResponseEntity.ok(reportScheduleService.getAll(pageable));
    }

    @GetMapping("/schedules/{id}")
    public ResponseEntity<ReportScheduleResponse> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(reportScheduleService.getById(id));
    }

    @PostMapping("/schedules")
    public ResponseEntity<ReportScheduleResponse> createSchedule(
            @Valid @RequestBody ReportScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportScheduleService.create(request));
    }

    @PutMapping("/schedules/{id}")
    public ResponseEntity<ReportScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ReportScheduleRequest request) {
        return ResponseEntity.ok(reportScheduleService.update(id, request));
    }

    @PatchMapping("/schedules/{id}/active")
    public ResponseEntity<ReportScheduleResponse> toggleScheduleActive(@PathVariable Long id) {
        return ResponseEntity.ok(reportScheduleService.toggleActive(id));
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        reportScheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
