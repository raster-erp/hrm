package com.raster.hrm.designation.dto;

public record DesignationResponse(
        Long id,
        String title,
        String code,
        Integer level,
        String grade,
        Long departmentId,
        String departmentName,
        String description,
        Boolean active
) {
}
