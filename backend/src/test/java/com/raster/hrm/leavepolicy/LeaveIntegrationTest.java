package com.raster.hrm.leavepolicy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.leavepolicy.service.LeaveAccrualService;
import com.raster.hrm.leavepolicyassignment.dto.LeavePolicyAssignmentRequest;
import com.raster.hrm.leavepolicyassignment.repository.LeavePolicyAssignmentRepository;
import com.raster.hrm.leavetype.dto.LeaveTypeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/leavepolicy/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(statements = "DELETE FROM leave_policy_assignments WHERE id >= 9000; DELETE FROM leave_policies WHERE id >= 9000; DELETE FROM leave_types WHERE id >= 9000;", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class LeaveIntegrationTest {

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

    @Test
    void shouldGetLeaveTypesList() throws Exception {
        mockMvc.perform(get(LEAVE_TYPES_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    void shouldGetLeaveTypeById() throws Exception {
        mockMvc.perform(get(LEAVE_TYPES_URL + "/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9001))
                .andExpect(jsonPath("$.code").value("CL"))
                .andExpect(jsonPath("$.name").value("Casual Leave"))
                .andExpect(jsonPath("$.category").value("PAID"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldCreateNewLeaveType() throws Exception {
        var request = new LeaveTypeRequest("PL", "Privilege Leave", "PAID", null);

        mockMvc.perform(post(LEAVE_TYPES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("PL"))
                .andExpect(jsonPath("$.name").value("Privilege Leave"))
                .andExpect(jsonPath("$.category").value("PAID"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldUpdateLeaveType() throws Exception {
        var request = new LeaveTypeRequest("CL", "Casual Leave Updated", "PAID", "Updated description");

        mockMvc.perform(put(LEAVE_TYPES_URL + "/9001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9001))
                .andExpect(jsonPath("$.name").value("Casual Leave Updated"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void shouldGetLeavePoliciesWithLeaveTypeInfo() throws Exception {
        mockMvc.perform(get(LEAVE_POLICIES_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    void shouldGetLeavePolicyById() throws Exception {
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9001))
                .andExpect(jsonPath("$.name").value("Standard CL Policy"))
                .andExpect(jsonPath("$.leaveTypeId").value(9001))
                .andExpect(jsonPath("$.leaveTypeName").value("Casual Leave"))
                .andExpect(jsonPath("$.leaveTypeCode").value("CL"))
                .andExpect(jsonPath("$.accrualFrequency").value("MONTHLY"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldGetLeavePoliciesByLeaveTypeId() throws Exception {
        mockMvc.perform(get(LEAVE_POLICIES_URL + "/leave-type/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Standard CL Policy"))
                .andExpect(jsonPath("$[0].leaveTypeId").value(9001));
    }

    @Test
    void shouldGetLeavePolicyAssignments() throws Exception {
        mockMvc.perform(get(ASSIGNMENTS_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    void shouldGetAssignmentsByDepartmentId() throws Exception {
        mockMvc.perform(get(ASSIGNMENTS_URL + "/department/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].assignmentType").value("DEPARTMENT"))
                .andExpect(jsonPath("$[0].departmentId").value(1))
                .andExpect(jsonPath("$[0].leavePolicyId").value(9001));
    }

    @Test
    void shouldGetAssignmentsByEmployeeId() throws Exception {
        mockMvc.perform(get(ASSIGNMENTS_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].assignmentType").value("INDIVIDUAL"))
                .andExpect(jsonPath("$[0].employeeId").value(1))
                .andExpect(jsonPath("$[0].leavePolicyId").value(9002));
    }

    @Test
    void shouldCreateNewAssignment() throws Exception {
        var request = new LeavePolicyAssignmentRequest(
                9003L, "DEPARTMENT", 1L, null, null,
                LocalDate.of(2026, 6, 1), null);

        mockMvc.perform(post(ASSIGNMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leavePolicyId").value(9003))
                .andExpect(jsonPath("$.assignmentType").value("DEPARTMENT"))
                .andExpect(jsonPath("$.departmentId").value(1))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldVerifyAccrualDueForMonthlyPolicy() throws Exception {
        var assignment = assignmentRepository.findById(9001L).orElseThrow();

        // Monthly accrual is due on the 1st of each month
        var firstOfMonth = LocalDate.of(2026, 3, 1);
        assertThat(invokeIsAccrualDue(assignment, firstOfMonth)).isTrue();

        // Not due on other days
        var midMonth = LocalDate.of(2026, 3, 15);
        assertThat(invokeIsAccrualDue(assignment, midMonth)).isFalse();
    }

    @Test
    void shouldVerifyAccrualDueForQuarterlyPolicy() throws Exception {
        var assignment = assignmentRepository.findById(9002L).orElseThrow();

        // Quarterly accrual is due on 1st of Jan, Apr, Jul, Oct
        var quarterStart = LocalDate.of(2026, 4, 1);
        assertThat(invokeIsAccrualDue(assignment, quarterStart)).isTrue();

        // Not due on 1st of non-quarter months
        var nonQuarter = LocalDate.of(2026, 2, 1);
        assertThat(invokeIsAccrualDue(assignment, nonQuarter)).isFalse();
    }

    @Test
    void shouldReturnFalseForAccrualBeforeEffectiveDate() throws Exception {
        var assignment = assignmentRepository.findById(9001L).orElseThrow();

        // Effective from is 2026-01-01, so before that should return false
        var beforeEffective = LocalDate.of(2025, 12, 1);
        assertThat(invokeIsAccrualDue(assignment, beforeEffective)).isFalse();
    }

    @Test
    void shouldReturnFalseForAccrualAfterEffectiveTo() throws Exception {
        var assignment = assignmentRepository.findById(9002L).orElseThrow();

        // Assignment 9002 has effectiveTo = 2026-12-31
        var afterExpiry = LocalDate.of(2027, 1, 1);
        assertThat(invokeIsAccrualDue(assignment, afterExpiry)).isFalse();
    }

    private boolean invokeIsAccrualDue(Object assignment, LocalDate date) throws Exception {
        Method method = LeaveAccrualService.class.getDeclaredMethod(
                "isAccrualDue",
                com.raster.hrm.leavepolicyassignment.entity.LeavePolicyAssignment.class,
                LocalDate.class);
        method.setAccessible(true);
        return (boolean) method.invoke(leaveAccrualService, assignment, date);
    }
}
