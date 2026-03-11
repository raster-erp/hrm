package com.raster.hrm.leavetype;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leavetype.controller.LeaveTypeController;
import com.raster.hrm.leavetype.dto.LeaveTypeRequest;
import com.raster.hrm.leavetype.dto.LeaveTypeResponse;
import com.raster.hrm.leavetype.entity.LeaveTypeCategory;
import com.raster.hrm.leavetype.service.LeaveTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(LeaveTypeController.class)
class LeaveTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaveTypeService leaveTypeService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/leave-types";

    private LeaveTypeResponse createResponse(Long id, String code, String name, String category) {
        return new LeaveTypeResponse(
                id, code, name, category,
                "Test leave type", true,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private LeaveTypeRequest createRequest() {
        return new LeaveTypeRequest(
                "AL",
                "Annual Leave",
                "PAID",
                "Standard annual leave"
        );
    }

    @Test
    void getAll_shouldReturnPageOfLeaveTypes() throws Exception {
        var leaveTypes = List.of(
                createResponse(1L, "AL", "Annual Leave", "PAID"),
                createResponse(2L, "SL", "Sick Leave", "PAID")
        );
        var page = new PageImpl<>(leaveTypes, PageRequest.of(0, 20), 2);
        when(leaveTypeService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Annual Leave"))
                .andExpect(jsonPath("$.content[1].name").value("Sick Leave"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnLeaveType() throws Exception {
        var response = createResponse(1L, "AL", "Annual Leave", "PAID");
        when(leaveTypeService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("AL"))
                .andExpect(jsonPath("$.name").value("Annual Leave"))
                .andExpect(jsonPath("$.category").value("PAID"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(leaveTypeService.getById(999L))
                .thenThrow(new ResourceNotFoundException("LeaveType", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByCategory_shouldReturnLeaveTypes() throws Exception {
        var leaveTypes = List.of(createResponse(1L, "AL", "Annual Leave", "PAID"));
        when(leaveTypeService.getByCategory(LeaveTypeCategory.PAID)).thenReturn(leaveTypes);

        mockMvc.perform(get(BASE_URL + "/category/PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("PAID"));
    }

    @Test
    void getActive_shouldReturnActiveLeaveTypes() throws Exception {
        var leaveTypes = List.of(createResponse(1L, "AL", "Annual Leave", "PAID"));
        when(leaveTypeService.getActive()).thenReturn(leaveTypes);

        mockMvc.perform(get(BASE_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void create_shouldReturn201WithCreatedLeaveType() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "AL", "Annual Leave", "PAID");
        when(leaveTypeService.create(any(LeaveTypeRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("AL"))
                .andExpect(jsonPath("$.name").value("Annual Leave"));
    }

    @Test
    void create_shouldReturn400WhenCodeExists() throws Exception {
        var request = createRequest();
        when(leaveTypeService.create(any(LeaveTypeRequest.class)))
                .thenThrow(new BadRequestException("Leave type with code 'AL' already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new LeaveTypeRequest("", null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedLeaveType() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "AL", "Annual Leave", "PAID");
        when(leaveTypeService.update(eq(1L), any(LeaveTypeRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Annual Leave"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(leaveTypeService.update(eq(999L), any(LeaveTypeRequest.class)))
                .thenThrow(new ResourceNotFoundException("LeaveType", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateActive_shouldReturnUpdatedLeaveType() throws Exception {
        var response = new LeaveTypeResponse(1L, "AL", "Annual Leave", "PAID",
                "Test", false,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0));
        when(leaveTypeService.updateActive(eq(1L), eq(false))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateActive_shouldReturn404WhenNotFound() throws Exception {
        when(leaveTypeService.updateActive(eq(999L), eq(false)))
                .thenThrow(new ResourceNotFoundException("LeaveType", "id", 999L));

        mockMvc.perform(patch(BASE_URL + "/999/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(leaveTypeService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(leaveTypeService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("LeaveType", "id", 999L))
                .when(leaveTypeService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
