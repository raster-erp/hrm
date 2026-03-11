package com.raster.hrm.attendanceregularization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.attendanceregularization.controller.RegularizationRequestController;
import com.raster.hrm.attendanceregularization.dto.RegularizationApprovalRequest;
import com.raster.hrm.attendanceregularization.dto.RegularizationRequestRequest;
import com.raster.hrm.attendanceregularization.dto.RegularizationRequestResponse;
import com.raster.hrm.attendanceregularization.entity.RegularizationStatus;
import com.raster.hrm.attendanceregularization.entity.RegularizationType;
import com.raster.hrm.attendanceregularization.service.RegularizationRequestService;
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

@WebMvcTest(RegularizationRequestController.class)
class AttendanceRegularizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegularizationRequestService regularizationRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/regularization-requests";

    private RegularizationRequestResponse createResponse(Long id, Long employeeId, String employeeName, String type) {
        return new RegularizationRequestResponse(
                id, employeeId, "EMP001", employeeName,
                LocalDate.of(2024, 1, 15),
                type,
                "Missed punch due to system error",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                null,
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 17, 0),
                "PENDING",
                0,
                "Test regularization",
                null, null,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private RegularizationRequestRequest createRequest() {
        return new RegularizationRequestRequest(
                1L,
                LocalDate.of(2024, 1, 15),
                "MISSED_PUNCH",
                "Missed punch due to system error",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                null,
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 17, 0),
                "Test regularization"
        );
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        var responses = List.of(
                createResponse(1L, 1L, "John Doe", "MISSED_PUNCH"),
                createResponse(2L, 2L, "Jane Smith", "ON_DUTY")
        );
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 2);
        when(regularizationRequestService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.content[1].employeeName").value("Jane Smith"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        var response = createResponse(1L, 1L, "John Doe", "MISSED_PUNCH");
        when(regularizationRequestService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value("MISSED_PUNCH"))
                .andExpect(jsonPath("$.reason").value("Missed punch due to system error"));
    }

    @Test
    void getById_shouldReturn404() throws Exception {
        when(regularizationRequestService.getById(999L))
                .thenThrow(new ResourceNotFoundException("RegularizationRequest", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturnPage() throws Exception {
        var responses = List.of(createResponse(1L, 1L, "John Doe", "MISSED_PUNCH"));
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 1);
        when(regularizationRequestService.getByEmployeeId(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].employeeId").value(1));
    }

    @Test
    void getByStatus_shouldReturnPage() throws Exception {
        var responses = List.of(createResponse(1L, 1L, "John Doe", "MISSED_PUNCH"));
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 1);
        when(regularizationRequestService.getByStatus(eq(RegularizationStatus.PENDING), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getByType_shouldReturnPage() throws Exception {
        var responses = List.of(createResponse(1L, 1L, "John Doe", "MISSED_PUNCH"));
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 1);
        when(regularizationRequestService.getByType(eq(RegularizationType.MISSED_PUNCH), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/type/MISSED_PUNCH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].type").value("MISSED_PUNCH"));
    }

    @Test
    void getByDateRange_shouldReturnPage() throws Exception {
        var responses = List.of(createResponse(1L, 1L, "John Doe", "MISSED_PUNCH"));
        var page = new PageImpl<>(responses, PageRequest.of(0, 20), 1);
        when(regularizationRequestService.getByDateRange(
                eq(LocalDate.of(2024, 1, 1)),
                eq(LocalDate.of(2024, 1, 31)),
                any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, 1L, "John Doe", "MISSED_PUNCH");
        when(regularizationRequestService.create(any(RegularizationRequestRequest.class))).thenReturn(response);

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
        var response = createResponse(1L, 1L, "John Doe", "MISSED_PUNCH");
        when(regularizationRequestService.update(eq(1L), any(RegularizationRequestRequest.class))).thenReturn(response);

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
        when(regularizationRequestService.update(eq(999L), any(RegularizationRequestRequest.class)))
                .thenThrow(new ResourceNotFoundException("RegularizationRequest", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn400WhenNotPending() throws Exception {
        var request = createRequest();
        when(regularizationRequestService.update(eq(1L), any(RegularizationRequestRequest.class)))
                .thenThrow(new BadRequestException("Cannot update regularization request with status: APPROVED"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_shouldReturn200() throws Exception {
        var approvalRequest = new RegularizationApprovalRequest("APPROVED", "admin", "Acknowledged");
        var response = new RegularizationRequestResponse(
                1L, 1L, "EMP001", "John Doe",
                LocalDate.of(2024, 1, 15),
                "MISSED_PUNCH",
                "Missed punch due to system error",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                null,
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 17, 0),
                "APPROVED",
                0,
                "Acknowledged",
                "admin", LocalDateTime.of(2024, 1, 16, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 16, 10, 0)
        );
        when(regularizationRequestService.approve(eq(1L), any(RegularizationApprovalRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").value("admin"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(regularizationRequestService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(regularizationRequestService).delete(1L);
    }

    @Test
    void delete_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("RegularizationRequest", "id", 999L))
                .when(regularizationRequestService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployeeAndDateRange_shouldReturnList() throws Exception {
        var responses = List.of(
                createResponse(1L, 1L, "John Doe", "MISSED_PUNCH"),
                createResponse(2L, 1L, "John Doe", "ON_DUTY")
        );
        when(regularizationRequestService.getByEmployeeAndDateRange(
                eq(1L),
                eq(LocalDate.of(2024, 1, 1)),
                eq(LocalDate.of(2024, 1, 31))))
                .thenReturn(responses);

        mockMvc.perform(get(BASE_URL + "/employee/1/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("MISSED_PUNCH"))
                .andExpect(jsonPath("$[1].type").value("ON_DUTY"));
    }
}
