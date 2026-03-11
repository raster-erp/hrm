package com.raster.hrm.leavepolicy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leavepolicy.controller.LeavePolicyController;
import com.raster.hrm.leavepolicy.dto.LeavePolicyRequest;
import com.raster.hrm.leavepolicy.dto.LeavePolicyResponse;
import com.raster.hrm.leavepolicy.service.LeavePolicyService;
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

@WebMvcTest(LeavePolicyController.class)
class LeavePolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeavePolicyService leavePolicyService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/leave-policies";

    private LeavePolicyResponse createResponse(Long id, String name) {
        return new LeavePolicyResponse(
                id, name,
                10L, "Annual Leave", "AL",
                "MONTHLY",
                new BigDecimal("1.50"),
                new BigDecimal("30.00"),
                new BigDecimal("5.00"),
                true, 90,
                true, "Test policy",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private LeavePolicyRequest createRequest() {
        return new LeavePolicyRequest(
                "Annual Leave Policy",
                10L,
                "MONTHLY",
                new BigDecimal("1.50"),
                new BigDecimal("30.00"),
                new BigDecimal("5.00"),
                true,
                90,
                "Annual leave accrual policy"
        );
    }

    @Test
    void getAll_shouldReturnPageOfPolicies() throws Exception {
        var policies = List.of(
                createResponse(1L, "Policy A"),
                createResponse(2L, "Policy B")
        );
        var page = new PageImpl<>(policies, PageRequest.of(0, 20), 2);
        when(leavePolicyService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Policy A"))
                .andExpect(jsonPath("$.content[1].name").value("Policy B"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnPolicy() throws Exception {
        var response = createResponse(1L, "Policy A");
        when(leavePolicyService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Policy A"))
                .andExpect(jsonPath("$.leaveTypeId").value(10))
                .andExpect(jsonPath("$.leaveTypeName").value("Annual Leave"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(leavePolicyService.getById(999L))
                .thenThrow(new ResourceNotFoundException("LeavePolicy", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByLeaveTypeId_shouldReturnPolicies() throws Exception {
        var policies = List.of(createResponse(1L, "Policy A"));
        when(leavePolicyService.getByLeaveTypeId(10L)).thenReturn(policies);

        mockMvc.perform(get(BASE_URL + "/leave-type/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].leaveTypeId").value(10));
    }

    @Test
    void getActive_shouldReturnActivePolicies() throws Exception {
        var policies = List.of(createResponse(1L, "Policy A"));
        when(leavePolicyService.getActive()).thenReturn(policies);

        mockMvc.perform(get(BASE_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void create_shouldReturn201WithCreatedPolicy() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "Annual Leave Policy");
        when(leavePolicyService.create(any(LeavePolicyRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Annual Leave Policy"));
    }

    @Test
    void create_shouldReturn400WhenNameExists() throws Exception {
        var request = createRequest();
        when(leavePolicyService.create(any(LeavePolicyRequest.class)))
                .thenThrow(new BadRequestException("Leave policy with name 'Annual Leave Policy' already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new LeavePolicyRequest("", null, null, null, null, null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedPolicy() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "Annual Leave Policy");
        when(leavePolicyService.update(eq(1L), any(LeavePolicyRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Annual Leave Policy"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(leavePolicyService.update(eq(999L), any(LeavePolicyRequest.class)))
                .thenThrow(new ResourceNotFoundException("LeavePolicy", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateActive_shouldReturnUpdatedPolicy() throws Exception {
        var response = new LeavePolicyResponse(1L, "Policy A",
                10L, "Annual Leave", "AL",
                "MONTHLY", new BigDecimal("1.50"),
                new BigDecimal("30.00"), new BigDecimal("5.00"),
                true, 90,
                false, "Test",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0));
        when(leavePolicyService.updateActive(eq(1L), eq(false))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateActive_shouldReturn404WhenNotFound() throws Exception {
        when(leavePolicyService.updateActive(eq(999L), eq(false)))
                .thenThrow(new ResourceNotFoundException("LeavePolicy", "id", 999L));

        mockMvc.perform(patch(BASE_URL + "/999/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(leavePolicyService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(leavePolicyService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("LeavePolicy", "id", 999L))
                .when(leavePolicyService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
