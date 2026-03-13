package com.raster.hrm.tds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.tds.controller.TaxComputationController;
import com.raster.hrm.tds.dto.Form16DataResponse;
import com.raster.hrm.tds.dto.InvestmentDeclarationItemResponse;
import com.raster.hrm.tds.dto.TaxComputationRequest;
import com.raster.hrm.tds.dto.TaxComputationResponse;
import com.raster.hrm.tds.service.TaxComputationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaxComputationController.class)
class TaxComputationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaxComputationService taxComputationService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/tax-computations";

    private TaxComputationResponse createResponse() {
        return new TaxComputationResponse(
                1L, 100L, "John Doe", "2025-26", 4,
                new BigDecimal("1200000"), new BigDecimal("150000"),
                new BigDecimal("1000000"), new BigDecimal("75000"),
                new BigDecimal("6250"), new BigDecimal("3000"), BigDecimal.ZERO,
                new BigDecimal("25000"), new BigDecimal("50000"),
                "NEW",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0));
    }

    private TaxComputationRequest createRequest() {
        return new TaxComputationRequest(100L, "2025-26", 4);
    }

    @Test
    void compute_shouldReturnComputation() throws Exception {
        var request = createRequest();
        var response = createResponse();
        when(taxComputationService.computeMonthlyTds(any(TaxComputationRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/compute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(100))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.month").value(4))
                .andExpect(jsonPath("$.regime").value("NEW"));
    }

    @Test
    void getByEmployeeAndYear_shouldReturnList() throws Exception {
        var responses = List.of(createResponse());
        when(taxComputationService.getByEmployeeAndYear(100L, "2025-26")).thenReturn(responses);

        mockMvc.perform(get(BASE_URL + "/employee/100/year/2025-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeId").value(100))
                .andExpect(jsonPath("$[0].financialYear").value("2025-26"));
    }

    @Test
    void getByEmployeeYearMonth_shouldReturn() throws Exception {
        var response = createResponse();
        when(taxComputationService.getByEmployeeYearMonth(100L, "2025-26", 4)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/employee/100/year/2025-26/month/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(100))
                .andExpect(jsonPath("$.month").value(4));
    }

    @Test
    void getByEmployeeYearMonth_shouldReturn404() throws Exception {
        when(taxComputationService.getByEmployeeYearMonth(100L, "2025-26", 4))
                .thenThrow(new ResourceNotFoundException("TaxComputation", "employeeId/year/month", "100/2025-26/4"));

        mockMvc.perform(get(BASE_URL + "/employee/100/year/2025-26/month/4"))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateForm16_shouldReturn() throws Exception {
        var items = List.of(new InvestmentDeclarationItemResponse(
                1L, "80C", "PPF", new BigDecimal("150000"), new BigDecimal("150000"),
                "VERIFIED", "proof.pdf", null,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)));
        var monthlyBreakup = List.of(createResponse());
        var form16 = new Form16DataResponse(
                100L, "John Doe", "2025-26", "NEW",
                new BigDecimal("1200000"), new BigDecimal("150000"),
                new BigDecimal("1000000"), new BigDecimal("75000"),
                new BigDecimal("75000"), new BigDecimal("3000"), BigDecimal.ZERO,
                items, monthlyBreakup);
        when(taxComputationService.generateForm16Data(100L, "2025-26")).thenReturn(form16);

        mockMvc.perform(get(BASE_URL + "/form16/employee/100/year/2025-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(100))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.financialYear").value("2025-26"))
                .andExpect(jsonPath("$.regime").value("NEW"))
                .andExpect(jsonPath("$.verifiedInvestments.length()").value(1))
                .andExpect(jsonPath("$.monthlyBreakup.length()").value(1));
    }
}
