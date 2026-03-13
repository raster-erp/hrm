package com.raster.hrm.tds.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvestmentDeclarationResponse(
        Long id, Long employeeId, String employeeName,
        String financialYear, String regime,
        BigDecimal totalDeclaredAmount, BigDecimal totalVerifiedAmount,
        String status, String remarks,
        LocalDateTime submittedAt, LocalDateTime verifiedAt, Long verifiedBy,
        List<InvestmentDeclarationItemResponse> items,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {}
