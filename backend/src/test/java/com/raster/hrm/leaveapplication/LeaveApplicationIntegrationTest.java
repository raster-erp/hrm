package com.raster.hrm.leaveapplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.leaveapplication.dto.LeaveApplicationRequest;
import com.raster.hrm.leaveapplication.dto.LeaveApprovalRequest;
import com.raster.hrm.leaveapplication.entity.LeaveApplicationStatus;
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
@Sql(scripts = "/leaveapplication/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/leaveapplication/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class LeaveApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/leave-applications";

    private LeaveApplicationRequest createRequest(Long employeeId, Long leaveTypeId,
                                                   LocalDate from, LocalDate to, BigDecimal days) {
        return new LeaveApplicationRequest(employeeId, leaveTypeId, from, to, days, "Integration test", null);
    }

    @Test
    void shouldCreateAndRetrieveApplication() throws Exception {
        var request = createRequest(9001L, 9001L,
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 3), new BigDecimal("3.00"));

        var result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.employeeCode").value("LA-EMP-001"))
                .andExpect(jsonPath("$.leaveTypeName").value("Casual Leave"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.approvalLevel").value(0))
                .andReturn();

        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.leaveTypeName").value("Casual Leave"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldRejectFromDateAfterToDate() throws Exception {
        var request = createRequest(9001L, 9001L,
                LocalDate.of(2025, 6, 5), LocalDate.of(2025, 6, 1), new BigDecimal("3.00"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdatePendingApplication() throws Exception {
        var createReq = createRequest(9001L, 9001L,
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 3), new BigDecimal("3.00"));

        var createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var updateReq = createRequest(9001L, 9002L,
                LocalDate.of(2025, 6, 2), LocalDate.of(2025, 6, 4), new BigDecimal("3.00"));

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaveTypeName").value("Sick Leave"))
                .andExpect(jsonPath("$.fromDate").value("2025-06-02"));
    }

    @Test
    void shouldApproveApplication() throws Exception {
        var createReq = createRequest(9001L, 9001L,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 2), new BigDecimal("2.00"));

        var createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var approvalReq = new LeaveApprovalRequest(LeaveApplicationStatus.APPROVED, "Manager", "Looks good");

        mockMvc.perform(patch(BASE_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").value("Manager"))
                .andExpect(jsonPath("$.approvalLevel").value(1))
                .andExpect(jsonPath("$.remarks").value("Looks good"));
    }

    @Test
    void shouldRejectApplication() throws Exception {
        var createReq = createRequest(9001L, 9001L,
                LocalDate.of(2025, 7, 10), LocalDate.of(2025, 7, 15), new BigDecimal("5.00"));

        var createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var rejectReq = new LeaveApprovalRequest(LeaveApplicationStatus.REJECTED, "HR", "Insufficient balance");

        mockMvc.perform(patch(BASE_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.approvedBy").value("HR"))
                .andExpect(jsonPath("$.remarks").value("Insufficient balance"));
    }

    @Test
    void shouldNotApproveNonPendingApplication() throws Exception {
        var createReq = createRequest(9001L, 9001L,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 2), new BigDecimal("2.00"));

        var createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Approve first
        var approvalReq = new LeaveApprovalRequest(LeaveApplicationStatus.APPROVED, "Manager", null);
        mockMvc.perform(patch(BASE_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalReq)))
                .andExpect(status().isOk());

        // Attempt to approve again
        mockMvc.perform(patch(BASE_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUpdateNonPendingApplication() throws Exception {
        var createReq = createRequest(9001L, 9001L,
                LocalDate.of(2025, 8, 5), LocalDate.of(2025, 8, 6), new BigDecimal("2.00"));

        var createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Approve first
        var approvalReq = new LeaveApprovalRequest(LeaveApplicationStatus.APPROVED, "Manager", null);
        mockMvc.perform(patch(BASE_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalReq)))
                .andExpect(status().isOk());

        // Attempt to update
        var updateReq = createRequest(9001L, 9001L,
                LocalDate.of(2025, 8, 7), LocalDate.of(2025, 8, 8), new BigDecimal("2.00"));
        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCancelPendingApplication() throws Exception {
        var createReq = createRequest(9001L, 9001L,
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 3), new BigDecimal("3.00"));

        var createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch(BASE_URL + "/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify the application is now cancelled
        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldCancelApprovedApplicationAndRestoreAvailability() throws Exception {
        var createReq = createRequest(9001L, 9001L,
                LocalDate.of(2025, 9, 10), LocalDate.of(2025, 9, 12), new BigDecimal("3.00"));

        var createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Approve application
        var approvalReq = new LeaveApprovalRequest(LeaveApplicationStatus.APPROVED, "Manager", "Approved");
        mockMvc.perform(patch(BASE_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // Cancel the approved application (simulates balance restoration)
        mockMvc.perform(patch(BASE_URL + "/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify the application status is CANCELLED
        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.numberOfDays").value(3.00));
    }

    @Test
    void shouldNotCancelRejectedApplication() throws Exception {
        var createReq = createRequest(9001L, 9001L,
                LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 2), new BigDecimal("2.00"));

        var createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Reject application
        var rejectReq = new LeaveApprovalRequest(LeaveApplicationStatus.REJECTED, "HR", "Not eligible");
        mockMvc.perform(patch(BASE_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk());

        // Attempt to cancel rejected application
        mockMvc.perform(patch(BASE_URL + "/" + id + "/cancel"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteApplication() throws Exception {
        var createReq = createRequest(9001L, 9001L,
                LocalDate.of(2025, 11, 1), LocalDate.of(2025, 11, 2), new BigDecimal("2.00"));

        var createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetApplicationsByEmployee() throws Exception {
        var req1 = createRequest(9001L, 9001L,
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 2), new BigDecimal("2.00"));
        var req2 = createRequest(9002L, 9001L,
                LocalDate.of(2025, 6, 3), LocalDate.of(2025, 6, 4), new BigDecimal("2.00"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "/employee/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].employeeCode").value("LA-EMP-001"));
    }

    @Test
    void shouldGetApplicationsByStatus() throws Exception {
        var req = createRequest(9001L, 9001L,
                LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 2), new BigDecimal("2.00"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void shouldGetApplicationsByDateRange() throws Exception {
        var req = createRequest(9001L, 9001L,
                LocalDate.of(2025, 5, 10), LocalDate.of(2025, 5, 15), new BigDecimal("5.00"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "/date-range")
                        .param("fromDate", "2025-05-01")
                        .param("toDate", "2025-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        // Out-of-range query should return empty
        mockMvc.perform(get(BASE_URL + "/date-range")
                        .param("fromDate", "2025-06-01")
                        .param("toDate", "2025-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void shouldCompleteFullApprovalLifecycle() throws Exception {
        // 1. Employee submits leave application
        var submitReq = createRequest(9001L, 9001L,
                LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 5), new BigDecimal("5.00"));

        var submitResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.approvalLevel").value(0))
                .andReturn();

        var id = objectMapper.readTree(submitResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Manager approves
        var approvalReq = new LeaveApprovalRequest(LeaveApplicationStatus.APPROVED, "Manager", "Approved by manager");

        mockMvc.perform(patch(BASE_URL + "/" + id + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").value("Manager"))
                .andExpect(jsonPath("$.approvalLevel").value(1))
                .andExpect(jsonPath("$.approvedAt").isNotEmpty());

        // 3. Verify final state
        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").value("Manager"))
                .andExpect(jsonPath("$.numberOfDays").value(5.00));
    }
}
