package com.raster.hrm.tds.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record InvestmentDeclarationItemRequest(
        @NotBlank(message = "Section is required") @Size(max = 20) String section,
        @NotBlank(message = "Description is required") @Size(max = 200) String description,
        @NotNull(message = "Declared amount is required") BigDecimal declaredAmount
) {}
