package com.raster.hrm.holiday.dto;

import com.raster.hrm.holiday.entity.HolidayType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record HolidayResponse(
    Long id,
    String name,
    LocalDate date,
    HolidayType type,
    String region,
    String description,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
