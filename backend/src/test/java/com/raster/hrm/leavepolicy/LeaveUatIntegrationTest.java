package com.raster.hrm.leavepolicy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.leavepolicy.service.LeaveAccrualService;
import com.raster.hrm.leavepolicyassignment.dto.LeavePolicyAssignmentRequest;
import com.raster.hrm.leavepolicyassignment.entity.LeavePolicyAssignment;
import com.raster.hrm.leavepolicyassignment.repository.LeavePolicyAssignmentRepository;
import com.raster.hrm.leavetype.dto.LeaveTypeRequest;
import com.raster.hrm.leavepolicy.dto.LeavePolicyRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprehensive UAT-style integration tests for 3.1 Leave Type & Policy Setup.
 * Validates full end-to-end workflows covering leave type CRUD, leave policy CRUD,
 * policy assignment CRUD, accrual computations across configurations, validation
 * rules, and edge cases.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/leavepolicy/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(statements = {
    "DELETE FROM leave_policy_assignments;",
    "DELETE FROM leave_policies;",
    "DELETE FROM leave_types;"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class LeaveUatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LeaveAccrualService leaveAccrualService;

    @Autowired
    private LeavePolicyAssignmentRepository assignmentRepository;

    private static final String LEAVE_TYPES_URL = "/api/v1/leave-types";
    private static final String LEAVE_POLICIES_URL = "/api/v1/leave-policies";
    private static final String ASSIGNMENTS_URL = "/api/v1/leave-policy-assignments";

    // ========== Leave Type UAT Tests ==========

    @Test
    void shouldPerformFullLeaveTypeLifecycle() throws Exception {
        // Create a new leave type
        var createResult = mockMvc.perform(post(LEAVE_TYPES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeaveTypeRequest("MAT", "Maternity Leave", "STATUTORY", "For maternity"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("MAT"))
                .andExpect(jsonPath("$.name").value("Maternity Leave"))
                .andExpect(jsonPath("$.category").value("STATUTORY"))
                .andExpect(jsonPath("$.description").value("For maternity"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Read the newly created leave type
        mockMvc.perform(get(LEAVE_TYPES_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MAT"))
                .andExpect(jsonPath("$.category").value("STATUTORY"));

        // Update the leave type
        mockMvc.perform(put(LEAVE_TYPES_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeaveTypeRequest("MAT", "Maternity Leave Updated", "STATUTORY", "Updated description"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maternity Leave Updated"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        // Toggle active status to inactive
        mockMvc.perform(patch(LEAVE_TYPES_URL + "/" + id + "/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Toggle active status back to active
        mockMvc.perform(patch(LEAVE_TYPES_URL + "/" + id + "/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        // Delete the leave type
        mockMvc.perform(delete(LEAVE_TYPES_URL + "/" + id))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get(LEAVE_TYPES_URL + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFilterLeaveTypesByCategory() throws Exception {
        // Create leave types in different categories
        mockMvc.perform(post(LEAVE_TYPES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeaveTypeRequest("SP1", "Special Leave 1", "SPECIAL", null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post(LEAVE_TYPES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeaveTypeRequest("SP2", "Special Leave 2", "SPECIAL", null))))
                .andExpect(status().isCreated());

        // Filter by SPECIAL category - should include the 2 we just created
        mockMvc.perform(get(LEAVE_TYPES_URL + "/category/SPECIAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        // Filter by PAID category - should include CL and SL from test data
        mockMvc.perform(get(LEAVE_TYPES_URL + "/category/PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void shouldGetActiveLeaveTypes() throws Exception {
        // All test data types are active, so should return at least 3
        mockMvc.perform(get(LEAVE_TYPES_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void shouldRejectDuplicateLeaveTypeCode() throws Exception {
        // CL already exists in test data
        mockMvc.perform(post(LEAVE_TYPES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeaveTypeRequest("CL", "Duplicate Code Leave", "PAID", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectLeaveTypeWithBlankCode() throws Exception {
        mockMvc.perform(post(LEAVE_TYPES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeaveTypeRequest("", "No Code Leave", "PAID", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectLeaveTypeWithBlankName() throws Exception {
        mockMvc.perform(post(LEAVE_TYPES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeaveTypeRequest("XX", "", "PAID", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundForNonExistentLeaveType() throws Exception {
        mockMvc.perform(get(LEAVE_TYPES_URL + "/99999"))
                .andExpect(status().isNotFound());
    }

    // ========== Leave Policy UAT Tests ==========

    @Test
    void shouldPerformFullLeavePolicyLifecycle() throws Exception {
        // Create a policy with all fields
        var request = new LeavePolicyRequest(
                "UAT Test Policy", 9001L, "MONTHLY",
                new BigDecimal("1.50"), new BigDecimal("18.00"),
                new BigDecimal("6.00"), true, 30, "UAT test description");

        var createResult = mockMvc.perform(post(LEAVE_POLICIES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("UAT Test Policy"))
                .andExpect(jsonPath("$.leaveTypeId").value(9001))
                .andExpect(jsonPath("$.leaveTypeName").value("Casual Leave"))
                .andExpect(jsonPath("$.leaveTypeCode").value("CL"))
                .andExpect(jsonPath("$.accrualFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.accrualDays").value(1.50))
                .andExpect(jsonPath("$.maxAccumulation").value(18.00))
                .andExpect(jsonPath("$.carryForwardLimit").value(6.00))
                .andExpect(jsonPath("$.proRataForNewJoiners").value(true))
                .andExpect(jsonPath("$.minServiceDaysRequired").value(30))
                .andExpect(jsonPath("$.description").value("UAT test description"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Update policy
        var updateRequest = new LeavePolicyRequest(
                "UAT Test Policy Updated", 9002L, "QUARTERLY",
                new BigDecimal("3.00"), new BigDecimal("24.00"),
                new BigDecimal("10.00"), false, 60, "Updated description");

        mockMvc.perform(put(LEAVE_POLICIES_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UAT Test Policy Updated"))
                .andExpect(jsonPath("$.leaveTypeId").value(9002))
                .andExpect(jsonPath("$.leaveTypeName").value("Sick Leave"))
                .andExpect(jsonPath("$.accrualFrequency").value("QUARTERLY"))
                .andExpect(jsonPath("$.accrualDays").value(3.00))
                .andExpect(jsonPath("$.maxAccumulation").value(24.00))
                .andExpect(jsonPath("$.carryForwardLimit").value(10.00))
                .andExpect(jsonPath("$.proRataForNewJoiners").value(false))
                .andExpect(jsonPath("$.minServiceDaysRequired").value(60));

        // Toggle active off
        mockMvc.perform(patch(LEAVE_POLICIES_URL + "/" + id + "/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Delete
        mockMvc.perform(delete(LEAVE_POLICIES_URL + "/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(LEAVE_POLICIES_URL + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreatePolicyWithMinimalFields() throws Exception {
        var request = new LeavePolicyRequest(
                "Minimal Policy", 9001L, "ANNUAL",
                new BigDecimal("15.00"), null, null, null, null, null);

        mockMvc.perform(post(LEAVE_POLICIES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Minimal Policy"))
                .andExpect(jsonPath("$.accrualFrequency").value("ANNUAL"))
                .andExpect(jsonPath("$.accrualDays").value(15.00))
                .andExpect(jsonPath("$.maxAccumulation").isEmpty())
                .andExpect(jsonPath("$.carryForwardLimit").isEmpty())
                .andExpect(jsonPath("$.proRataForNewJoiners").value(false))
                .andExpect(jsonPath("$.minServiceDaysRequired").value(0));
    }

    @Test
    void shouldRejectDuplicatePolicyName() throws Exception {
        // "Standard CL Policy" already exists in test data
        var request = new LeavePolicyRequest(
                "Standard CL Policy", 9001L, "MONTHLY",
                new BigDecimal("1.00"), null, null, null, null, null);

        mockMvc.perform(post(LEAVE_POLICIES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectPolicyWithNonExistentLeaveType() throws Exception {
        var request = new LeavePolicyRequest(
                "Invalid Type Policy", 99999L, "MONTHLY",
                new BigDecimal("1.00"), null, null, null, null, null);

        mockMvc.perform(post(LEAVE_POLICIES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetPoliciesByLeaveTypeId() throws Exception {
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/leave-type/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].leaveTypeCode").value("CL"));
    }

    @Test
    void shouldGetActivePolicies() throws Exception {
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void shouldPaginatePolicies() throws Exception {
        mockMvc.perform(get(LEAVE_POLICIES_URL)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    // ========== Leave Policy Assignment UAT Tests ==========

    @Test
    void shouldPerformFullAssignmentLifecycle() throws Exception {
        // Create a DESIGNATION-type assignment
        var createRequest = new LeavePolicyAssignmentRequest(
                9001L, "DESIGNATION", null, 1L, null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        var createResult = mockMvc.perform(post(ASSIGNMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leavePolicyId").value(9001))
                .andExpect(jsonPath("$.leavePolicyName").value("Standard CL Policy"))
                .andExpect(jsonPath("$.assignmentType").value("DESIGNATION"))
                .andExpect(jsonPath("$.designationId").value(1))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Update assignment type to INDIVIDUAL
        var updateRequest = new LeavePolicyAssignmentRequest(
                9002L, "INDIVIDUAL", null, null, 1L,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 30));

        mockMvc.perform(put(ASSIGNMENTS_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leavePolicyId").value(9002))
                .andExpect(jsonPath("$.assignmentType").value("INDIVIDUAL"))
                .andExpect(jsonPath("$.employeeId").value(1));

        // Toggle active off
        mockMvc.perform(patch(ASSIGNMENTS_URL + "/" + id + "/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Delete
        mockMvc.perform(delete(ASSIGNMENTS_URL + "/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(ASSIGNMENTS_URL + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateDepartmentAssignment() throws Exception {
        var request = new LeavePolicyAssignmentRequest(
                9001L, "DEPARTMENT", 1L, null, null,
                LocalDate.of(2026, 6, 1), null);

        mockMvc.perform(post(ASSIGNMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentType").value("DEPARTMENT"))
                .andExpect(jsonPath("$.departmentId").value(1));
    }

    @Test
    void shouldFilterAssignmentsByPolicy() throws Exception {
        mockMvc.perform(get(ASSIGNMENTS_URL + "/policy/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void shouldFilterAssignmentsByType() throws Exception {
        mockMvc.perform(get(ASSIGNMENTS_URL + "/type/DEPARTMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get(ASSIGNMENTS_URL + "/type/INDIVIDUAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void shouldRejectAssignmentWithNonExistentPolicy() throws Exception {
        var request = new LeavePolicyAssignmentRequest(
                99999L, "DEPARTMENT", 1L, null, null,
                LocalDate.of(2026, 1, 1), null);

        mockMvc.perform(post(ASSIGNMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldPaginateAssignments() throws Exception {
        mockMvc.perform(get(ASSIGNMENTS_URL)
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)));
    }

    // ========== Accrual Computation UAT Tests ==========

    @Test
    @Transactional
    void shouldValidateMonthlyAccrualOnFirstOfEveryMonth() throws Exception {
        var assignment = assignmentRepository.findById(9001L).orElseThrow();

        // Monthly accrual should be due on the 1st of every month
        for (int month = 1; month <= 12; month++) {
            var firstOfMonth = LocalDate.of(2026, month, 1);
            assertThat(invokeIsAccrualDue(assignment, firstOfMonth))
                    .as("Monthly accrual should be due on %s", firstOfMonth)
                    .isTrue();
        }

        // Not due on any other day of the month
        for (int day = 2; day <= 28; day++) {
            var otherDay = LocalDate.of(2026, 3, day);
            assertThat(invokeIsAccrualDue(assignment, otherDay))
                    .as("Monthly accrual should NOT be due on %s", otherDay)
                    .isFalse();
        }
    }

    @Test
    @Transactional
    void shouldValidateQuarterlyAccrualOnlyOnQuarterStartMonths() throws Exception {
        var assignment = assignmentRepository.findById(9002L).orElseThrow();

        // Quarterly accrual should be due only on 1st of Jan, Apr, Jul, Oct
        int[] quarterMonths = {1, 4, 7, 10};
        for (int month : quarterMonths) {
            var quarterStart = LocalDate.of(2026, month, 1);
            assertThat(invokeIsAccrualDue(assignment, quarterStart))
                    .as("Quarterly accrual should be due on %s", quarterStart)
                    .isTrue();
        }

        // Not due on 1st of non-quarter months
        int[] nonQuarterMonths = {2, 3, 5, 6, 8, 9, 11, 12};
        for (int month : nonQuarterMonths) {
            var nonQuarterDate = LocalDate.of(2026, month, 1);
            assertThat(invokeIsAccrualDue(assignment, nonQuarterDate))
                    .as("Quarterly accrual should NOT be due on %s", nonQuarterDate)
                    .isFalse();
        }
    }

    @Test
    @Transactional
    void shouldValidateAnnualAccrualOnlyOnJanuary1st() throws Exception {
        // Create an annual policy assignment via API first
        var assignRequest = new LeavePolicyAssignmentRequest(
                9003L, "DEPARTMENT", 1L, null, null,
                LocalDate.of(2025, 1, 1), null);

        var createResult = mockMvc.perform(post(ASSIGNMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        var assignmentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();
        var assignment = assignmentRepository.findById(assignmentId).orElseThrow();

        // Annual accrual is due only on January 1st
        var jan1 = LocalDate.of(2026, 1, 1);
        assertThat(invokeIsAccrualDue(assignment, jan1)).isTrue();

        // Not due on any other 1st of the month
        for (int month = 2; month <= 12; month++) {
            var otherFirst = LocalDate.of(2026, month, 1);
            assertThat(invokeIsAccrualDue(assignment, otherFirst))
                    .as("Annual accrual should NOT be due on %s", otherFirst)
                    .isFalse();
        }

        // Not due on any other day in January
        for (int day = 2; day <= 31; day++) {
            var otherDay = LocalDate.of(2026, 1, day);
            assertThat(invokeIsAccrualDue(assignment, otherDay))
                    .as("Annual accrual should NOT be due on Jan %d", day)
                    .isFalse();
        }
    }

    @Test
    @Transactional
    void shouldRespectEffectiveDateBoundaries() throws Exception {
        // Assignment 9002 has effectiveFrom=2026-01-01 and effectiveTo=2026-12-31
        var assignment = assignmentRepository.findById(9002L).orElseThrow();

        // Before effective date
        assertThat(invokeIsAccrualDue(assignment, LocalDate.of(2025, 10, 1))).isFalse();
        assertThat(invokeIsAccrualDue(assignment, LocalDate.of(2025, 12, 31))).isFalse();

        // On effective from date (Jan 1st, also quarter start)
        assertThat(invokeIsAccrualDue(assignment, LocalDate.of(2026, 1, 1))).isTrue();

        // Within effective range
        assertThat(invokeIsAccrualDue(assignment, LocalDate.of(2026, 4, 1))).isTrue();
        assertThat(invokeIsAccrualDue(assignment, LocalDate.of(2026, 7, 1))).isTrue();
        assertThat(invokeIsAccrualDue(assignment, LocalDate.of(2026, 10, 1))).isTrue();

        // After effective to date
        assertThat(invokeIsAccrualDue(assignment, LocalDate.of(2027, 1, 1))).isFalse();
        assertThat(invokeIsAccrualDue(assignment, LocalDate.of(2027, 4, 1))).isFalse();
    }

    @Test
    @Transactional
    void shouldHandleOpenEndedAssignment() throws Exception {
        // Assignment 9001 has no effectiveTo (open-ended)
        var assignment = assignmentRepository.findById(9001L).orElseThrow();

        // Should accrue for future dates within effective range
        assertThat(invokeIsAccrualDue(assignment, LocalDate.of(2027, 1, 1))).isTrue();
        assertThat(invokeIsAccrualDue(assignment, LocalDate.of(2028, 6, 1))).isTrue();
        assertThat(invokeIsAccrualDue(assignment, LocalDate.of(2030, 1, 1))).isTrue();
    }

    @Test
    @Transactional
    void shouldValidateAccrualAcrossDifferentPolicyConfigurations() throws Exception {
        // Test that policies with different accrual frequencies are correctly evaluated
        // in the same test run, simulating a real scenario where a company has
        // multiple policies active simultaneously

        var monthlyAssignment = assignmentRepository.findById(9001L).orElseThrow();
        var quarterlyAssignment = assignmentRepository.findById(9002L).orElseThrow();

        // On January 1st: both should accrue
        var jan1 = LocalDate.of(2026, 1, 1);
        assertThat(invokeIsAccrualDue(monthlyAssignment, jan1)).isTrue();
        assertThat(invokeIsAccrualDue(quarterlyAssignment, jan1)).isTrue();

        // On February 1st: only monthly should accrue
        var feb1 = LocalDate.of(2026, 2, 1);
        assertThat(invokeIsAccrualDue(monthlyAssignment, feb1)).isTrue();
        assertThat(invokeIsAccrualDue(quarterlyAssignment, feb1)).isFalse();

        // On March 15th: neither should accrue
        var mar15 = LocalDate.of(2026, 3, 15);
        assertThat(invokeIsAccrualDue(monthlyAssignment, mar15)).isFalse();
        assertThat(invokeIsAccrualDue(quarterlyAssignment, mar15)).isFalse();

        // On April 1st: both should accrue (monthly + quarter start)
        var apr1 = LocalDate.of(2026, 4, 1);
        assertThat(invokeIsAccrualDue(monthlyAssignment, apr1)).isTrue();
        assertThat(invokeIsAccrualDue(quarterlyAssignment, apr1)).isTrue();
    }

    @Test
    @Transactional
    void shouldVerifyPolicyConfigurationPersistedCorrectly() throws Exception {
        // Verify test data policy configurations are correct
        // Policy 9001: Monthly, 1 day/month, max 12, carry forward 5
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accrualFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.accrualDays").value(1.00))
                .andExpect(jsonPath("$.maxAccumulation").value(12.00))
                .andExpect(jsonPath("$.carryForwardLimit").value(5.00))
                .andExpect(jsonPath("$.proRataForNewJoiners").value(false))
                .andExpect(jsonPath("$.minServiceDaysRequired").value(0));

        // Policy 9002: Quarterly, 3 days/quarter, max 12, carry forward 3, pro-rata yes, min 30 days
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/9002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accrualFrequency").value("QUARTERLY"))
                .andExpect(jsonPath("$.accrualDays").value(3.00))
                .andExpect(jsonPath("$.maxAccumulation").value(12.00))
                .andExpect(jsonPath("$.carryForwardLimit").value(3.00))
                .andExpect(jsonPath("$.proRataForNewJoiners").value(true))
                .andExpect(jsonPath("$.minServiceDaysRequired").value(30));

        // Policy 9003: Annual, 0 days, no limits (unpaid leave)
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/9003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accrualFrequency").value("ANNUAL"))
                .andExpect(jsonPath("$.accrualDays").value(0.00));
    }

    // ========== End-to-End Workflow Tests ==========

    @Test
    void shouldCompleteFullWorkflowCreateTypeCreatePolicyAssignToEntity() throws Exception {
        // Step 1: Create a new leave type
        var typeResult = mockMvc.perform(post(LEAVE_TYPES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeaveTypeRequest("PAT", "Paternity Leave", "STATUTORY", "For new fathers"))))
                .andExpect(status().isCreated())
                .andReturn();
        var typeId = objectMapper.readTree(typeResult.getResponse().getContentAsString()).get("id").asLong();

        // Step 2: Create a policy for the new leave type
        var policyRequest = new LeavePolicyRequest(
                "Paternity Leave Policy", typeId, "ANNUAL",
                new BigDecimal("15.00"), new BigDecimal("15.00"),
                null, false, 180, "15 days paternity leave per year");

        var policyResult = mockMvc.perform(post(LEAVE_POLICIES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leaveTypeId").value(typeId))
                .andExpect(jsonPath("$.leaveTypeName").value("Paternity Leave"))
                .andExpect(jsonPath("$.leaveTypeCode").value("PAT"))
                .andReturn();
        var policyId = objectMapper.readTree(policyResult.getResponse().getContentAsString()).get("id").asLong();

        // Step 3: Assign the policy to a department
        var assignRequest = new LeavePolicyAssignmentRequest(
                policyId, "DEPARTMENT", 1L, null, null,
                LocalDate.of(2026, 1, 1), null);

        mockMvc.perform(post(ASSIGNMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leavePolicyId").value(policyId))
                .andExpect(jsonPath("$.leavePolicyName").value("Paternity Leave Policy"))
                .andExpect(jsonPath("$.assignmentType").value("DEPARTMENT"))
                .andExpect(jsonPath("$.departmentId").value(1));

        // Step 4: Verify the full chain - policy lists for the leave type
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/leave-type/" + typeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Paternity Leave Policy"));

        // Step 5: Verify assignments for the department
        mockMvc.perform(get(ASSIGNMENTS_URL + "/department/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.leavePolicyName == 'Paternity Leave Policy')]").exists());
    }

    @Test
    void shouldCreateMultiplePoliciesForSameLeaveType() throws Exception {
        // Create a second policy for CL (id 9001) with different config
        var request = new LeavePolicyRequest(
                "Premium CL Policy", 9001L, "MONTHLY",
                new BigDecimal("2.00"), new BigDecimal("24.00"),
                new BigDecimal("12.00"), true, 0, "Premium CL for senior staff");

        mockMvc.perform(post(LEAVE_POLICIES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Both policies should appear when filtering by leave type
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/leave-type/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldAssignMultiplePoliciesToDifferentEntityTypes() throws Exception {
        // Assign a policy to DEPARTMENT
        mockMvc.perform(post(ASSIGNMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeavePolicyAssignmentRequest(
                                        9001L, "DEPARTMENT", 2L, null, null,
                                        LocalDate.of(2026, 1, 1), null))))
                .andExpect(status().isCreated());

        // Assign same policy to DESIGNATION
        mockMvc.perform(post(ASSIGNMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeavePolicyAssignmentRequest(
                                        9001L, "DESIGNATION", null, 1L, null,
                                        LocalDate.of(2026, 1, 1), null))))
                .andExpect(status().isCreated());

        // Assign same policy to INDIVIDUAL
        mockMvc.perform(post(ASSIGNMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeavePolicyAssignmentRequest(
                                        9001L, "INDIVIDUAL", null, null, 2L,
                                        LocalDate.of(2026, 1, 1), null))))
                .andExpect(status().isCreated());

        // All should appear when filtering by policy
        mockMvc.perform(get(ASSIGNMENTS_URL + "/policy/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4)))); // 1 from test-data + 3 new
    }

    private boolean invokeIsAccrualDue(Object assignment, LocalDate date) throws Exception {
        Method method = LeaveAccrualService.class.getDeclaredMethod(
                "isAccrualDue",
                LeavePolicyAssignment.class,
                LocalDate.class);
        method.setAccessible(true);
        return (boolean) method.invoke(leaveAccrualService, assignment, date);
    }
}
