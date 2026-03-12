package com.raster.hrm.compoff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.compoff.controller.CompOffCreditController;
import com.raster.hrm.compoff.dto.CompOffApprovalRequest;
import com.raster.hrm.compoff.dto.CompOffBalanceResponse;
import com.raster.hrm.compoff.dto.CompOffCreditRequest;
import com.raster.hrm.compoff.dto.CompOffCreditResponse;
import com.raster.hrm.compoff.entity.CompOffStatus;
import com.raster.hrm.compoff.service.CompOffCreditService;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompOffCreditController.class)
class CompOffCreditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompOffCreditService compOffCreditService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/comp-off-credits";

    private CompOffCreditResponse createResponse() {
        return new CompOffCreditResponse(
                1L, 1L, "EMP001", "John Doe",
                LocalDate.of(2025, 6, 15),
                "Worked on weekend for release",
                LocalDate.of(2025, 6, 16),
                LocalDate.of(2025, 9, 13),
                new BigDecimal("8.00"),
                "PENDING",
                null, null, null,
                "Test comp-off",
                LocalDateTime.of(2025, 6, 16, 10, 0),
                LocalDateTime.of(2025, 6, 16, 10, 0)
        );
    }

    @Test
    void createRequest_shouldReturn201() throws Exception {
        var request = new CompOffCreditRequest(
                1L, LocalDate.of(2025, 6, 15), "Worked on weekend", new BigDecimal("8.00"), "Test");
        when(compOffCreditService.createRequest(any(CompOffCreditRequest.class)))
                .thenReturn(createResponse());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.workedDate").value("2025-06-15"));
    }

    @Test
    void createRequest_shouldReturn400_whenInvalid() throws Exception {
        var request = new CompOffCreditRequest(null, null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        var page = new PageImpl<>(List.of(createResponse()));
        when(compOffCreditService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(compOffCreditService.getById(1L)).thenReturn(createResponse());

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(compOffCreditService.getById(999L))
                .thenThrow(new ResourceNotFoundException("CompOffCredit", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturn200() throws Exception {
        var page = new PageImpl<>(List.of(createResponse()));
        when(compOffCreditService.getByEmployee(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getByStatus_shouldReturn200() throws Exception {
        var page = new PageImpl<>(List.of(createResponse()));
        when(compOffCreditService.getByStatus(eq(CompOffStatus.PENDING), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getBalance_shouldReturn200() throws Exception {
        var balance = new CompOffBalanceResponse(1L, "John Doe", 5, 3, 1, 1, 0, 3);
        when(compOffCreditService.getBalance(1L)).thenReturn(balance);

        mockMvc.perform(get(BASE_URL + "/balance/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCredits").value(5))
                .andExpect(jsonPath("$.approved").value(3))
                .andExpect(jsonPath("$.availableForUse").value(3));
    }

    @Test
    void approve_shouldReturn200() throws Exception {
        var request = new CompOffApprovalRequest(CompOffStatus.APPROVED, "Admin", "Approved");
        var response = createResponse();
        when(compOffCreditService.approve(eq(1L), any(CompOffApprovalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void expireCredits_shouldReturn200() throws Exception {
        when(compOffCreditService.expireCredits()).thenReturn(3);

        mockMvc.perform(post(BASE_URL + "/expire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));
    }
}
