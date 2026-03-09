package com.raster.hrm.separation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.separation.controller.SeparationController;
import com.raster.hrm.separation.dto.SeparationRequest;
import com.raster.hrm.separation.dto.SeparationResponse;
import com.raster.hrm.separation.service.SeparationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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

@WebMvcTest(SeparationController.class)
class SeparationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SeparationService separationService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/separations";

    private SeparationResponse createResponse(Long id, String status) {
        return new SeparationResponse(
                id, 1L, "EMP001", "John Doe",
                "RESIGNATION", "Personal reasons",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1),
                status, null, null, null,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private SeparationResponse createApprovedResponse(Long id) {
        return new SeparationResponse(
                id, 1L, "EMP001", "John Doe",
                "RESIGNATION", "Personal reasons",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1),
                "APPROVED", 2L, "Jane Smith",
                LocalDateTime.of(2024, 2, 1, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 2, 1, 10, 0)
        );
    }

    private SeparationRequest createRequest() {
        return new SeparationRequest(
                1L, "RESIGNATION", "Personal reasons",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)
        );
    }

    @Test
    void getAll_shouldReturnPageOfSeparations() throws Exception {
        var separations = List.of(
                createResponse(1L, "PENDING"),
                createResponse(2L, "APPROVED")
        );
        var page = new PageImpl<>(separations, PageRequest.of(0, 20), 2);
        when(separationService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[1].status").value("APPROVED"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnSeparation() throws Exception {
        var response = createResponse(1L, "PENDING");
        when(separationService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.separationType").value("RESIGNATION"))
                .andExpect(jsonPath("$.reason").value("Personal reasons"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(separationService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Separation", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployeeId_shouldReturnSeparations() throws Exception {
        var separations = List.of(
                createResponse(1L, "PENDING"),
                createResponse(2L, "APPROVED")
        );
        when(separationService.getByEmployeeId(1L)).thenReturn(separations);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("APPROVED"));
    }

    @Test
    void getPendingSeparations_shouldReturnPendingList() throws Exception {
        var separations = List.of(createResponse(1L, "PENDING"));
        when(separationService.getPendingSeparations()).thenReturn(separations);

        mockMvc.perform(get(BASE_URL + "/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void create_shouldReturnCreatedSeparation() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "PENDING");
        when(separationService.create(any(SeparationRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.separationType").value("RESIGNATION"));
    }

    @Test
    void create_shouldReturn400WhenEmployeeIdIsNull() throws Exception {
        var request = new SeparationRequest(
                null, "RESIGNATION", "Reason",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenSeparationTypeIsBlank() throws Exception {
        var request = new SeparationRequest(
                1L, "", "Reason",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenNoticeDateIsNull() throws Exception {
        var request = new SeparationRequest(
                1L, "RESIGNATION", "Reason",
                null, LocalDate.of(2024, 2, 1)
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenLastWorkingDayIsNull() throws Exception {
        var request = new SeparationRequest(
                1L, "RESIGNATION", "Reason",
                LocalDate.of(2024, 1, 1), null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_shouldReturnApprovedSeparation() throws Exception {
        var response = createApprovedResponse(1L);
        when(separationService.approve(eq(1L), eq(2L))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/approve")
                        .param("approvedById", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedById").value(2))
                .andExpect(jsonPath("$.approvedByName").value("Jane Smith"));
    }

    @Test
    void approve_shouldReturn404WhenNotFound() throws Exception {
        when(separationService.approve(eq(999L), eq(2L)))
                .thenThrow(new ResourceNotFoundException("Separation", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999/approve")
                        .param("approvedById", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void approve_shouldReturn400WhenNotPending() throws Exception {
        when(separationService.approve(eq(1L), eq(2L)))
                .thenThrow(new BadRequestException("Separation can only be approved when in PENDING status"));

        mockMvc.perform(put(BASE_URL + "/1/approve")
                        .param("approvedById", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reject_shouldReturnRejectedSeparation() throws Exception {
        var response = new SeparationResponse(
                1L, 1L, "EMP001", "John Doe",
                "RESIGNATION", "Personal reasons",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1),
                "REJECTED", 2L, "Jane Smith",
                LocalDateTime.of(2024, 2, 1, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 2, 1, 10, 0)
        );
        when(separationService.reject(eq(1L), eq(2L))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/reject")
                        .param("approvedById", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.approvedById").value(2));
    }

    @Test
    void reject_shouldReturn400WhenNotPending() throws Exception {
        when(separationService.reject(eq(1L), eq(2L)))
                .thenThrow(new BadRequestException("Separation can only be rejected when in PENDING status"));

        mockMvc.perform(put(BASE_URL + "/1/reject")
                        .param("approvedById", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void finalizeSeparation_shouldReturnFinalizedSeparation() throws Exception {
        var response = new SeparationResponse(
                1L, 1L, "EMP001", "John Doe",
                "RESIGNATION", "Personal reasons",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1),
                "FINALIZED", 2L, "Jane Smith",
                LocalDateTime.of(2024, 2, 1, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 2, 1, 10, 0)
        );
        when(separationService.finalizeSeparation(1L)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/finalize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZED"));
    }

    @Test
    void finalizeSeparation_shouldReturn400WhenNotApproved() throws Exception {
        when(separationService.finalizeSeparation(1L))
                .thenThrow(new BadRequestException("Separation must be in APPROVED status before finalization"));

        mockMvc.perform(put(BASE_URL + "/1/finalize"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(separationService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(separationService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Separation", "id", 999L))
                .when(separationService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn400WhenNotPending() throws Exception {
        doThrow(new BadRequestException("Only separations in PENDING status can be deleted"))
                .when(separationService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isBadRequest());
    }
}
