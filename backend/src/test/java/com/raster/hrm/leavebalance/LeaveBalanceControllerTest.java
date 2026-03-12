package com.raster.hrm.leavebalance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.leavebalance.controller.LeaveBalanceController;
import com.raster.hrm.leavebalance.dto.BalanceAdjustmentRequest;
import com.raster.hrm.leavebalance.dto.LeaveBalanceResponse;
import com.raster.hrm.leavebalance.dto.LeaveTransactionResponse;
import com.raster.hrm.leavebalance.dto.YearEndProcessingRequest;
import com.raster.hrm.leavebalance.dto.YearEndSummaryResponse;
import com.raster.hrm.leavebalance.entity.TransactionType;
import com.raster.hrm.leavebalance.service.LeaveBalanceService;
import com.raster.hrm.exception.ResourceNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaveBalanceController.class)
class LeaveBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/leave-balances";

    private LeaveBalanceResponse createBalanceResponse() {
        return new LeaveBalanceResponse(
                1L, 1L, "EMP001", "John Doe",
                1L, "Casual Leave", "CL",
                2025,
                new BigDecimal("12.00"),
                new BigDecimal("3.00"),
                new BigDecimal("2.00"),
                new BigDecimal("7.00"),
                BigDecimal.ZERO,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 1, 0, 0)
        );
    }

    private LeaveTransactionResponse createTransactionResponse() {
        return new LeaveTransactionResponse(
                1L, 1L, "EMP001", "John Doe",
                1L, "Casual Leave",
                "CREDIT",
                new BigDecimal("12.00"),
                new BigDecimal("12.00"),
                "MANUAL", null,
                "Initial credit",
                "Admin",
                LocalDateTime.of(2025, 1, 1, 0, 0)
        );
    }

    @Test
    void getBalancesByEmployee_shouldReturn200() throws Exception {
        var response = createBalanceResponse();
        when(leaveBalanceService.getBalancesByEmployee(1L, 2025)).thenReturn(List.of(response));

        mockMvc.perform(get(BASE_URL + "/employee/1").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$[0].leaveTypeName").value("Casual Leave"))
                .andExpect(jsonPath("$[0].credited").value(12.00))
                .andExpect(jsonPath("$[0].available").value(7.00));
    }

    @Test
    void getBalancesByEmployee_shouldReturn404WhenNotFound() throws Exception {
        when(leaveBalanceService.getBalancesByEmployee(999L, 2025))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/employee/999").param("year", "2025"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBalance_shouldReturn200() throws Exception {
        var response = createBalanceResponse();
        when(leaveBalanceService.getBalance(1L, 1L, 2025)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/employee/1/leave-type/1").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.year").value(2025));
    }

    @Test
    void getBalance_shouldReturn404WhenNotFound() throws Exception {
        when(leaveBalanceService.getBalance(1L, 1L, 2025))
                .thenThrow(new ResourceNotFoundException("LeaveBalance", "employeeId/leaveTypeId/year", "1/1/2025"));

        mockMvc.perform(get(BASE_URL + "/employee/1/leave-type/1").param("year", "2025"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactions_shouldReturn200() throws Exception {
        var response = createTransactionResponse();
        var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(leaveBalanceService.getTransactions(eq(1L), isNull(), isNull(), any()))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].transactionType").value("CREDIT"));
    }

    @Test
    void getTransactions_shouldFilterByLeaveType() throws Exception {
        var response = createTransactionResponse();
        var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(leaveBalanceService.getTransactions(eq(1L), eq(1L), isNull(), any()))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1/transactions").param("leaveTypeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getTransactions_shouldFilterByTransactionType() throws Exception {
        var response = createTransactionResponse();
        var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(leaveBalanceService.getTransactions(eq(1L), isNull(), eq(TransactionType.CREDIT), any()))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1/transactions").param("transactionType", "CREDIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void adjustBalance_shouldReturn200() throws Exception {
        var request = new BalanceAdjustmentRequest(1L, 1L, 2025, new BigDecimal("5.00"), "Bonus leave", "Admin");
        var response = new LeaveBalanceResponse(
                1L, 1L, "EMP001", "John Doe",
                1L, "Casual Leave", "CL",
                2025,
                new BigDecimal("17.00"),
                new BigDecimal("3.00"),
                new BigDecimal("2.00"),
                new BigDecimal("12.00"),
                BigDecimal.ZERO,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 1, 0, 0)
        );
        when(leaveBalanceService.adjustBalance(any(BalanceAdjustmentRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credited").value(17.00))
                .andExpect(jsonPath("$.available").value(12.00));
    }

    @Test
    void adjustBalance_shouldReturn400WhenInvalid() throws Exception {
        var request = new BalanceAdjustmentRequest(null, null, null, null, null, null);

        mockMvc.perform(post(BASE_URL + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processYearEnd_shouldReturn200() throws Exception {
        var request = new YearEndProcessingRequest(2025, "Admin");
        var response = new YearEndSummaryResponse(
                2025, 2026, 10, 8,
                new BigDecimal("40.00"), new BigDecimal("15.00")
        );
        when(leaveBalanceService.processYearEnd(any(YearEndProcessingRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/year-end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedYear").value(2025))
                .andExpect(jsonPath("$.nextYear").value(2026))
                .andExpect(jsonPath("$.employeesProcessed").value(10))
                .andExpect(jsonPath("$.balancesCreated").value(8))
                .andExpect(jsonPath("$.totalCarryForwarded").value(40.00))
                .andExpect(jsonPath("$.totalLapsed").value(15.00));
    }

    @Test
    void processYearEnd_shouldReturn400WhenYearNull() throws Exception {
        var request = new YearEndProcessingRequest(null, null);

        mockMvc.perform(post(BASE_URL + "/year-end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
