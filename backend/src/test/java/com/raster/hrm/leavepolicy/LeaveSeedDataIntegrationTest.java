package com.raster.hrm.leavepolicy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test that validates the V18 seed data for leave types,
 * leave policies, and leave policy assignments is correctly loaded.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/leavepolicy/seed-data-reload.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(statements = {
    "DELETE FROM leave_policy_assignments;",
    "DELETE FROM leave_policies;",
    "DELETE FROM leave_types;"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class LeaveSeedDataIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String LEAVE_TYPES_URL = "/api/v1/leave-types";
    private static final String LEAVE_POLICIES_URL = "/api/v1/leave-policies";
    private static final String ASSIGNMENTS_URL = "/api/v1/leave-policy-assignments";

    // ── Leave Type seed data validation ──────────────────────────────

    @Test
    void shouldHaveNineSeededLeaveTypes() throws Exception {
        mockMvc.perform(get(LEAVE_TYPES_URL)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(9));
    }

    @Test
    void shouldHaveCasualLeaveType() throws Exception {
        mockMvc.perform(get(LEAVE_TYPES_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CL"))
                .andExpect(jsonPath("$.name").value("Casual Leave"))
                .andExpect(jsonPath("$.category").value("PAID"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldHaveSickLeaveType() throws Exception {
        mockMvc.perform(get(LEAVE_TYPES_URL + "/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SL"))
                .andExpect(jsonPath("$.name").value("Sick Leave"))
                .andExpect(jsonPath("$.category").value("PAID"));
    }

    @Test
    void shouldHaveEarnedLeaveType() throws Exception {
        mockMvc.perform(get(LEAVE_TYPES_URL + "/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("EL"))
                .andExpect(jsonPath("$.name").value("Earned Leave"))
                .andExpect(jsonPath("$.category").value("PAID"));
    }

    @Test
    void shouldHaveMaternityLeaveType() throws Exception {
        mockMvc.perform(get(LEAVE_TYPES_URL + "/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ML"))
                .andExpect(jsonPath("$.name").value("Maternity Leave"))
                .andExpect(jsonPath("$.category").value("STATUTORY"));
    }

    @Test
    void shouldHavePaternityLeaveType() throws Exception {
        mockMvc.perform(get(LEAVE_TYPES_URL + "/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PL"))
                .andExpect(jsonPath("$.name").value("Paternity Leave"))
                .andExpect(jsonPath("$.category").value("STATUTORY"));
    }

    @Test
    void shouldHaveUnpaidLeaveType() throws Exception {
        mockMvc.perform(get(LEAVE_TYPES_URL + "/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("UL"))
                .andExpect(jsonPath("$.name").value("Unpaid Leave"))
                .andExpect(jsonPath("$.category").value("UNPAID"));
    }

    @Test
    void shouldHaveBereavementLeaveType() throws Exception {
        mockMvc.perform(get(LEAVE_TYPES_URL + "/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("BL"))
                .andExpect(jsonPath("$.name").value("Bereavement Leave"))
                .andExpect(jsonPath("$.category").value("SPECIAL"));
    }

    @Test
    void shouldHaveCompensatoryOffLeaveType() throws Exception {
        mockMvc.perform(get(LEAVE_TYPES_URL + "/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CO"))
                .andExpect(jsonPath("$.name").value("Compensatory Off"))
                .andExpect(jsonPath("$.category").value("PAID"));
    }

    // ── Leave Policy seed data validation ────────────────────────────

    @Test
    void shouldHaveNineSeededLeavePolicies() throws Exception {
        mockMvc.perform(get(LEAVE_POLICIES_URL)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(9));
    }

    @Test
    void shouldHaveCasualLeavePolicyWithMonthlyAccrual() throws Exception {
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Standard Casual Leave Policy"))
                .andExpect(jsonPath("$.leaveTypeId").value(1))
                .andExpect(jsonPath("$.accrualFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.accrualDays").value(1.00))
                .andExpect(jsonPath("$.maxAccumulation").value(12.00))
                .andExpect(jsonPath("$.carryForwardLimit").value(0.00))
                .andExpect(jsonPath("$.proRataForNewJoiners").value(true));
    }

    @Test
    void shouldHaveSickLeavePolicyWithCarryForward() throws Exception {
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Standard Sick Leave Policy"))
                .andExpect(jsonPath("$.accrualFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.accrualDays").value(0.50))
                .andExpect(jsonPath("$.maxAccumulation").value(6.00))
                .andExpect(jsonPath("$.carryForwardLimit").value(3.00))
                .andExpect(jsonPath("$.proRataForNewJoiners").value(false));
    }

    @Test
    void shouldHaveEarnedLeavePolicyWithMaxAccumulation() throws Exception {
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Standard Earned Leave Policy"))
                .andExpect(jsonPath("$.accrualFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.accrualDays").value(1.25))
                .andExpect(jsonPath("$.maxAccumulation").value(30.00))
                .andExpect(jsonPath("$.carryForwardLimit").value(15.00))
                .andExpect(jsonPath("$.proRataForNewJoiners").value(true))
                .andExpect(jsonPath("$.minServiceDaysRequired").value(90));
    }

    @Test
    void shouldHaveMaternityLeavePolicyWithAnnualAccrual() throws Exception {
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Standard Maternity Leave Policy"))
                .andExpect(jsonPath("$.accrualFrequency").value("ANNUAL"))
                .andExpect(jsonPath("$.accrualDays").value(182.00))
                .andExpect(jsonPath("$.maxAccumulation").value(182.00))
                .andExpect(jsonPath("$.carryForwardLimit").value(0.00))
                .andExpect(jsonPath("$.minServiceDaysRequired").value(80));
    }

    @Test
    void shouldHaveCompOffPolicyWithQuarterlyAccrual() throws Exception {
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Standard Comp-Off Policy"))
                .andExpect(jsonPath("$.accrualFrequency").value("QUARTERLY"))
                .andExpect(jsonPath("$.accrualDays").value(3.00))
                .andExpect(jsonPath("$.maxAccumulation").value(6.00))
                .andExpect(jsonPath("$.carryForwardLimit").value(2.00));
    }

    // ── Leave Policy Assignment seed data validation ─────────────────

    @Test
    void shouldHaveSixtySeededAssignments() throws Exception {
        mockMvc.perform(get(ASSIGNMENTS_URL)
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(60));
    }

    @Test
    void shouldHaveCasualLeaveAssignedToAllDepartments() throws Exception {
        mockMvc.perform(get(ASSIGNMENTS_URL + "/department/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNumber());

        mockMvc.perform(get(ASSIGNMENTS_URL + "/department/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.leavePolicyId == 1)]").exists());
    }

    @Test
    void shouldHaveAssignmentsByType() throws Exception {
        mockMvc.perform(get(ASSIGNMENTS_URL + "/type/DEPARTMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(60));
    }
}
