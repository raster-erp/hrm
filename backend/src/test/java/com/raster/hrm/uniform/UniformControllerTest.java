package com.raster.hrm.uniform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.uniform.controller.UniformController;
import com.raster.hrm.uniform.dto.UniformRequest;
import com.raster.hrm.uniform.dto.UniformResponse;
import com.raster.hrm.uniform.service.UniformService;
import com.raster.hrm.exception.ResourceNotFoundException;
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

@WebMvcTest(UniformController.class)
class UniformControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UniformService uniformService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/uniforms";

    private UniformResponse createResponse(Long id, String name, String type) {
        return new UniformResponse(
                id, name, type, "M", "Standard uniform",
                true,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private UniformRequest createRequest() {
        return new UniformRequest(
                "Safety Vest", "PPE", "L", "High-visibility vest"
        );
    }

    @Test
    void getAll_shouldReturnPageOfUniforms() throws Exception {
        var uniforms = List.of(
                createResponse(1L, "Safety Vest", "PPE"),
                createResponse(2L, "Work Boots", "Footwear")
        );
        var page = new PageImpl<>(uniforms, PageRequest.of(0, 20), 2);
        when(uniformService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Safety Vest"))
                .andExpect(jsonPath("$.content[1].name").value("Work Boots"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnUniform() throws Exception {
        var response = createResponse(1L, "Safety Vest", "PPE");
        when(uniformService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Safety Vest"))
                .andExpect(jsonPath("$.type").value("PPE"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(uniformService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Uniform", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201WithCreatedUniform() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "Safety Vest", "PPE");
        when(uniformService.create(any(UniformRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Safety Vest"))
                .andExpect(jsonPath("$.type").value("PPE"));
    }

    @Test
    void create_shouldReturn400WhenNameBlank() throws Exception {
        var request = new UniformRequest("", "PPE", "M", null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenTypeBlank() throws Exception {
        var request = new UniformRequest("Safety Vest", "", "M", null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenNameNull() throws Exception {
        var request = new UniformRequest(null, "PPE", "M", null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedUniform() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "Safety Vest", "PPE");
        when(uniformService.update(eq(1L), any(UniformRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Safety Vest"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(uniformService.update(eq(999L), any(UniformRequest.class)))
                .thenThrow(new ResourceNotFoundException("Uniform", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn400WhenValidationFails() throws Exception {
        var request = new UniformRequest("", "", null, null);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deactivate_shouldReturn204() throws Exception {
        doNothing().when(uniformService).deactivate(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(uniformService).deactivate(1L);
    }

    @Test
    void deactivate_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Uniform", "id", 999L))
                .when(uniformService).deactivate(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
