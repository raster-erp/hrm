package com.raster.hrm.separation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.separation.controller.NoDuesController;
import com.raster.hrm.separation.dto.NoDuesRequest;
import com.raster.hrm.separation.dto.NoDuesResponse;
import com.raster.hrm.separation.service.NoDuesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(NoDuesController.class)
class NoDuesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoDuesService noDuesService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/no-dues";

    private NoDuesResponse createResponse(Long id, boolean cleared) {
        return new NoDuesResponse(
                id, 1L, "Finance",
                cleared,
                cleared ? "Admin" : null,
                cleared ? LocalDateTime.of(2024, 2, 1, 10, 0) : null,
                new BigDecimal("500.00"),
                "Pending reimbursement",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private NoDuesRequest createRequest() {
        return new NoDuesRequest(1L, "Finance", new BigDecimal("500.00"), "Pending reimbursement");
    }

    @Test
    void getBySeparationId_shouldReturnNoDuesItems() throws Exception {
        var items = List.of(
                createResponse(1L, false),
                createResponse(2L, true)
        );
        when(noDuesService.getBySeparationId(1L)).thenReturn(items);

        mockMvc.perform(get(BASE_URL + "/separation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].department").value("Finance"))
                .andExpect(jsonPath("$[0].cleared").value(false))
                .andExpect(jsonPath("$[1].cleared").value(true));
    }

    @Test
    void getBySeparationId_shouldReturnEmptyList() throws Exception {
        when(noDuesService.getBySeparationId(999L)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/separation/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void create_shouldReturnCreatedRecord() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, false);
        when(noDuesService.create(any(NoDuesRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.separationId").value(1))
                .andExpect(jsonPath("$.department").value("Finance"))
                .andExpect(jsonPath("$.cleared").value(false))
                .andExpect(jsonPath("$.amountDue").value(500.00))
                .andExpect(jsonPath("$.notes").value("Pending reimbursement"));
    }

    @Test
    void create_shouldReturn400WhenDepartmentIsBlank() throws Exception {
        var request = new NoDuesRequest(1L, "", new BigDecimal("100.00"), "Notes");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenSeparationIdIsNull() throws Exception {
        var request = new NoDuesRequest(null, "Finance", new BigDecimal("100.00"), "Notes");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clearDepartment_shouldReturnClearedRecord() throws Exception {
        var response = createResponse(1L, true);
        when(noDuesService.clearDepartment(eq(1L), eq("Admin User"))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/clear")
                        .param("clearedBy", "Admin User"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cleared").value(true))
                .andExpect(jsonPath("$.clearedBy").value("Admin"));
    }

    @Test
    void clearDepartment_shouldReturn404WhenNotFound() throws Exception {
        when(noDuesService.clearDepartment(eq(999L), eq("Admin")))
                .thenThrow(new ResourceNotFoundException("NoDues", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999/clear")
                        .param("clearedBy", "Admin"))
                .andExpect(status().isNotFound());
    }

    @Test
    void clearDepartment_shouldReturn400WhenAlreadyCleared() throws Exception {
        when(noDuesService.clearDepartment(eq(1L), eq("Admin")))
                .thenThrow(new BadRequestException("No-dues record is already cleared"));

        mockMvc.perform(put(BASE_URL + "/1/clear")
                        .param("clearedBy", "Admin"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(noDuesService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(noDuesService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("NoDues", "id", 999L))
                .when(noDuesService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn400WhenCleared() throws Exception {
        doThrow(new BadRequestException("Cannot delete a cleared no-dues record"))
                .when(noDuesService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isBadRequest());
    }
}
