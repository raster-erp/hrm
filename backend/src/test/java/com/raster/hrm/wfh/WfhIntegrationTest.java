package com.raster.hrm.wfh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.wfh.dto.WfhApprovalRequest;
import com.raster.hrm.wfh.dto.WfhRequestCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

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
@Sql(scripts = "/wfh/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/wfh/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class WfhIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String REQUESTS_URL = "/api/v1/wfh-requests";
    private static final String ACTIVITY_URL = "/api/v1/wfh-activity-logs";

    @Test
    void shouldCreateAndRetrieveWfhRequest() throws Exception {
        var request = new WfhRequestCreateRequest(
                9501L, LocalDate.of(2025, 6, 15), "Need to work from home", "Personal reasons");

        var result = mockMvc.perform(post(REQUESTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeName").value("Alice WFH"))
                .andExpect(jsonPath("$.reason").value("Need to work from home"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get(REQUESTS_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCode").value("EMP-WFH-001"));
    }

    @Test
    void shouldApproveWfhRequest() throws Exception {
        var createRequest = new WfhRequestCreateRequest(
                9501L, LocalDate.of(2025, 6, 15), "WFH request", null);
        var createResult = mockMvc.perform(post(REQUESTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var approvalRequest = new WfhApprovalRequest("APPROVED", "manager", "Approved by manager");
        mockMvc.perform(patch(REQUESTS_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").value("manager"));
    }

    @Test
    void shouldRejectWfhRequest() throws Exception {
        var createRequest = new WfhRequestCreateRequest(
                9501L, LocalDate.of(2025, 6, 15), "WFH request", null);
        var createResult = mockMvc.perform(post(REQUESTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var approvalRequest = new WfhApprovalRequest("REJECTED", "manager", "Not justified");
        mockMvc.perform(patch(REQUESTS_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void shouldNotApproveAlreadyApprovedRequest() throws Exception {
        var createRequest = new WfhRequestCreateRequest(
                9501L, LocalDate.of(2025, 6, 15), "WFH request", null);
        var createResult = mockMvc.perform(post(REQUESTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var approvalRequest = new WfhApprovalRequest("APPROVED", "manager", null);
        mockMvc.perform(patch(REQUESTS_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk());

        // Second approval should fail
        mockMvc.perform(patch(REQUESTS_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdatePendingWfhRequest() throws Exception {
        var createRequest = new WfhRequestCreateRequest(
                9501L, LocalDate.of(2025, 6, 15), "Original reason", null);
        var createResult = mockMvc.perform(post(REQUESTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var updateRequest = new WfhRequestCreateRequest(
                9501L, LocalDate.of(2025, 6, 20), "Updated reason", "Updated remarks");
        mockMvc.perform(put(REQUESTS_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Updated reason"))
                .andExpect(jsonPath("$.requestDate").value("2025-06-20"));
    }

    @Test
    void shouldNotUpdateApprovedWfhRequest() throws Exception {
        var createRequest = new WfhRequestCreateRequest(
                9501L, LocalDate.of(2025, 6, 15), "WFH request", null);
        var createResult = mockMvc.perform(post(REQUESTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var approvalRequest = new WfhApprovalRequest("APPROVED", "manager", null);
        mockMvc.perform(patch(REQUESTS_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk());

        var updateRequest = new WfhRequestCreateRequest(
                9501L, LocalDate.of(2025, 6, 20), "Updated reason", null);
        mockMvc.perform(put(REQUESTS_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteWfhRequest() throws Exception {
        var request = new WfhRequestCreateRequest(
                9501L, LocalDate.of(2025, 6, 15), "WFH request", null);
        var result = mockMvc.perform(post(REQUESTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete(REQUESTS_URL + "/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(REQUESTS_URL + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetRequestsByEmployee() throws Exception {
        var request = new WfhRequestCreateRequest(
                9501L, LocalDate.of(2025, 6, 15), "WFH request", null);
        mockMvc.perform(post(REQUESTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(REQUESTS_URL + "/employee/9501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].employeeCode").value("EMP-WFH-001"));
    }

    @Test
    void shouldGetRequestsByDateRange() throws Exception {
        var request = new WfhRequestCreateRequest(
                9501L, LocalDate.of(2025, 6, 15), "WFH request", null);
        mockMvc.perform(post(REQUESTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(REQUESTS_URL + "/date-range")
                        .param("startDate", "2025-06-01")
                        .param("endDate", "2025-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldGetDashboard() throws Exception {
        // Create multiple requests for dashboard
        mockMvc.perform(post(REQUESTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new WfhRequestCreateRequest(9501L, LocalDate.of(2025, 6, 10), "WFH 1", null))))
                .andExpect(status().isCreated());

        var result = mockMvc.perform(post(REQUESTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new WfhRequestCreateRequest(9501L, LocalDate.of(2025, 6, 15), "WFH 2", null))))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        var approvalRequest = new WfhApprovalRequest("APPROVED", "manager", null);
        mockMvc.perform(patch(REQUESTS_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get(REQUESTS_URL + "/dashboard")
                        .param("startDate", "2025-06-01")
                        .param("endDate", "2025-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeCode").value("EMP-WFH-001"))
                .andExpect(jsonPath("$[0].totalRequests").value(2))
                .andExpect(jsonPath("$[0].approvedRequests").value(1))
                .andExpect(jsonPath("$[0].pendingRequests").value(1));
    }

    @Test
    void shouldReturn404ForNonExistentRequest() throws Exception {
        mockMvc.perform(get(REQUESTS_URL + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400ForInvalidCreateRequest() throws Exception {
        var invalidRequest = new WfhRequestCreateRequest(null, null, "", null);

        mockMvc.perform(post(REQUESTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
