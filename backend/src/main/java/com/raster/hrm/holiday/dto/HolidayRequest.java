package com.raster.hrm.holiday.dto;

import com.raster.hrm.holiday.entity.HolidayType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record HolidayRequest(
    @NotNull @Size(max = 100) String name,
    @NotNull LocalDate date,
    @NotNull HolidayType type,
    @Size(max = 100) String region,
    @Size(max = 500) String description
) {}
