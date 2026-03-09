package com.raster.hrm.separation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.separation.controller.ExitChecklistController;
import com.raster.hrm.separation.dto.ExitChecklistRequest;
import com.raster.hrm.separation.dto.ExitChecklistResponse;
import com.raster.hrm.separation.service.ExitChecklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(ExitChecklistController.class)
class ExitChecklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExitChecklistService exitChecklistService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/exit-checklists";

    private ExitChecklistResponse createResponse(Long id, boolean cleared) {
        return new ExitChecklistResponse(
                id, 1L, "Return laptop", "IT",
                cleared,
                cleared ? "Admin" : null,
                cleared ? LocalDateTime.of(2024, 2, 1, 10, 0) : null,
                "Company laptop",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private ExitChecklistRequest createRequest() {
        return new ExitChecklistRequest(1L, "Return laptop", "IT", "Company laptop");
    }

    @Test
    void getBySeparationId_shouldReturnChecklistItems() throws Exception {
        var items = List.of(
                createResponse(1L, false),
                createResponse(2L, true)
        );
        when(exitChecklistService.getBySeparationId(1L)).thenReturn(items);

        mockMvc.perform(get(BASE_URL + "/separation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].itemName").value("Return laptop"))
                .andExpect(jsonPath("$[0].department").value("IT"))
                .andExpect(jsonPath("$[0].cleared").value(false))
                .andExpect(jsonPath("$[1].cleared").value(true));
    }

    @Test
    void getBySeparationId_shouldReturnEmptyList() throws Exception {
        when(exitChecklistService.getBySeparationId(999L)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/separation/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void create_shouldReturnCreatedItem() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, false);
        when(exitChecklistService.create(any(ExitChecklistRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.separationId").value(1))
                .andExpect(jsonPath("$.itemName").value("Return laptop"))
                .andExpect(jsonPath("$.department").value("IT"))
                .andExpect(jsonPath("$.cleared").value(false))
                .andExpect(jsonPath("$.notes").value("Company laptop"));
    }

    @Test
    void create_shouldReturn400WhenItemNameIsBlank() throws Exception {
        var request = new ExitChecklistRequest(1L, "", "IT", "Notes");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenDepartmentIsBlank() throws Exception {
        var request = new ExitChecklistRequest(1L, "Return laptop", "", "Notes");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenSeparationIdIsNull() throws Exception {
        var request = new ExitChecklistRequest(null, "Return laptop", "IT", "Notes");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clearItem_shouldReturnClearedItem() throws Exception {
        var response = createResponse(1L, true);
        when(exitChecklistService.clearItem(eq(1L), eq("Admin User"))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/clear")
                        .param("clearedBy", "Admin User"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cleared").value(true))
                .andExpect(jsonPath("$.clearedBy").value("Admin"));
    }

    @Test
    void clearItem_shouldReturn404WhenNotFound() throws Exception {
        when(exitChecklistService.clearItem(eq(999L), eq("Admin")))
                .thenThrow(new ResourceNotFoundException("ExitChecklist", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999/clear")
                        .param("clearedBy", "Admin"))
                .andExpect(status().isNotFound());
    }

    @Test
    void clearItem_shouldReturn400WhenAlreadyCleared() throws Exception {
        when(exitChecklistService.clearItem(eq(1L), eq("Admin")))
                .thenThrow(new BadRequestException("Exit checklist item is already cleared"));

        mockMvc.perform(put(BASE_URL + "/1/clear")
                        .param("clearedBy", "Admin"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(exitChecklistService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(exitChecklistService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("ExitChecklist", "id", 999L))
                .when(exitChecklistService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn400WhenCleared() throws Exception {
        doThrow(new BadRequestException("Cannot delete a cleared exit checklist item"))
                .when(exitChecklistService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isBadRequest());
    }
}
