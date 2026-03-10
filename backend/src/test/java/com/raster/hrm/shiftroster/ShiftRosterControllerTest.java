package com.raster.hrm.shiftroster;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.shiftroster.controller.ShiftRosterController;
import com.raster.hrm.shiftroster.dto.BulkShiftRosterRequest;
import com.raster.hrm.shiftroster.dto.ShiftRosterRequest;
import com.raster.hrm.shiftroster.dto.ShiftRosterResponse;
import com.raster.hrm.shiftroster.service.ShiftRosterService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShiftRosterController.class)
class ShiftRosterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShiftRosterService shiftRosterService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/shift-rosters";

    private ShiftRosterResponse createResponse(Long id, Long employeeId, Long shiftId) {
        return new ShiftRosterResponse(
                id, employeeId, "John Doe", "EMP-001",
                shiftId, "Morning Shift",
                LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 31),
                null, null,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private ShiftRosterRequest createRequest() {
        return new ShiftRosterRequest(
                1L, 1L, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 31), null
        );
    }

    @Test
    void getAll_shouldReturnPageOfRosters() throws Exception {
        var rosters = List.of(
                createResponse(1L, 1L, 1L),
                createResponse(2L, 2L, 1L)
        );
        var page = new PageImpl<>(rosters, PageRequest.of(0, 20), 2);
        when(shiftRosterService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnRoster() throws Exception {
        var response = createResponse(1L, 1L, 1L);
        when(shiftRosterService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.shiftName").value("Morning Shift"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(shiftRosterService.getById(999L))
                .thenThrow(new ResourceNotFoundException("ShiftRoster", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployeeId_shouldReturnRosters() throws Exception {
        var rosters = List.of(createResponse(1L, 1L, 1L));
        when(shiftRosterService.getByEmployeeId(1L)).thenReturn(rosters);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeCode").value("EMP-001"));
    }

    @Test
    void create_shouldReturn201WithCreatedRoster() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, 1L, 1L);
        when(shiftRosterService.create(any(ShiftRosterRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void create_shouldReturn400WhenOverlapping() throws Exception {
        var request = createRequest();
        when(shiftRosterService.create(any(ShiftRosterRequest.class)))
                .thenThrow(new BadRequestException("overlapping shift roster"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new ShiftRosterRequest(null, null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bulkCreate_shouldReturn201WithCreatedRosters() throws Exception {
        var request = new BulkShiftRosterRequest(
                List.of(1L, 2L), 1L, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 31), null);
        var rosters = List.of(
                createResponse(1L, 1L, 1L),
                createResponse(2L, 2L, 1L)
        );
        when(shiftRosterService.bulkCreate(any(BulkShiftRosterRequest.class))).thenReturn(rosters);

        mockMvc.perform(post(BASE_URL + "/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void update_shouldReturnUpdatedRoster() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, 1L, 1L);
        when(shiftRosterService.update(eq(1L), any(ShiftRosterRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(shiftRosterService.update(eq(999L), any(ShiftRosterRequest.class)))
                .thenThrow(new ResourceNotFoundException("ShiftRoster", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(shiftRosterService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(shiftRosterService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("ShiftRoster", "id", 999L))
                .when(shiftRosterService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByDateRange_shouldReturnRosters() throws Exception {
        var rosters = List.of(createResponse(1L, 1L, 1L));
        when(shiftRosterService.getByDateRange(
                LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 7)))
                .thenReturn(rosters);

        mockMvc.perform(get(BASE_URL + "/date-range")
                        .param("startDate", "2024-07-01")
                        .param("endDate", "2024-07-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeName").value("John Doe"));
    }
}
