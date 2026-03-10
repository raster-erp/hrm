package com.raster.hrm.shift;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.shift.controller.ShiftController;
import com.raster.hrm.shift.dto.ShiftRequest;
import com.raster.hrm.shift.dto.ShiftResponse;
import com.raster.hrm.shift.entity.ShiftType;
import com.raster.hrm.shift.service.ShiftService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

@WebMvcTest(ShiftController.class)
class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShiftService shiftService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/shifts";

    private ShiftResponse createResponse(Long id, String name, String type) {
        return new ShiftResponse(
                id, name, type,
                LocalTime.of(9, 0), LocalTime.of(17, 0),
                60, 15, "Test shift", true,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private ShiftRequest createRequest() {
        return new ShiftRequest(
                "Morning Shift",
                "MORNING",
                LocalTime.of(6, 0),
                LocalTime.of(14, 0),
                30, 10,
                "Morning shift"
        );
    }

    @Test
    void getAll_shouldReturnPageOfShifts() throws Exception {
        var shifts = List.of(
                createResponse(1L, "Morning", "MORNING"),
                createResponse(2L, "Evening", "EVENING")
        );
        var page = new PageImpl<>(shifts, PageRequest.of(0, 20), 2);
        when(shiftService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Morning"))
                .andExpect(jsonPath("$.content[1].name").value("Evening"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnShift() throws Exception {
        var response = createResponse(1L, "Morning", "MORNING");
        when(shiftService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Morning"))
                .andExpect(jsonPath("$.type").value("MORNING"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(shiftService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Shift", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByType_shouldReturnShifts() throws Exception {
        var shifts = List.of(createResponse(1L, "Morning", "MORNING"));
        when(shiftService.getByType(ShiftType.MORNING)).thenReturn(shifts);

        mockMvc.perform(get(BASE_URL + "/type/MORNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("MORNING"));
    }

    @Test
    void getActive_shouldReturnActiveShifts() throws Exception {
        var shifts = List.of(createResponse(1L, "Morning", "MORNING"));
        when(shiftService.getActive()).thenReturn(shifts);

        mockMvc.perform(get(BASE_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void create_shouldReturn201WithCreatedShift() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "Morning Shift", "MORNING");
        when(shiftService.create(any(ShiftRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Morning Shift"));
    }

    @Test
    void create_shouldReturn400WhenNameExists() throws Exception {
        var request = createRequest();
        when(shiftService.create(any(ShiftRequest.class)))
                .thenThrow(new BadRequestException("Shift with name 'Morning Shift' already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new ShiftRequest("", null, null, null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedShift() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "Morning Shift", "MORNING");
        when(shiftService.update(eq(1L), any(ShiftRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Morning Shift"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(shiftService.update(eq(999L), any(ShiftRequest.class)))
                .thenThrow(new ResourceNotFoundException("Shift", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateActive_shouldReturnUpdatedShift() throws Exception {
        var response = new ShiftResponse(1L, "Morning", "MORNING",
                LocalTime.of(9, 0), LocalTime.of(17, 0),
                60, 15, "Test", false,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0));
        when(shiftService.updateActive(eq(1L), eq(false))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateActive_shouldReturn404WhenNotFound() throws Exception {
        when(shiftService.updateActive(eq(999L), eq(false)))
                .thenThrow(new ResourceNotFoundException("Shift", "id", 999L));

        mockMvc.perform(patch(BASE_URL + "/999/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(shiftService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(shiftService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Shift", "id", 999L))
                .when(shiftService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
