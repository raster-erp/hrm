package com.raster.hrm.employee.dto;

import java.time.LocalDate;

public record EmployeeSearchCriteria(
        String name,
        Long departmentId,
        String status,
        LocalDate joiningDateFrom,
        LocalDate joiningDateTo
) {
}
