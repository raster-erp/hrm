package com.raster.hrm.tds.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record InvestmentDeclarationRequest(
        @NotNull(message = "Employee ID is required") Long employeeId,
        @NotBlank(message = "Financial year is required") @Size(max = 10) String financialYear,
        @NotNull(message = "Regime is required") String regime,
        @Size(max = 500) String remarks,
        @Valid List<InvestmentDeclarationItemRequest> items
) {}
