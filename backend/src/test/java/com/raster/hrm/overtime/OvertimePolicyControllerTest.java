package com.raster.hrm.overtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.overtime.controller.OvertimePolicyController;
import com.raster.hrm.overtime.dto.OvertimePolicyRequest;
import com.raster.hrm.overtime.dto.OvertimePolicyResponse;
import com.raster.hrm.overtime.entity.OvertimePolicyType;
import com.raster.hrm.overtime.service.OvertimePolicyService;
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

@WebMvcTest(OvertimePolicyController.class)
class OvertimePolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OvertimePolicyService overtimePolicyService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/overtime-policies";

    private OvertimePolicyResponse createResponse(Long id, String name, String type) {
        return new OvertimePolicyResponse(
                id, name, type,
                new BigDecimal("1.50"),
                30, 480, 2400,
                true, true, "Test policy",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private OvertimePolicyRequest createRequest() {
        return new OvertimePolicyRequest(
                "Weekday Overtime",
                "WEEKDAY",
                new BigDecimal("1.50"),
                30, 480, 2400,
                true, "Weekday overtime policy"
        );
    }

    @Test
    void getAll_shouldReturnPageOfPolicies() throws Exception {
        var policies = List.of(
                createResponse(1L, "Weekday", "WEEKDAY"),
                createResponse(2L, "Weekend", "WEEKEND")
        );
        var page = new PageImpl<>(policies, PageRequest.of(0, 20), 2);
        when(overtimePolicyService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Weekday"))
                .andExpect(jsonPath("$.content[1].name").value("Weekend"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnPolicy() throws Exception {
        var response = createResponse(1L, "Weekday", "WEEKDAY");
        when(overtimePolicyService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Weekday"))
                .andExpect(jsonPath("$.type").value("WEEKDAY"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(overtimePolicyService.getById(999L))
                .thenThrow(new ResourceNotFoundException("OvertimePolicy", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByType_shouldReturnPolicies() throws Exception {
        var policies = List.of(createResponse(1L, "Weekday", "WEEKDAY"));
        when(overtimePolicyService.getByType(OvertimePolicyType.WEEKDAY)).thenReturn(policies);

        mockMvc.perform(get(BASE_URL + "/type/WEEKDAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("WEEKDAY"));
    }

    @Test
    void getActive_shouldReturnActivePolicies() throws Exception {
        var policies = List.of(createResponse(1L, "Weekday", "WEEKDAY"));
        when(overtimePolicyService.getActive()).thenReturn(policies);

        mockMvc.perform(get(BASE_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void create_shouldReturn201WithCreatedPolicy() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "Weekday Overtime", "WEEKDAY");
        when(overtimePolicyService.create(any(OvertimePolicyRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Weekday Overtime"));
    }

    @Test
    void create_shouldReturn400WhenNameExists() throws Exception {
        var request = createRequest();
        when(overtimePolicyService.create(any(OvertimePolicyRequest.class)))
                .thenThrow(new BadRequestException("Overtime policy with name 'Weekday Overtime' already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new OvertimePolicyRequest("", null, null, null, null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedPolicy() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "Weekday Overtime", "WEEKDAY");
        when(overtimePolicyService.update(eq(1L), any(OvertimePolicyRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Weekday Overtime"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(overtimePolicyService.update(eq(999L), any(OvertimePolicyRequest.class)))
                .thenThrow(new ResourceNotFoundException("OvertimePolicy", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateActive_shouldReturnUpdatedPolicy() throws Exception {
        var response = new OvertimePolicyResponse(1L, "Weekday", "WEEKDAY",
                new BigDecimal("1.50"), 30, 480, 2400,
                true, false, "Test",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0));
        when(overtimePolicyService.updateActive(eq(1L), eq(false))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateActive_shouldReturn404WhenNotFound() throws Exception {
        when(overtimePolicyService.updateActive(eq(999L), eq(false)))
                .thenThrow(new ResourceNotFoundException("OvertimePolicy", "id", 999L));

        mockMvc.perform(patch(BASE_URL + "/999/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(overtimePolicyService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(overtimePolicyService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("OvertimePolicy", "id", 999L))
                .when(overtimePolicyService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
