package com.raster.hrm.leavecalendar.dto;

import java.time.LocalDate;
import java.util.List;

public record TeamAvailabilityResponse(
    LocalDate date,
    int totalMembers,
    int availableMembers,
    int onLeave,
    int onPlannedLeave,
    double coveragePercentage,
    List<String> absentEmployees
) {}
