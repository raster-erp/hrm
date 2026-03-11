package com.raster.hrm.wfh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.wfh.controller.WfhActivityLogController;
import com.raster.hrm.wfh.dto.WfhCheckInRequest;
import com.raster.hrm.wfh.dto.WfhCheckInResponse;
import com.raster.hrm.wfh.service.WfhActivityLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WfhActivityLogController.class)
class WfhActivityLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WfhActivityLogService wfhActivityLogService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/wfh-activity-logs";

    private WfhCheckInResponse createCheckInResponse(Long id, boolean checkedOut) {
        return new WfhCheckInResponse(
                id, 1L, 1L, "EMP001", "John Doe",
                LocalDateTime.of(2024, 6, 15, 9, 0),
                checkedOut ? LocalDateTime.of(2024, 6, 15, 18, 0) : null,
                "192.168.1.1", "Home Office",
                LocalDateTime.of(2024, 6, 15, 9, 0),
                LocalDateTime.of(2024, 6, 15, 9, 0)
        );
    }

    @Test
    void checkIn_shouldReturn201WithCheckInResponse() throws Exception {
        var request = new WfhCheckInRequest(1L, "192.168.1.1", "Home Office");
        var response = createCheckInResponse(1L, false);
        when(wfhActivityLogService.checkIn(any(WfhCheckInRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.wfhRequestId").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.ipAddress").value("192.168.1.1"))
                .andExpect(jsonPath("$.checkOutTime").isEmpty());
    }

    @Test
    void checkIn_shouldReturn400WhenValidationFails() throws Exception {
        var invalidRequest = new WfhCheckInRequest(null, null, null);

        mockMvc.perform(post(BASE_URL + "/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkOut_shouldReturnCheckedOutResponse() throws Exception {
        var response = createCheckInResponse(1L, true);
        when(wfhActivityLogService.checkOut(1L)).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/check-out"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.checkOutTime").isNotEmpty());
    }

    @Test
    void checkOut_shouldReturn404WhenNotFound() throws Exception {
        when(wfhActivityLogService.checkOut(999L))
                .thenThrow(new ResourceNotFoundException("WfhActivityLog", "id", 999L));

        mockMvc.perform(patch(BASE_URL + "/999/check-out"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByRequestId_shouldReturnLogs() throws Exception {
        var logs = List.of(
                createCheckInResponse(1L, true),
                createCheckInResponse(2L, false)
        );
        when(wfhActivityLogService.getByRequestId(1L)).thenReturn(logs);

        mockMvc.perform(get(BASE_URL + "/request/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getActiveSession_shouldReturnActiveLog() throws Exception {
        var response = createCheckInResponse(1L, false);
        when(wfhActivityLogService.getActiveSession(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get(BASE_URL + "/request/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.checkOutTime").isEmpty());
    }

    @Test
    void getActiveSession_shouldReturn204WhenNoActiveSession() throws Exception {
        when(wfhActivityLogService.getActiveSession(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/request/1/active"))
                .andExpect(status().isNoContent());
    }
}
