package com.raster.hrm.leavebalance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.leavebalance.dto.BalanceAdjustmentRequest;
import com.raster.hrm.leavebalance.dto.YearEndProcessingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/leavebalance/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/leavebalance/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class LeaveBalanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/leave-balances";

    @Test
    void shouldGetBalancesByEmployee() throws Exception {
        mockMvc.perform(get(BASE_URL + "/employee/9001").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$[0].leaveTypeName").value("Casual Leave"))
                .andExpect(jsonPath("$[0].credited").value(12.00))
                .andExpect(jsonPath("$[0].available").value(7.00));
    }

    @Test
    void shouldGetBalanceByEmployeeAndLeaveType() throws Exception {
        mockMvc.perform(get(BASE_URL + "/employee/9001/leave-type/9001").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.leaveTypeName").value("Casual Leave"))
                .andExpect(jsonPath("$.credited").value(12.00))
                .andExpect(jsonPath("$.used").value(3.00))
                .andExpect(jsonPath("$.pending").value(2.00))
                .andExpect(jsonPath("$.available").value(7.00));
    }

    @Test
    void shouldReturn404ForNonExistentBalance() throws Exception {
        mockMvc.perform(get(BASE_URL + "/employee/9001/leave-type/9001").param("year", "2020"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404ForNonExistentEmployee() throws Exception {
        mockMvc.perform(get(BASE_URL + "/employee/99999").param("year", "2025"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAdjustBalance() throws Exception {
        var request = new BalanceAdjustmentRequest(9001L, 9001L, 2025, new BigDecimal("3.00"), "Bonus leave", "HR Admin");

        mockMvc.perform(post(BASE_URL + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credited").value(15.00))
                .andExpect(jsonPath("$.available").value(10.00));

        // Verify the balance was updated
        mockMvc.perform(get(BASE_URL + "/employee/9001/leave-type/9001").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credited").value(15.00))
                .andExpect(jsonPath("$.available").value(10.00));
    }

    @Test
    void shouldAdjustBalanceWithNegativeAmount() throws Exception {
        var request = new BalanceAdjustmentRequest(9001L, 9001L, 2025, new BigDecimal("-2.00"), "Correction", "HR Admin");

        mockMvc.perform(post(BASE_URL + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credited").value(10.00))
                .andExpect(jsonPath("$.available").value(5.00));
    }

    @Test
    void shouldCreateBalanceOnAdjustmentIfNotExists() throws Exception {
        var request = new BalanceAdjustmentRequest(9001L, 9001L, 2026, new BigDecimal("10.00"), "New year credit", "HR Admin");

        mockMvc.perform(post(BASE_URL + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.credited").value(10.00))
                .andExpect(jsonPath("$.available").value(10.00));
    }

    @Test
    void shouldGetTransactionHistory() throws Exception {
        // First create a transaction via adjustment
        var request = new BalanceAdjustmentRequest(9001L, 9001L, 2025, new BigDecimal("5.00"), "Test adjustment", "Admin");
        mockMvc.perform(post(BASE_URL + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then fetch transactions
        mockMvc.perform(get(BASE_URL + "/employee/9001/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].transactionType").value("ADJUSTMENT"))
                .andExpect(jsonPath("$.content[0].amount").value(5.00));
    }

    @Test
    void shouldFilterTransactionsByLeaveType() throws Exception {
        // Create transactions for different types
        var req1 = new BalanceAdjustmentRequest(9001L, 9001L, 2025, new BigDecimal("5.00"), "CL adjustment", "Admin");
        var req2 = new BalanceAdjustmentRequest(9001L, 9002L, 2025, new BigDecimal("3.00"), "SL adjustment", "Admin");

        mockMvc.perform(post(BASE_URL + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk());
        mockMvc.perform(post(BASE_URL + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isOk());

        // Filter by leave type
        mockMvc.perform(get(BASE_URL + "/employee/9001/transactions").param("leaveTypeId", "9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].leaveTypeName").value("Casual Leave"));
    }

    @Test
    void shouldProcessYearEnd() throws Exception {
        var request = new YearEndProcessingRequest(2025, "Admin");

        mockMvc.perform(post(BASE_URL + "/year-end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedYear").value(2025))
                .andExpect(jsonPath("$.nextYear").value(2026))
                .andExpect(jsonPath("$.employeesProcessed").value(2));
    }

    @Test
    void shouldValidateAdjustmentRequest() throws Exception {
        var request = new BalanceAdjustmentRequest(null, null, null, null, null, null);

        mockMvc.perform(post(BASE_URL + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateYearEndRequest() throws Exception {
        var request = new YearEndProcessingRequest(null, null);

        mockMvc.perform(post(BASE_URL + "/year-end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldIntegrateWithLeaveApplicationFlow() throws Exception {
        int currentYear = java.time.LocalDate.now().getYear();

        // Create a balance for the current year to be used by the application flow
        var setupRequest = new BalanceAdjustmentRequest(9001L, 9001L, currentYear, new BigDecimal("12.00"), "Annual credit", "System");
        mockMvc.perform(post(BASE_URL + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setupRequest)))
                .andExpect(status().isOk());

        // Create a leave application
        var leaveAppRequest = """
                {
                    "employeeId": 9001,
                    "leaveTypeId": 9001,
                    "fromDate": "%s-06-01",
                    "toDate": "%s-06-03",
                    "numberOfDays": 3.00,
                    "reason": "Integration test"
                }
                """.formatted(currentYear, currentYear);

        mockMvc.perform(post("/api/v1/leave-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leaveAppRequest))
                .andExpect(status().isCreated());

        // Verify pending balance increased
        mockMvc.perform(get(BASE_URL + "/employee/9001/leave-type/9001").param("year", String.valueOf(currentYear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").value(3.00));

        // Verify transactions recorded
        mockMvc.perform(get(BASE_URL + "/employee/9001/transactions").param("transactionType", "PENDING_DEBIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
