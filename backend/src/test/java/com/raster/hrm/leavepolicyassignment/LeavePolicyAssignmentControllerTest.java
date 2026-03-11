package com.raster.hrm.leavepolicyassignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leavepolicyassignment.controller.LeavePolicyAssignmentController;
import com.raster.hrm.leavepolicyassignment.dto.LeavePolicyAssignmentRequest;
import com.raster.hrm.leavepolicyassignment.dto.LeavePolicyAssignmentResponse;
import com.raster.hrm.leavepolicyassignment.entity.AssignmentType;
import com.raster.hrm.leavepolicyassignment.service.LeavePolicyAssignmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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

@WebMvcTest(LeavePolicyAssignmentController.class)
class LeavePolicyAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeavePolicyAssignmentService leavePolicyAssignmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/leave-policy-assignments";

    private LeavePolicyAssignmentResponse createResponse(Long id, String assignmentType) {
        return new LeavePolicyAssignmentResponse(
                id, 1L, "Annual Leave", assignmentType,
                assignmentType.equals("DEPARTMENT") ? 10L : null,
                assignmentType.equals("DESIGNATION") ? 20L : null,
                assignmentType.equals("INDIVIDUAL") ? 30L : null,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                true,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private LeavePolicyAssignmentRequest createRequest() {
        return new LeavePolicyAssignmentRequest(
                1L, "DEPARTMENT", 10L, null, null,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );
    }

    @Test
    void getAll_shouldReturnPageOfAssignments() throws Exception {
        var assignments = List.of(
                createResponse(1L, "DEPARTMENT"),
                createResponse(2L, "INDIVIDUAL")
        );
        var page = new PageImpl<>(assignments, PageRequest.of(0, 20), 2);
        when(leavePolicyAssignmentService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].assignmentType").value("DEPARTMENT"))
                .andExpect(jsonPath("$.content[1].assignmentType").value("INDIVIDUAL"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnAssignment() throws Exception {
        var response = createResponse(1L, "DEPARTMENT");
        when(leavePolicyAssignmentService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.leavePolicyId").value(1))
                .andExpect(jsonPath("$.leavePolicyName").value("Annual Leave"))
                .andExpect(jsonPath("$.assignmentType").value("DEPARTMENT"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(leavePolicyAssignmentService.getById(999L))
                .thenThrow(new ResourceNotFoundException("LeavePolicyAssignment", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByPolicyId_shouldReturnAssignments() throws Exception {
        var assignments = List.of(createResponse(1L, "DEPARTMENT"));
        when(leavePolicyAssignmentService.getByPolicyId(1L)).thenReturn(assignments);

        mockMvc.perform(get(BASE_URL + "/policy/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].leavePolicyId").value(1));
    }

    @Test
    void getByAssignmentType_shouldReturnAssignments() throws Exception {
        var assignments = List.of(createResponse(1L, "DEPARTMENT"));
        when(leavePolicyAssignmentService.getByAssignmentType(AssignmentType.DEPARTMENT)).thenReturn(assignments);

        mockMvc.perform(get(BASE_URL + "/type/DEPARTMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].assignmentType").value("DEPARTMENT"));
    }

    @Test
    void getByDepartmentId_shouldReturnAssignments() throws Exception {
        var assignments = List.of(createResponse(1L, "DEPARTMENT"));
        when(leavePolicyAssignmentService.getByDepartmentId(10L)).thenReturn(assignments);

        mockMvc.perform(get(BASE_URL + "/department/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].departmentId").value(10));
    }

    @Test
    void getByDesignationId_shouldReturnAssignments() throws Exception {
        var assignments = List.of(createResponse(1L, "DESIGNATION"));
        when(leavePolicyAssignmentService.getByDesignationId(20L)).thenReturn(assignments);

        mockMvc.perform(get(BASE_URL + "/designation/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].designationId").value(20));
    }

    @Test
    void getByEmployeeId_shouldReturnAssignments() throws Exception {
        var assignments = List.of(createResponse(1L, "INDIVIDUAL"));
        when(leavePolicyAssignmentService.getByEmployeeId(30L)).thenReturn(assignments);

        mockMvc.perform(get(BASE_URL + "/employee/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeId").value(30));
    }

    @Test
    void create_shouldReturn201WithCreatedAssignment() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "DEPARTMENT");
        when(leavePolicyAssignmentService.create(any(LeavePolicyAssignmentRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.assignmentType").value("DEPARTMENT"));
    }

    @Test
    void create_shouldReturn400WhenPolicyNotFound() throws Exception {
        var request = createRequest();
        when(leavePolicyAssignmentService.create(any(LeavePolicyAssignmentRequest.class)))
                .thenThrow(new ResourceNotFoundException("LeavePolicy", "id", 1L));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new LeavePolicyAssignmentRequest(null, null, null, null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedAssignment() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "DEPARTMENT");
        when(leavePolicyAssignmentService.update(eq(1L), any(LeavePolicyAssignmentRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.assignmentType").value("DEPARTMENT"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(leavePolicyAssignmentService.update(eq(999L), any(LeavePolicyAssignmentRequest.class)))
                .thenThrow(new ResourceNotFoundException("LeavePolicyAssignment", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateActive_shouldReturnUpdatedAssignment() throws Exception {
        var response = new LeavePolicyAssignmentResponse(
                1L, 1L, "Annual Leave", "DEPARTMENT",
                10L, null, null,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                false,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
        when(leavePolicyAssignmentService.updateActive(eq(1L), eq(false))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateActive_shouldReturn404WhenNotFound() throws Exception {
        when(leavePolicyAssignmentService.updateActive(eq(999L), eq(false)))
                .thenThrow(new ResourceNotFoundException("LeavePolicyAssignment", "id", 999L));

        mockMvc.perform(patch(BASE_URL + "/999/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(leavePolicyAssignmentService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(leavePolicyAssignmentService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("LeavePolicyAssignment", "id", 999L))
                .when(leavePolicyAssignmentService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
