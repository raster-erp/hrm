package com.raster.hrm.leavecalendar.dto;

import java.time.LocalDate;

public record LeaveCalendarEntry(
    Long id,
    String type,
    Long employeeId,
    String employeeName,
    String leaveTypeName,
    String leaveTypeCategory,
    LocalDate fromDate,
    LocalDate toDate,
    String status,
    String color
) {}
