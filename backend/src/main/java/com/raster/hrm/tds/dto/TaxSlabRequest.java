package com.raster.hrm.tds.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TaxSlabRequest(
        @NotNull(message = "Regime is required") String regime,
        @NotBlank(message = "Financial year is required") @Size(max = 10) String financialYear,
        @NotNull(message = "Slab from is required") BigDecimal slabFrom,
        BigDecimal slabTo,
        @NotNull(message = "Rate is required") BigDecimal rate,
        @Size(max = 200) String description
) {}
