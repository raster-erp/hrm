package com.raster.hrm.attendance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.attendance.controller.AttendancePunchController;
import com.raster.hrm.attendance.dto.AttendancePunchRequest;
import com.raster.hrm.attendance.dto.AttendancePunchResponse;
import com.raster.hrm.attendance.dto.PunchSyncRequest;
import com.raster.hrm.attendance.dto.PunchSyncResponse;
import com.raster.hrm.attendance.service.AttendancePunchService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendancePunchController.class)
class AttendancePunchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendancePunchService punchService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/attendance-punches";

    private AttendancePunchResponse createResponse(Long id, String direction, LocalDateTime punchTime) {
        return new AttendancePunchResponse(
                id, 1L, "EMP001", "John Doe",
                1L, "BIO-001", "Main Entrance",
                punchTime, direction,
                "raw-data", true, "DEVICE",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    @Test
    void getAll_shouldReturnPageOfPunches() throws Exception {
        var punches = List.of(
                createResponse(1L, "IN", LocalDateTime.of(2024, 6, 1, 9, 0)),
                createResponse(2L, "OUT", LocalDateTime.of(2024, 6, 1, 18, 0))
        );
        var page = new PageImpl<>(punches, PageRequest.of(0, 20), 2);
        when(punchService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].direction").value("IN"))
                .andExpect(jsonPath("$.content[1].direction").value("OUT"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnPunch() throws Exception {
        var response = createResponse(1L, "IN", LocalDateTime.of(2024, 6, 1, 9, 0));
        when(punchService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.deviceSerialNumber").value("BIO-001"))
                .andExpect(jsonPath("$.direction").value("IN"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(punchService.getById(999L))
                .thenThrow(new ResourceNotFoundException("AttendancePunch", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturnPunches() throws Exception {
        var punches = List.of(
                createResponse(1L, "IN", LocalDateTime.of(2024, 6, 1, 9, 0))
        );
        var page = new PageImpl<>(punches, PageRequest.of(0, 20), 1);
        when(punchService.getByEmployeeId(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].employeeCode").value("EMP001"));
    }

    @Test
    void getByEmployee_shouldReturn404WhenEmployeeNotFound() throws Exception {
        when(punchService.getByEmployeeId(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/employee/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByDateRange_shouldReturnPunches() throws Exception {
        var punches = List.of(
                createResponse(1L, "IN", LocalDateTime.of(2024, 6, 1, 9, 0))
        );
        var page = new PageImpl<>(punches, PageRequest.of(0, 20), 1);
        when(punchService.getByDateRange(eq(LocalDate.of(2024, 6, 1)), eq(LocalDate.of(2024, 6, 30)), any()))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/date-range")
                        .param("from", "2024-06-01")
                        .param("to", "2024-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getByEmployeeAndDateRange_shouldReturnPunches() throws Exception {
        var punches = List.of(
                createResponse(1L, "IN", LocalDateTime.of(2024, 6, 1, 9, 0))
        );
        var page = new PageImpl<>(punches, PageRequest.of(0, 20), 1);
        when(punchService.getByEmployeeAndDateRange(eq(1L), eq(LocalDate.of(2024, 6, 1)),
                eq(LocalDate.of(2024, 6, 30)), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1/date-range")
                        .param("from", "2024-06-01")
                        .param("to", "2024-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void create_shouldReturn201WithCreatedPunch() throws Exception {
        var request = new AttendancePunchRequest(1L, 1L,
                LocalDateTime.of(2024, 6, 1, 9, 0), "IN", "raw-data");
        var response = createResponse(1L, "IN", LocalDateTime.of(2024, 6, 1, 9, 0));
        when(punchService.create(any(AttendancePunchRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.direction").value("IN"));
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new AttendancePunchRequest(null, null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void syncPunches_shouldReturnSyncResponse() throws Exception {
        var punches = List.of(
                new PunchSyncRequest.PunchData(1L, "2024-06-01T09:00:00", "IN", "raw-1"),
                new PunchSyncRequest.PunchData(1L, "2024-06-01T18:00:00", "OUT", "raw-2")
        );
        var request = new PunchSyncRequest(1L, punches);
        var response = new PunchSyncResponse(2, 2, 0, List.of(1L, 2L));
        when(punchService.syncPunches(any(PunchSyncRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReceived").value(2))
                .andExpect(jsonPath("$.accepted").value(2))
                .andExpect(jsonPath("$.duplicatesSkipped").value(0))
                .andExpect(jsonPath("$.acceptedPunchIds.length()").value(2));
    }

    @Test
    void syncPunches_shouldReturn404WhenDeviceNotFound() throws Exception {
        var punches = List.of(
                new PunchSyncRequest.PunchData(1L, "2024-06-01T09:00:00", "IN", null)
        );
        var request = new PunchSyncRequest(999L, punches);
        when(punchService.syncPunches(any(PunchSyncRequest.class)))
                .thenThrow(new ResourceNotFoundException("Device", "id", 999L));

        mockMvc.perform(post(BASE_URL + "/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void syncPunches_shouldReturn400WhenEmptyPunches() throws Exception {
        var request = new PunchSyncRequest(1L, List.of());

        mockMvc.perform(post(BASE_URL + "/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(punchService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(punchService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("AttendancePunch", "id", 999L))
                .when(punchService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
