package com.raster.hrm.wfh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.wfh.controller.WfhRequestController;
import com.raster.hrm.wfh.dto.WfhApprovalRequest;
import com.raster.hrm.wfh.dto.WfhDashboardResponse;
import com.raster.hrm.wfh.dto.WfhRequestCreateRequest;
import com.raster.hrm.wfh.dto.WfhRequestResponse;
import com.raster.hrm.wfh.entity.WfhStatus;
import com.raster.hrm.wfh.service.WfhRequestService;
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

@WebMvcTest(WfhRequestController.class)
class WfhRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WfhRequestService wfhRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/wfh-requests";

    private WfhRequestResponse createResponse(Long id, Long employeeId, String employeeName, String status) {
        return new WfhRequestResponse(
                id, employeeId, "EMP001", employeeName,
                LocalDate.of(2024, 6, 15), "Working from home", status,
                null, null, "Test remarks",
                LocalDateTime.of(2024, 6, 14, 10, 0),
                LocalDateTime.of(2024, 6, 14, 10, 0)
        );
    }

    @Test
    void getAll_shouldReturnPageOfRequests() throws Exception {
        var responses = List.of(
                createResponse(1L, 1L, "John Doe", "PENDING"),
                createResponse(2L, 2L, "Jane Smith", "APPROVED")
        );
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 2);
        when(wfhRequestService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnRequest() throws Exception {
        var response = createResponse(1L, 1L, "John Doe", "PENDING");
        when(wfhRequestService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(wfhRequestService.getById(999L))
                .thenThrow(new ResourceNotFoundException("WfhRequest", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturnPage() throws Exception {
        var responses = List.of(createResponse(1L, 1L, "John Doe", "PENDING"));
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 1);
        when(wfhRequestService.getByEmployeeId(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].employeeId").value(1));
    }

    @Test
    void getByStatus_shouldReturnPage() throws Exception {
        var responses = List.of(createResponse(1L, 1L, "John Doe", "PENDING"));
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 1);
        when(wfhRequestService.getByStatus(eq(WfhStatus.PENDING), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getByDateRange_shouldReturnPage() throws Exception {
        var responses = List.of(createResponse(1L, 1L, "John Doe", "PENDING"));
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 1);
        when(wfhRequestService.getByDateRange(
                eq(LocalDate.of(2024, 6, 1)),
                eq(LocalDate.of(2024, 6, 30)),
                any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/date-range")
                        .param("startDate", "2024-06-01")
                        .param("endDate", "2024-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void create_shouldReturn201WithCreatedRequest() throws Exception {
        var createRequest = new WfhRequestCreateRequest(1L, LocalDate.of(2024, 6, 15), "WFH needed", "Test");
        var response = createResponse(1L, 1L, "John Doe", "PENDING");
        when(wfhRequestService.create(any(WfhRequestCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var invalidRequest = new WfhRequestCreateRequest(null, null, "", null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedRequest() throws Exception {
        var updateRequest = new WfhRequestCreateRequest(1L, LocalDate.of(2024, 6, 20), "Updated reason", null);
        var response = createResponse(1L, 1L, "John Doe", "PENDING");
        when(wfhRequestService.update(eq(1L), any(WfhRequestCreateRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var updateRequest = new WfhRequestCreateRequest(1L, LocalDate.of(2024, 6, 20), "Updated reason", null);
        when(wfhRequestService.update(eq(999L), any(WfhRequestCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("WfhRequest", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void approve_shouldReturnApprovedRequest() throws Exception {
        var approvalRequest = new WfhApprovalRequest("APPROVED", "admin", "Approved");
        var response = new WfhRequestResponse(
                1L, 1L, "EMP001", "John Doe",
                LocalDate.of(2024, 6, 15), "Working from home", "APPROVED",
                "admin", LocalDateTime.of(2024, 6, 15, 14, 0), "Approved",
                LocalDateTime.of(2024, 6, 14, 10, 0),
                LocalDateTime.of(2024, 6, 15, 14, 0)
        );
        when(wfhRequestService.approve(eq(1L), any(WfhApprovalRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").value("admin"));
    }

    @Test
    void approve_shouldReturn400WhenAlreadyApproved() throws Exception {
        var approvalRequest = new WfhApprovalRequest("APPROVED", "admin", null);
        when(wfhRequestService.approve(eq(1L), any(WfhApprovalRequest.class)))
                .thenThrow(new BadRequestException("Cannot change status"));

        mockMvc.perform(patch(BASE_URL + "/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(wfhRequestService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(wfhRequestService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("WfhRequest", "id", 999L))
                .when(wfhRequestService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDashboard_shouldReturnDashboardData() throws Exception {
        var dashboard = new WfhDashboardResponse(
                1L, "EMP001", "John Doe", 5, 3, 1, 1, true
        );
        when(wfhRequestService.getDashboard(
                eq(LocalDate.of(2024, 6, 1)),
                eq(LocalDate.of(2024, 6, 30))))
                .thenReturn(List.of(dashboard));

        mockMvc.perform(get(BASE_URL + "/dashboard")
                        .param("startDate", "2024-06-01")
                        .param("endDate", "2024-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeCode").value("EMP001"))
                .andExpect(jsonPath("$[0].totalRequests").value(5))
                .andExpect(jsonPath("$[0].approvedRequests").value(3))
                .andExpect(jsonPath("$[0].checkedInToday").value(true));
    }
}
