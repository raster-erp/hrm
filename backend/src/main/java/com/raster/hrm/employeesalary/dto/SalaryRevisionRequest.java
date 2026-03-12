package com.raster.hrm.employeesalary.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalaryRevisionRequest(
        @NotNull(message = "Salary structure ID is required")
        Long salaryStructureId,

        @NotNull(message = "New CTC is required")
        BigDecimal ctc,

        @NotNull(message = "New basic salary is required")
        BigDecimal basicSalary,

        @NotNull(message = "Effective date is required")
        LocalDate effectiveDate,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}
