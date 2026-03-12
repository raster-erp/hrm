package com.raster.hrm.leavecalendar.controller;

import com.raster.hrm.leavecalendar.dto.LeaveCalendarEntry;
import com.raster.hrm.leavecalendar.dto.TeamAvailabilityResponse;
import com.raster.hrm.leavecalendar.service.LeaveCalendarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-calendar")
public class LeaveCalendarController {

    private final LeaveCalendarService leaveCalendarService;

    public LeaveCalendarController(LeaveCalendarService leaveCalendarService) {
        this.leaveCalendarService = leaveCalendarService;
    }

    @GetMapping("/entries")
    public ResponseEntity<List<LeaveCalendarEntry>> getCalendarEntries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String region) {
        return ResponseEntity.ok(
                leaveCalendarService.getCalendarEntries(start, end, employeeId, departmentId, region));
    }

    @GetMapping("/team-availability")
    public ResponseEntity<List<TeamAvailabilityResponse>> getTeamAvailability(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(leaveCalendarService.getTeamAvailability(departmentId, start, end));
    }
}
