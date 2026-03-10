package com.raster.hrm.rotationpattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.rotationpattern.controller.RotationPatternController;
import com.raster.hrm.rotationpattern.dto.RotationPatternRequest;
import com.raster.hrm.rotationpattern.dto.RotationPatternResponse;
import com.raster.hrm.rotationpattern.service.RotationPatternService;
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

@WebMvcTest(RotationPatternController.class)
class RotationPatternControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RotationPatternService rotationPatternService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/rotation-patterns";

    private RotationPatternResponse createResponse(Long id, String name) {
        return new RotationPatternResponse(
                id, name, "Test rotation pattern", 7, "1,2,3,1,2,3,1",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private RotationPatternRequest createRequest() {
        return new RotationPatternRequest(
                "Weekly Rotation",
                "Rotates weekly",
                7,
                "1,2,3,1,2,3,1"
        );
    }

    @Test
    void getAll_shouldReturnPageOfPatterns() throws Exception {
        var patterns = List.of(
                createResponse(1L, "Weekly Rotation"),
                createResponse(2L, "Bi-Weekly Rotation")
        );
        var page = new PageImpl<>(patterns, PageRequest.of(0, 20), 2);
        when(rotationPatternService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Weekly Rotation"))
                .andExpect(jsonPath("$.content[1].name").value("Bi-Weekly Rotation"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnPattern() throws Exception {
        var response = createResponse(1L, "Weekly Rotation");
        when(rotationPatternService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Weekly Rotation"))
                .andExpect(jsonPath("$.rotationDays").value(7));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(rotationPatternService.getById(999L))
                .thenThrow(new ResourceNotFoundException("RotationPattern", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201WithCreatedPattern() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "Weekly Rotation");
        when(rotationPatternService.create(any(RotationPatternRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Weekly Rotation"));
    }

    @Test
    void create_shouldReturn400WhenNameExists() throws Exception {
        var request = createRequest();
        when(rotationPatternService.create(any(RotationPatternRequest.class)))
                .thenThrow(new BadRequestException("Rotation pattern with name 'Weekly Rotation' already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new RotationPatternRequest("", null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedPattern() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "Weekly Rotation");
        when(rotationPatternService.update(eq(1L), any(RotationPatternRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Weekly Rotation"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(rotationPatternService.update(eq(999L), any(RotationPatternRequest.class)))
                .thenThrow(new ResourceNotFoundException("RotationPattern", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(rotationPatternService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(rotationPatternService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("RotationPattern", "id", 999L))
                .when(rotationPatternService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
