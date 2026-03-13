package com.raster.hrm.salarystructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.salarystructure.controller.SalaryStructureController;
import com.raster.hrm.salarystructure.dto.SalaryStructureCloneRequest;
import com.raster.hrm.salarystructure.dto.SalaryStructureRequest;
import com.raster.hrm.salarystructure.dto.SalaryStructureResponse;
import com.raster.hrm.salarystructure.service.SalaryStructureService;
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

@WebMvcTest(SalaryStructureController.class)
class SalaryStructureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalaryStructureService salaryStructureService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/salary-structures";

    private SalaryStructureResponse createResponse(Long id, String code, String name) {
        return new SalaryStructureResponse(
                id, code, name, "Test structure", true, List.of(),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    @Test
    void getAll_shouldReturnPageOfStructures() throws Exception {
        var structures = List.of(
                createResponse(1L, "STD", "Standard"),
                createResponse(2L, "MGR", "Manager")
        );
        var page = new PageImpl<>(structures, PageRequest.of(0, 20), 2);
        when(salaryStructureService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Standard"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnStructure() throws Exception {
        var response = createResponse(1L, "STD", "Standard");
        when(salaryStructureService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("STD"))
                .andExpect(jsonPath("$.name").value("Standard"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(salaryStructureService.getById(999L))
                .thenThrow(new ResourceNotFoundException("SalaryStructure", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getActive_shouldReturnActiveStructures() throws Exception {
        var structures = List.of(createResponse(1L, "STD", "Standard"));
        when(salaryStructureService.getActive()).thenReturn(structures);

        mockMvc.perform(get(BASE_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var request = new SalaryStructureRequest("STD", "Standard", "Test", null);
        var response = createResponse(1L, "STD", "Standard");
        when(salaryStructureService.create(any(SalaryStructureRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("STD"));
    }

    @Test
    void create_shouldReturn400WhenCodeExists() throws Exception {
        var request = new SalaryStructureRequest("STD", "Standard", "Test", null);
        when(salaryStructureService.create(any(SalaryStructureRequest.class)))
                .thenThrow(new BadRequestException("already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new SalaryStructureRequest("", null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedStructure() throws Exception {
        var request = new SalaryStructureRequest("STD", "Updated Standard", "Updated", null);
        var response = createResponse(1L, "STD", "Updated Standard");
        when(salaryStructureService.update(eq(1L), any(SalaryStructureRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Standard"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = new SalaryStructureRequest("STD", "Standard", "Test", null);
        when(salaryStructureService.update(eq(999L), any(SalaryStructureRequest.class)))
                .thenThrow(new ResourceNotFoundException("SalaryStructure", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void clone_shouldReturn201() throws Exception {
        var request = new SalaryStructureCloneRequest("STD2", "Standard Copy");
        var response = createResponse(2L, "STD2", "Standard Copy");
        when(salaryStructureService.clone(eq(1L), eq("STD2"), eq("Standard Copy"))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/1/clone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("STD2"))
                .andExpect(jsonPath("$.name").value("Standard Copy"));
    }

    @Test
    void clone_shouldReturn404WhenSourceNotFound() throws Exception {
        var request = new SalaryStructureCloneRequest("STD2", "Standard Copy");
        when(salaryStructureService.clone(eq(999L), eq("STD2"), eq("Standard Copy")))
                .thenThrow(new ResourceNotFoundException("SalaryStructure", "id", 999L));

        mockMvc.perform(post(BASE_URL + "/999/clone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateActive_shouldReturnUpdatedStructure() throws Exception {
        var response = new SalaryStructureResponse(1L, "STD", "Standard",
                "Test", false, List.of(),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0));
        when(salaryStructureService.updateActive(eq(1L), eq(false))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(salaryStructureService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(salaryStructureService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("SalaryStructure", "id", 999L))
                .when(salaryStructureService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
