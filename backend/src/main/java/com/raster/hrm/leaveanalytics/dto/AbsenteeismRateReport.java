package com.raster.hrm.leaveanalytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AbsenteeismRateReport(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal overallRate,
        List<AbsenteeismRateEntry> entries
) {
}
