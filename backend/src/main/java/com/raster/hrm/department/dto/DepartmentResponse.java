package com.raster.hrm.department.dto;

import java.util.List;

public record DepartmentResponse(
        Long id,
        String name,
        String code,
        Long parentId,
        String parentName,
        String description,
        Boolean active,
        List<DepartmentResponse> children
) {
}
