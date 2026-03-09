package com.raster.hrm.department;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.department.controller.DepartmentController;
import com.raster.hrm.department.dto.DepartmentRequest;
import com.raster.hrm.department.dto.DepartmentResponse;
import com.raster.hrm.department.service.DepartmentService;
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

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/departments";

    @Test
    void getAll_shouldReturnListOfDepartments() throws Exception {
        var departments = List.of(
                new DepartmentResponse(1L, "Engineering", "ENG", null, null, "Engineering dept", true, List.of()),
                new DepartmentResponse(2L, "Marketing", "MKT", null, null, "Marketing dept", true, List.of())
        );
        when(departmentService.getAll()).thenReturn(departments);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Engineering"))
                .andExpect(jsonPath("$[1].name").value("Marketing"));
    }

    @Test
    void getAll_shouldReturnEmptyList() throws Exception {
        when(departmentService.getAll()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getById_shouldReturnDepartment() throws Exception {
        var response = new DepartmentResponse(1L, "Engineering", "ENG", null, null, "Engineering dept", true, List.of());
        when(departmentService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Engineering"))
                .andExpect(jsonPath("$.code").value("ENG"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(departmentService.getById(999L)).thenThrow(new ResourceNotFoundException("Department", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRootDepartments_shouldReturnRootDepartments() throws Exception {
        var roots = List.of(
                new DepartmentResponse(1L, "Engineering", "ENG", null, null, null, true, List.of())
        );
        when(departmentService.getRootDepartments()).thenReturn(roots);

        mockMvc.perform(get(BASE_URL + "/root"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].parentId").isEmpty());
    }

    @Test
    void getByParentId_shouldReturnChildDepartments() throws Exception {
        var children = List.of(
                new DepartmentResponse(2L, "Frontend", "FE", 1L, "Engineering", null, true, List.of())
        );
        when(departmentService.getByParentId(1L)).thenReturn(children);

        mockMvc.perform(get(BASE_URL + "/parent/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].parentId").value(1));
    }

    @Test
    void create_shouldReturn201WithCreatedDepartment() throws Exception {
        var request = new DepartmentRequest("Engineering", "ENG", null, "Engineering dept", true);
        var response = new DepartmentResponse(1L, "Engineering", "ENG", null, null, "Engineering dept", true, List.of());
        when(departmentService.create(any(DepartmentRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Engineering"));
    }

    @Test
    void create_shouldReturn400WhenNameIsBlank() throws Exception {
        var request = new DepartmentRequest("", "ENG", null, null, true);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenCodeIsBlank() throws Exception {
        var request = new DepartmentRequest("Engineering", "", null, null, true);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenNameIsNull() throws Exception {
        var request = new DepartmentRequest(null, "ENG", null, null, true);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenDuplicateCode() throws Exception {
        var request = new DepartmentRequest("Engineering", "ENG", null, null, true);
        when(departmentService.create(any(DepartmentRequest.class)))
                .thenThrow(new BadRequestException("Department with code 'ENG' already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200WithUpdatedDepartment() throws Exception {
        var request = new DepartmentRequest("Engineering Updated", "ENG", null, "Updated desc", true);
        var response = new DepartmentResponse(1L, "Engineering Updated", "ENG", null, null, "Updated desc", true, List.of());
        when(departmentService.update(eq(1L), any(DepartmentRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Engineering Updated"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = new DepartmentRequest("Engineering", "ENG", null, null, true);
        when(departmentService.update(eq(999L), any(DepartmentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Department", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn400WhenValidationFails() throws Exception {
        var request = new DepartmentRequest("", "", null, null, true);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(departmentService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Department", "id", 999L))
                .when(departmentService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn400WhenDepartmentHasEmployees() throws Exception {
        doThrow(new BadRequestException("Cannot delete department with existing employees"))
                .when(departmentService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isBadRequest());
    }
}
