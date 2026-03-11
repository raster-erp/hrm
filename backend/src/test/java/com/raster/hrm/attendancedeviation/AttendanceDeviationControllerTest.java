package com.raster.hrm.attendancedeviation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.attendancedeviation.controller.AttendanceDeviationController;
import com.raster.hrm.attendancedeviation.dto.AttendanceDeviationRequest;
import com.raster.hrm.attendancedeviation.dto.AttendanceDeviationResponse;
import com.raster.hrm.attendancedeviation.dto.DeviationApprovalRequest;
import com.raster.hrm.attendancedeviation.dto.DeviationSummaryResponse;
import com.raster.hrm.attendancedeviation.entity.DeviationStatus;
import com.raster.hrm.attendancedeviation.entity.DeviationType;
import com.raster.hrm.attendancedeviation.service.AttendanceDeviationService;
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
import java.time.LocalTime;
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

@WebMvcTest(AttendanceDeviationController.class)
class AttendanceDeviationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceDeviationService attendanceDeviationService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/attendance-deviations";

    private AttendanceDeviationResponse createResponse(Long id, Long employeeId, String employeeName, String type) {
        return new AttendanceDeviationResponse(
                id, employeeId, "EMP001", employeeName,
                LocalDate.of(2024, 1, 15),
                type, 15,
                LocalTime.of(9, 0),
                LocalDateTime.of(2024, 1, 15, 9, 15),
                5, "NONE", "PENDING",
                "Test deviation",
                null, null,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private AttendanceDeviationRequest createRequest() {
        return new AttendanceDeviationRequest(
                1L,
                LocalDate.of(2024, 1, 15),
                "LATE_COMING",
                15,
                LocalTime.of(9, 0),
                LocalDateTime.of(2024, 1, 15, 9, 15),
                5,
                "NONE",
                "Test deviation"
        );
    }

    @Test
    void getAll_shouldReturnPageOfDeviations() throws Exception {
        var deviations = List.of(
                createResponse(1L, 1L, "John Doe", "LATE_COMING"),
                createResponse(2L, 2L, "Jane Smith", "EARLY_GOING")
        );
        var page = new PageImpl<>(deviations, PageRequest.of(0, 20), 2);
        when(attendanceDeviationService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.content[1].employeeName").value("Jane Smith"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnDeviation() throws Exception {
        var response = createResponse(1L, 1L, "John Doe", "LATE_COMING");
        when(attendanceDeviationService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value("LATE_COMING"))
                .andExpect(jsonPath("$.deviationMinutes").value(15));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(attendanceDeviationService.getById(999L))
                .thenThrow(new ResourceNotFoundException("AttendanceDeviation", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturnPage() throws Exception {
        var deviations = List.of(createResponse(1L, 1L, "John Doe", "LATE_COMING"));
        var page = new PageImpl<>(deviations, PageRequest.of(0, 20), 1);
        when(attendanceDeviationService.getByEmployeeId(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].employeeId").value(1));
    }

    @Test
    void getByType_shouldReturnPage() throws Exception {
        var deviations = List.of(createResponse(1L, 1L, "John Doe", "LATE_COMING"));
        var page = new PageImpl<>(deviations, PageRequest.of(0, 20), 1);
        when(attendanceDeviationService.getByType(eq(DeviationType.LATE_COMING), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/type/LATE_COMING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].type").value("LATE_COMING"));
    }

    @Test
    void getByStatus_shouldReturnPage() throws Exception {
        var deviations = List.of(createResponse(1L, 1L, "John Doe", "LATE_COMING"));
        var page = new PageImpl<>(deviations, PageRequest.of(0, 20), 1);
        when(attendanceDeviationService.getByStatus(eq(DeviationStatus.PENDING), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getByDateRange_shouldReturnPage() throws Exception {
        var deviations = List.of(createResponse(1L, 1L, "John Doe", "LATE_COMING"));
        var page = new PageImpl<>(deviations, PageRequest.of(0, 20), 1);
        when(attendanceDeviationService.getByDateRange(
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
    void create_shouldReturn201WithCreatedDeviation() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, 1L, "John Doe", "LATE_COMING");
        when(attendanceDeviationService.create(any(AttendanceDeviationRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void update_shouldReturnUpdatedDeviation() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, 1L, "John Doe", "LATE_COMING");
        when(attendanceDeviationService.update(eq(1L), any(AttendanceDeviationRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(attendanceDeviationService.update(eq(999L), any(AttendanceDeviationRequest.class)))
                .thenThrow(new ResourceNotFoundException("AttendanceDeviation", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn400WhenNotPending() throws Exception {
        var request = createRequest();
        when(attendanceDeviationService.update(eq(1L), any(AttendanceDeviationRequest.class)))
                .thenThrow(new BadRequestException("Cannot update attendance deviation with status: APPROVED"));

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_shouldReturnApprovedDeviation() throws Exception {
        var approvalRequest = new DeviationApprovalRequest("APPROVED", "admin", "Acknowledged");
        var response = new AttendanceDeviationResponse(
                1L, 1L, "EMP001", "John Doe",
                LocalDate.of(2024, 1, 15),
                "LATE_COMING", 15,
                LocalTime.of(9, 0),
                LocalDateTime.of(2024, 1, 15, 9, 15),
                5, "NONE", "APPROVED",
                "Acknowledged",
                "admin", LocalDateTime.of(2024, 1, 16, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 16, 10, 0)
        );
        when(attendanceDeviationService.approve(eq(1L), any(DeviationApprovalRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").value("admin"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(attendanceDeviationService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(attendanceDeviationService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("AttendanceDeviation", "id", 999L))
                .when(attendanceDeviationService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void detectDeviations_shouldReturn201WithDetectedDeviations() throws Exception {
        var response = new AttendanceDeviationResponse(
                1L, 1L, "EMP001", "John Doe",
                LocalDate.of(2024, 1, 15),
                "LATE_COMING", 20,
                LocalTime.of(9, 0),
                LocalDateTime.of(2024, 1, 15, 9, 20),
                5, "NONE", "PENDING",
                "Auto-detected from attendance punches",
                null, null,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
        when(attendanceDeviationService.detectDeviations(eq(1L), eq(LocalDate.of(2024, 1, 15))))
                .thenReturn(List.of(response));

        mockMvc.perform(post(BASE_URL + "/detect")
                        .param("employeeId", "1")
                        .param("date", "2024-01-15"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("LATE_COMING"))
                .andExpect(jsonPath("$[0].deviationMinutes").value(20));
    }

    @Test
    void getSummary_shouldReturnSummaries() throws Exception {
        var summary = new DeviationSummaryResponse(
                1L, "EMP001", "John Doe",
                3, 2, 75, 45, 30,
                1, 1, 1
        );
        when(attendanceDeviationService.getSummary(
                eq(LocalDate.of(2024, 1, 1)),
                eq(LocalDate.of(2024, 1, 31))))
                .thenReturn(List.of(summary));

        mockMvc.perform(get(BASE_URL + "/summary")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeCode").value("EMP001"))
                .andExpect(jsonPath("$[0].lateComingCount").value(3))
                .andExpect(jsonPath("$[0].earlyGoingCount").value(2))
                .andExpect(jsonPath("$[0].totalDeviationMinutes").value(75))
                .andExpect(jsonPath("$[0].warningCount").value(1))
                .andExpect(jsonPath("$[0].leaveDeductionCount").value(1))
                .andExpect(jsonPath("$[0].payCutCount").value(1));
    }
}
