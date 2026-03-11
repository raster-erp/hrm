package com.raster.hrm.leaveapplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.leaveapplication.controller.LeaveApplicationController;
import com.raster.hrm.leaveapplication.dto.LeaveApplicationRequest;
import com.raster.hrm.leaveapplication.dto.LeaveApplicationResponse;
import com.raster.hrm.leaveapplication.dto.LeaveApprovalRequest;
import com.raster.hrm.leaveapplication.entity.LeaveApplicationStatus;
import com.raster.hrm.leaveapplication.service.LeaveApplicationService;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(LeaveApplicationController.class)
class LeaveApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaveApplicationService leaveApplicationService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/leave-applications";

    private LeaveApplicationResponse createResponse(Long id, Long employeeId, String employeeName) {
        return new LeaveApplicationResponse(
                id, employeeId, "EMP001", employeeName,
                1L, "Casual Leave",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 3),
                new BigDecimal("3.00"),
                "Personal work",
                "PENDING",
                0,
                "Test application",
                null, null,
                LocalDateTime.of(2024, 2, 28, 10, 0),
                LocalDateTime.of(2024, 2, 28, 10, 0)
        );
    }

    private LeaveApplicationRequest createRequest() {
        return new LeaveApplicationRequest(
                1L, 1L,
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 3),
                new BigDecimal("3.00"),
                "Personal work",
                "Test application"
        );
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        var responses = List.of(
                createResponse(1L, 1L, "John Doe"),
                createResponse(2L, 2L, "Jane Smith")
        );
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 2);
        when(leaveApplicationService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.content[1].employeeName").value("Jane Smith"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        var response = createResponse(1L, 1L, "John Doe");
        when(leaveApplicationService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.leaveTypeName").value("Casual Leave"));
    }

    @Test
    void getById_shouldReturn404() throws Exception {
        when(leaveApplicationService.getById(999L))
                .thenThrow(new ResourceNotFoundException("LeaveApplication", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturnPage() throws Exception {
        var responses = List.of(createResponse(1L, 1L, "John Doe"));
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 1);
        when(leaveApplicationService.getByEmployee(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].employeeId").value(1));
    }

    @Test
    void getByStatus_shouldReturnPage() throws Exception {
        var responses = List.of(createResponse(1L, 1L, "John Doe"));
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 1);
        when(leaveApplicationService.getByStatus(eq(LeaveApplicationStatus.PENDING), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getByLeaveType_shouldReturnPage() throws Exception {
        var responses = List.of(createResponse(1L, 1L, "John Doe"));
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 1);
        when(leaveApplicationService.getByLeaveType(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/leave-type/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getByDateRange_shouldReturnPage() throws Exception {
        var responses = List.of(createResponse(1L, 1L, "John Doe"));
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 1);
        when(leaveApplicationService.getByDateRange(
                eq(LocalDate.of(2024, 3, 1)),
                eq(LocalDate.of(2024, 3, 31)),
                any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/date-range")
                        .param("fromDate", "2024-03-01")
                        .param("toDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getByEmployeeAndDateRange_shouldReturnPage() throws Exception {
        var responses = List.of(createResponse(1L, 1L, "John Doe"));
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 1);
        when(leaveApplicationService.getByEmployeeAndDateRange(
                eq(1L),
                eq(LocalDate.of(2024, 3, 1)),
                eq(LocalDate.of(2024, 3, 31)),
                any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1/date-range")
                        .param("fromDate", "2024-03-01")
                        .param("toDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, 1L, "John Doe");
        when(leaveApplicationService.create(any(LeaveApplicationRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, 1L, "John Doe");
        when(leaveApplicationService.update(eq(1L), any(LeaveApplicationRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void update_shouldReturn404() throws Exception {
        var request = createRequest();
        when(leaveApplicationService.update(eq(999L), any(LeaveApplicationRequest.class)))
                .thenThrow(new ResourceNotFoundException("LeaveApplication", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn400WhenNotPending() throws Exception {
        var request = createRequest();
        when(leaveApplicationService.update(eq(1L), any(LeaveApplicationRequest.class)))
                .thenThrow(new BadRequestException("Only PENDING leave applications can be updated"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_shouldReturn200() throws Exception {
        var approvalRequest = new LeaveApprovalRequest(LeaveApplicationStatus.APPROVED, "admin", "Approved");
        var response = new LeaveApplicationResponse(
                1L, 1L, "EMP001", "John Doe",
                1L, "Casual Leave",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 3),
                new BigDecimal("3.00"),
                "Personal work",
                "APPROVED",
                1,
                "Approved",
                "admin", LocalDateTime.of(2024, 3, 1, 10, 0),
                LocalDateTime.of(2024, 2, 28, 10, 0),
                LocalDateTime.of(2024, 3, 1, 10, 0)
        );
        when(leaveApplicationService.approve(eq(1L), any(LeaveApprovalRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").value("admin"));
    }

    @Test
    void cancel_shouldReturn200() throws Exception {
        var response = new LeaveApplicationResponse(
                1L, 1L, "EMP001", "John Doe",
                1L, "Casual Leave",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 3),
                new BigDecimal("3.00"),
                "Personal work",
                "CANCELLED",
                0,
                null,
                null, null,
                LocalDateTime.of(2024, 2, 28, 10, 0),
                LocalDateTime.of(2024, 3, 1, 10, 0)
        );
        when(leaveApplicationService.cancel(1L)).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(leaveApplicationService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(leaveApplicationService).delete(1L);
    }

    @Test
    void delete_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("LeaveApplication", "id", 999L))
                .when(leaveApplicationService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
