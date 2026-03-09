package com.raster.hrm.designation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.designation.controller.DesignationController;
import com.raster.hrm.designation.dto.DesignationRequest;
import com.raster.hrm.designation.dto.DesignationResponse;
import com.raster.hrm.designation.service.DesignationService;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DesignationController.class)
class DesignationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DesignationService designationService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/designations";

    @Test
    void getAll_shouldReturnListOfDesignations() throws Exception {
        var designations = List.of(
                new DesignationResponse(1L, "Software Engineer", "SE", 3, "A", 1L, "Engineering", "SE role", true),
                new DesignationResponse(2L, "Senior Engineer", "SSE", 4, "B", 1L, "Engineering", "SSE role", true)
        );
        when(designationService.getAll()).thenReturn(designations);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Software Engineer"))
                .andExpect(jsonPath("$[1].title").value("Senior Engineer"));
    }

    @Test
    void getAll_shouldReturnEmptyList() throws Exception {
        when(designationService.getAll()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getById_shouldReturnDesignation() throws Exception {
        var response = new DesignationResponse(1L, "Software Engineer", "SE", 3, "A", 1L, "Engineering", "SE role", true);
        when(designationService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Software Engineer"))
                .andExpect(jsonPath("$.code").value("SE"))
                .andExpect(jsonPath("$.level").value(3))
                .andExpect(jsonPath("$.grade").value("A"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(designationService.getById(999L)).thenThrow(new ResourceNotFoundException("Designation", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByDepartmentId_shouldReturnDesignations() throws Exception {
        var designations = List.of(
                new DesignationResponse(1L, "Software Engineer", "SE", 3, "A", 1L, "Engineering", null, true)
        );
        when(designationService.getByDepartmentId(1L)).thenReturn(designations);

        mockMvc.perform(get(BASE_URL + "/department/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].departmentId").value(1));
    }

    @Test
    void create_shouldReturn201WithCreatedDesignation() throws Exception {
        var request = new DesignationRequest("Software Engineer", "SE", 3, "A", 1L, "SE role", true);
        var response = new DesignationResponse(1L, "Software Engineer", "SE", 3, "A", 1L, "Engineering", "SE role", true);
        when(designationService.create(any(DesignationRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Software Engineer"));
    }

    @Test
    void create_shouldReturn400WhenTitleIsBlank() throws Exception {
        var request = new DesignationRequest("", "SE", 3, "A", 1L, null, true);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenCodeIsBlank() throws Exception {
        var request = new DesignationRequest("Software Engineer", "", 3, "A", 1L, null, true);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenTitleIsNull() throws Exception {
        var request = new DesignationRequest(null, "SE", 3, "A", 1L, null, true);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenDuplicateCode() throws Exception {
        var request = new DesignationRequest("Software Engineer", "SE", 3, "A", 1L, null, true);
        when(designationService.create(any(DesignationRequest.class)))
                .thenThrow(new BadRequestException("Designation with code 'SE' already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200WithUpdatedDesignation() throws Exception {
        var request = new DesignationRequest("Senior Engineer", "SE", 4, "B", 1L, "Updated", true);
        var response = new DesignationResponse(1L, "Senior Engineer", "SE", 4, "B", 1L, "Engineering", "Updated", true);
        when(designationService.update(eq(1L), any(DesignationRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Senior Engineer"))
                .andExpect(jsonPath("$.level").value(4));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = new DesignationRequest("Software Engineer", "SE", 3, "A", 1L, null, true);
        when(designationService.update(eq(999L), any(DesignationRequest.class)))
                .thenThrow(new ResourceNotFoundException("Designation", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn400WhenValidationFails() throws Exception {
        var request = new DesignationRequest("", "", null, null, null, null, null);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(designationService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Designation", "id", 999L))
                .when(designationService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn400WhenDesignationHasEmployees() throws Exception {
        doThrow(new BadRequestException("Cannot delete designation with existing employees"))
                .when(designationService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isBadRequest());
    }
}
