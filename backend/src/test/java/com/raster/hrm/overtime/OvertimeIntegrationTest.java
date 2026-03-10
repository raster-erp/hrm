package com.raster.hrm.overtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.overtime.dto.OvertimeApprovalRequest;
import com.raster.hrm.overtime.dto.OvertimePolicyRequest;
import com.raster.hrm.overtime.dto.OvertimeRecordRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/overtime/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/overtime/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class OvertimeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String POLICIES_URL = "/api/v1/overtime-policies";
    private static final String RECORDS_URL = "/api/v1/overtime-records";

    @Test
    void shouldCreateAndRetrieveOvertimePolicy() throws Exception {
        var request = new OvertimePolicyRequest(
                "Holiday OT", "HOLIDAY", new BigDecimal("2.50"),
                15, 180, 600, true, "Holiday overtime");

        var result = mockMvc.perform(post(POLICIES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Holiday OT"))
                .andExpect(jsonPath("$.type").value("HOLIDAY"))
                .andExpect(jsonPath("$.rateMultiplier").value(2.50))
                .andReturn();

        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get(POLICIES_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Holiday OT"))
                .andExpect(jsonPath("$.requiresApproval").value(true));
    }

    @Test
    void shouldRejectDuplicatePolicyName() throws Exception {
        var request = new OvertimePolicyRequest(
                "Weekday OT", "WEEKDAY", new BigDecimal("1.50"),
                0, null, null, true, null);

        mockMvc.perform(post(POLICIES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetActivePolicies() throws Exception {
        mockMvc.perform(get(POLICIES_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetPoliciesByType() throws Exception {
        mockMvc.perform(get(POLICIES_URL + "/type/WEEKDAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Weekday OT"));
    }

    @Test
    void shouldCreateAndRetrieveOvertimeRecord() throws Exception {
        var request = new OvertimeRecordRequest(
                9001L, LocalDate.of(2025, 3, 5), 9001L, 90,
                LocalTime.of(9, 0), LocalTime.of(17, 0), null, null, "Manual request");

        var result = mockMvc.perform(post(RECORDS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.overtimePolicyName").value("Weekday OT"))
                .andExpect(jsonPath("$.overtimeMinutes").value(90))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.source").value("MANUAL"))
                .andReturn();

        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get(RECORDS_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCode").value("EMP-OT-001"));
    }

    @Test
    void shouldApproveOvertimeRecord() throws Exception {
        var createRequest = new OvertimeRecordRequest(
                9001L, LocalDate.of(2025, 3, 5), 9001L, 60,
                null, null, null, null, null);
        var createResult = mockMvc.perform(post(RECORDS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var approvalRequest = new OvertimeApprovalRequest("APPROVED", "admin", "Approved by manager");
        mockMvc.perform(patch(RECORDS_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").value("admin"));
    }

    @Test
    void shouldRejectOvertimeRecord() throws Exception {
        var createRequest = new OvertimeRecordRequest(
                9001L, LocalDate.of(2025, 3, 5), 9001L, 60,
                null, null, null, null, null);
        var createResult = mockMvc.perform(post(RECORDS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var approvalRequest = new OvertimeApprovalRequest("REJECTED", "admin", "Not justified");
        mockMvc.perform(patch(RECORDS_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void shouldNotApproveAlreadyApprovedRecord() throws Exception {
        var createRequest = new OvertimeRecordRequest(
                9001L, LocalDate.of(2025, 3, 5), 9001L, 60,
                null, null, null, null, null);
        var createResult = mockMvc.perform(post(RECORDS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var approvalRequest = new OvertimeApprovalRequest("APPROVED", "admin", null);
        mockMvc.perform(patch(RECORDS_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(patch(RECORDS_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAutoDetectOvertimeFromPunches() throws Exception {
        // Employee 9001 has punches on 2025-03-10: IN at 08:55, OUT at 19:30
        // Shift is 09:00-17:00 with 60 min break = 7 hrs expected
        // Actual: 08:55-19:30 = 10h35m - 60m break = 9h35m = 575 min
        // Expected: 8h - 60m break = 7h = 420 min
        // Overtime: 575 - 420 = 155 min (above 30 min threshold)
        mockMvc.perform(post(RECORDS_URL + "/detect")
                        .param("employeeId", "9001")
                        .param("date", "2025-03-10")
                        .param("policyId", "9001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].source").value("AUTO_DETECTED"))
                .andExpect(jsonPath("$[0].employeeCode").value("EMP-OT-001"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void shouldNotDuplicateAutoDetection() throws Exception {
        mockMvc.perform(post(RECORDS_URL + "/detect")
                        .param("employeeId", "9001")
                        .param("date", "2025-03-10")
                        .param("policyId", "9001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1));

        // Second detection should return empty (already exists)
        mockMvc.perform(post(RECORDS_URL + "/detect")
                        .param("employeeId", "9001")
                        .param("date", "2025-03-10")
                        .param("policyId", "9001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetOvertimeSummary() throws Exception {
        // Create and approve a record for employee 9001
        var request1 = new OvertimeRecordRequest(
                9001L, LocalDate.of(2025, 3, 1), 9001L, 60,
                null, null, null, null, null);
        var result1 = mockMvc.perform(post(RECORDS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andReturn();
        var id1 = objectMapper.readTree(result1.getResponse().getContentAsString()).get("id").asLong();

        var approvalRequest = new OvertimeApprovalRequest("APPROVED", "admin", null);
        mockMvc.perform(patch(RECORDS_URL + "/" + id1 + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk());

        // Create a pending record for employee 9001
        var request2 = new OvertimeRecordRequest(
                9001L, LocalDate.of(2025, 3, 2), 9001L, 45,
                null, null, null, null, null);
        mockMvc.perform(post(RECORDS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(RECORDS_URL + "/summary")
                        .param("startDate", "2025-03-01")
                        .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeCode").value("EMP-OT-001"))
                .andExpect(jsonPath("$[0].totalOvertimeMinutes").value(105))
                .andExpect(jsonPath("$[0].approvedOvertimeMinutes").value(60))
                .andExpect(jsonPath("$[0].pendingOvertimeMinutes").value(45))
                .andExpect(jsonPath("$[0].recordCount").value(2));
    }

    @Test
    void shouldDeleteOvertimeRecord() throws Exception {
        var request = new OvertimeRecordRequest(
                9001L, LocalDate.of(2025, 3, 5), 9001L, 60,
                null, null, null, null, null);
        var result = mockMvc.perform(post(RECORDS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete(RECORDS_URL + "/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(RECORDS_URL + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetRecordsByDateRange() throws Exception {
        var request = new OvertimeRecordRequest(
                9001L, LocalDate.of(2025, 3, 15), 9001L, 60,
                null, null, null, null, null);
        mockMvc.perform(post(RECORDS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(RECORDS_URL + "/date-range")
                        .param("startDate", "2025-03-01")
                        .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
