package com.raster.hrm.salarycomponent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.salarycomponent.controller.SalaryComponentController;
import com.raster.hrm.salarycomponent.dto.SalaryComponentRequest;
import com.raster.hrm.salarycomponent.dto.SalaryComponentResponse;
import com.raster.hrm.salarycomponent.entity.SalaryComponentType;
import com.raster.hrm.salarycomponent.service.SalaryComponentService;
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

@WebMvcTest(SalaryComponentController.class)
class SalaryComponentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalaryComponentService salaryComponentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/salary-components";

    private SalaryComponentResponse createResponse(Long id, String code, String name, String type) {
        return new SalaryComponentResponse(
                id, code, name, type, "FIXED",
                null, true, false, "Test component", true,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private SalaryComponentRequest createRequest() {
        return new SalaryComponentRequest(
                "BASIC", "Basic Salary", "EARNING", "FIXED",
                null, true, true, "Basic salary component"
        );
    }

    @Test
    void getAll_shouldReturnPageOfComponents() throws Exception {
        var components = List.of(
                createResponse(1L, "BASIC", "Basic Salary", "EARNING"),
                createResponse(2L, "HRA", "House Rent Allowance", "EARNING")
        );
        var page = new PageImpl<>(components, PageRequest.of(0, 20), 2);
        when(salaryComponentService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Basic Salary"))
                .andExpect(jsonPath("$.content[1].name").value("House Rent Allowance"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnComponent() throws Exception {
        var response = createResponse(1L, "BASIC", "Basic Salary", "EARNING");
        when(salaryComponentService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("BASIC"))
                .andExpect(jsonPath("$.name").value("Basic Salary"))
                .andExpect(jsonPath("$.type").value("EARNING"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(salaryComponentService.getById(999L))
                .thenThrow(new ResourceNotFoundException("SalaryComponent", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByType_shouldReturnComponents() throws Exception {
        var components = List.of(createResponse(1L, "BASIC", "Basic Salary", "EARNING"));
        when(salaryComponentService.getByType(SalaryComponentType.EARNING)).thenReturn(components);

        mockMvc.perform(get(BASE_URL + "/type/EARNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("EARNING"));
    }

    @Test
    void getActive_shouldReturnActiveComponents() throws Exception {
        var components = List.of(createResponse(1L, "BASIC", "Basic Salary", "EARNING"));
        when(salaryComponentService.getActive()).thenReturn(components);

        mockMvc.perform(get(BASE_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void create_shouldReturn201WithCreatedComponent() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "BASIC", "Basic Salary", "EARNING");
        when(salaryComponentService.create(any(SalaryComponentRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("BASIC"))
                .andExpect(jsonPath("$.name").value("Basic Salary"));
    }

    @Test
    void create_shouldReturn400WhenCodeExists() throws Exception {
        var request = createRequest();
        when(salaryComponentService.create(any(SalaryComponentRequest.class)))
                .thenThrow(new BadRequestException("Salary component with code 'BASIC' already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new SalaryComponentRequest("", null, null, null, null, false, false, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedComponent() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "BASIC", "Basic Salary", "EARNING");
        when(salaryComponentService.update(eq(1L), any(SalaryComponentRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Basic Salary"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(salaryComponentService.update(eq(999L), any(SalaryComponentRequest.class)))
                .thenThrow(new ResourceNotFoundException("SalaryComponent", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateActive_shouldReturnUpdatedComponent() throws Exception {
        var response = new SalaryComponentResponse(1L, "BASIC", "Basic Salary", "EARNING",
                "FIXED", null, true, false, "Test", false,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0));
        when(salaryComponentService.updateActive(eq(1L), eq(false))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateActive_shouldReturn404WhenNotFound() throws Exception {
        when(salaryComponentService.updateActive(eq(999L), eq(false)))
                .thenThrow(new ResourceNotFoundException("SalaryComponent", "id", 999L));

        mockMvc.perform(patch(BASE_URL + "/999/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(salaryComponentService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(salaryComponentService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("SalaryComponent", "id", 999L))
                .when(salaryComponentService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
