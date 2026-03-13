package com.raster.hrm.tds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.tds.controller.InvestmentDeclarationController;
import com.raster.hrm.tds.dto.InvestmentDeclarationItemRequest;
import com.raster.hrm.tds.dto.InvestmentDeclarationItemResponse;
import com.raster.hrm.tds.dto.InvestmentDeclarationRequest;
import com.raster.hrm.tds.dto.InvestmentDeclarationResponse;
import com.raster.hrm.tds.dto.ProofSubmissionRequest;
import com.raster.hrm.tds.dto.ProofVerificationRequest;
import com.raster.hrm.tds.service.InvestmentDeclarationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvestmentDeclarationController.class)
class InvestmentDeclarationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvestmentDeclarationService investmentDeclarationService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/investment-declarations";

    private InvestmentDeclarationResponse createResponse() {
        var items = List.of(new InvestmentDeclarationItemResponse(
                1L, "80C", "PPF", new BigDecimal("150000"), new BigDecimal("0"),
                "PENDING", null, null,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)));
        return new InvestmentDeclarationResponse(
                1L, 100L, "John Doe", "2025-26", "NEW",
                new BigDecimal("150000"), new BigDecimal("0"),
                "DRAFT", null, null, null, null, items,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0));
    }

    private InvestmentDeclarationRequest createRequest() {
        var items = List.of(new InvestmentDeclarationItemRequest(
                "80C", "PPF", new BigDecimal("150000")));
        return new InvestmentDeclarationRequest(100L, "2025-26", "NEW", null, items);
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        var declarations = List.of(createResponse());
        var page = new PageImpl<>(declarations, PageRequest.of(0, 20), 1);
        when(investmentDeclarationService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getById_shouldReturnDeclaration() throws Exception {
        var response = createResponse();
        when(investmentDeclarationService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(100))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.financialYear").value("2025-26"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void getById_shouldReturn404() throws Exception {
        when(investmentDeclarationService.getById(999L))
                .thenThrow(new ResourceNotFoundException("InvestmentDeclaration", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployeeAndYear_shouldReturn() throws Exception {
        var response = createResponse();
        when(investmentDeclarationService.getByEmployeeAndYear(100L, "2025-26")).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/employee/100/year/2025-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(100))
                .andExpect(jsonPath("$.financialYear").value("2025-26"));
    }

    @Test
    void getByFinancialYear_shouldReturn() throws Exception {
        var declarations = List.of(createResponse());
        var page = new PageImpl<>(declarations, PageRequest.of(0, 20), 1);
        when(investmentDeclarationService.getByFinancialYear(eq("2025-26"), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/year/2025-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].financialYear").value("2025-26"));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var request = createRequest();
        var response = createResponse();
        when(investmentDeclarationService.create(any(InvestmentDeclarationRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void create_shouldReturn400WhenDuplicate() throws Exception {
        var request = createRequest();
        when(investmentDeclarationService.create(any(InvestmentDeclarationRequest.class)))
                .thenThrow(new BadRequestException("Declaration already exists for this employee and year"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var request = createRequest();
        var response = createResponse();
        when(investmentDeclarationService.update(eq(1L), any(InvestmentDeclarationRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void update_shouldReturn404() throws Exception {
        var request = createRequest();
        when(investmentDeclarationService.update(eq(999L), any(InvestmentDeclarationRequest.class)))
                .thenThrow(new ResourceNotFoundException("InvestmentDeclaration", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void submit_shouldReturn200() throws Exception {
        var response = createResponse();
        when(investmentDeclarationService.submit(1L)).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void verify_shouldReturn200() throws Exception {
        var response = createResponse();
        when(investmentDeclarationService.verify(eq(1L), eq(200L))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("verifiedBy", 200L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void reject_shouldReturn200() throws Exception {
        var response = createResponse();
        when(investmentDeclarationService.reject(eq(1L), eq("Insufficient proof"))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("remarks", "Insufficient proof"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void submitProof_shouldReturn200() throws Exception {
        var request = new ProofSubmissionRequest(1L, "proof.pdf", new BigDecimal("150000"));
        doNothing().when(investmentDeclarationService).submitProof(any(ProofSubmissionRequest.class));

        mockMvc.perform(post(BASE_URL + "/proof/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(investmentDeclarationService).submitProof(any(ProofSubmissionRequest.class));
    }

    @Test
    void verifyProof_shouldReturn200() throws Exception {
        var request = new ProofVerificationRequest(1L, new BigDecimal("140000"), "VERIFIED", "Looks good");
        doNothing().when(investmentDeclarationService).verifyProof(any(ProofVerificationRequest.class));

        mockMvc.perform(post(BASE_URL + "/proof/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(investmentDeclarationService).verifyProof(any(ProofVerificationRequest.class));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(investmentDeclarationService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(investmentDeclarationService).delete(1L);
    }
}
