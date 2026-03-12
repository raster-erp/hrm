package com.raster.hrm.salarystructure.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SalaryStructureResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        List<SalaryStructureComponentResponse> components,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
