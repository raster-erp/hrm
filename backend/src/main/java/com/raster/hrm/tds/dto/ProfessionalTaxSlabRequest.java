package com.raster.hrm.tds.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProfessionalTaxSlabRequest(
        @NotBlank(message = "State is required") @Size(max = 100) String state,
        @NotNull(message = "Slab from is required") BigDecimal slabFrom,
        BigDecimal slabTo,
        @NotNull(message = "Monthly tax is required") BigDecimal monthlyTax,
        BigDecimal februaryTax
) {}
