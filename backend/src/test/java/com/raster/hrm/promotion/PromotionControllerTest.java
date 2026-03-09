package com.raster.hrm.promotion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.promotion.controller.PromotionController;
import com.raster.hrm.promotion.dto.PromotionRequest;
import com.raster.hrm.promotion.dto.PromotionResponse;
import com.raster.hrm.promotion.service.PromotionService;
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

@WebMvcTest(PromotionController.class)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PromotionService promotionService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/promotions";

    private PromotionResponse createResponse(Long id, String status) {
        return new PromotionResponse(
                id, 1L, "EMP001", "John Doe",
                1L, "Software Engineer",
                2L, "Senior Software Engineer",
                "G5", "G6",
                LocalDate.of(2024, 6, 1),
                status, "Outstanding performance",
                null, null, null,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private PromotionResponse createApprovedResponse(Long id) {
        return new PromotionResponse(
                id, 1L, "EMP001", "John Doe",
                1L, "Software Engineer",
                2L, "Senior Software Engineer",
                "G5", "G6",
                LocalDate.of(2024, 6, 1),
                "APPROVED", "Outstanding performance",
                2L, "Jane Smith",
                LocalDateTime.of(2024, 2, 1, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 2, 1, 10, 0)
        );
    }

    private PromotionRequest createRequest() {
        return new PromotionRequest(
                1L, 1L, 2L,
                "G5", "G6",
                LocalDate.of(2024, 6, 1),
                "Outstanding performance"
        );
    }

    @Test
    void getAll_shouldReturnPageOfPromotions() throws Exception {
        var promotions = List.of(
                createResponse(1L, "PENDING"),
                createResponse(2L, "APPROVED")
        );
        var page = new PageImpl<>(promotions, PageRequest.of(0, 20), 2);
        when(promotionService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[1].status").value("APPROVED"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnPromotion() throws Exception {
        var response = createResponse(1L, "PENDING");
        when(promotionService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.oldDesignationTitle").value("Software Engineer"))
                .andExpect(jsonPath("$.newDesignationTitle").value("Senior Software Engineer"))
                .andExpect(jsonPath("$.oldGrade").value("G5"))
                .andExpect(jsonPath("$.newGrade").value("G6"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(promotionService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Promotion", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturnPromotions() throws Exception {
        var promotions = List.of(
                createResponse(1L, "PENDING"),
                createResponse(2L, "EXECUTED")
        );
        when(promotionService.getByEmployeeId(1L)).thenReturn(promotions);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employeeName").value("John Doe"));
    }

    @Test
    void getByEmployee_shouldReturnEmptyList() throws Exception {
        when(promotionService.getByEmployeeId(1L)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getPending_shouldReturnPendingPromotions() throws Exception {
        var promotions = List.of(createResponse(1L, "PENDING"));
        when(promotionService.getPendingPromotions()).thenReturn(promotions);

        mockMvc.perform(get(BASE_URL + "/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getPending_shouldReturnEmptyList() throws Exception {
        when(promotionService.getPendingPromotions()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void create_shouldReturn201WithCreatedPromotion() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "PENDING");
        when(promotionService.create(any(PromotionRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.oldGrade").value("G5"))
                .andExpect(jsonPath("$.newGrade").value("G6"));
    }

    @Test
    void create_shouldReturn400WhenEmployeeIdNull() throws Exception {
        var request = new PromotionRequest(
                null, 1L, 2L,
                "G5", "G6",
                LocalDate.of(2024, 6, 1),
                "Reason"
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenEffectiveDateNull() throws Exception {
        var request = new PromotionRequest(
                1L, 1L, 2L,
                "G5", "G6",
                null,
                "Reason"
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn404WhenEmployeeNotFound() throws Exception {
        var request = createRequest();
        when(promotionService.create(any(PromotionRequest.class)))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 1L));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void approve_shouldReturnApprovedPromotion() throws Exception {
        var response = createApprovedResponse(1L);
        when(promotionService.approve(eq(1L), eq(2L))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/approve")
                        .param("approvedById", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedById").value(2))
                .andExpect(jsonPath("$.approvedByName").value("Jane Smith"));
    }

    @Test
    void approve_shouldReturn404WhenPromotionNotFound() throws Exception {
        when(promotionService.approve(eq(999L), eq(2L)))
                .thenThrow(new ResourceNotFoundException("Promotion", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999/approve")
                        .param("approvedById", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void approve_shouldReturn400WhenNotPending() throws Exception {
        when(promotionService.approve(eq(1L), eq(2L)))
                .thenThrow(new BadRequestException("Promotion can only be approved when in PENDING status"));

        mockMvc.perform(put(BASE_URL + "/1/approve")
                        .param("approvedById", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reject_shouldReturnRejectedPromotion() throws Exception {
        var response = new PromotionResponse(
                1L, 1L, "EMP001", "John Doe",
                1L, "Software Engineer",
                2L, "Senior Software Engineer",
                "G5", "G6",
                LocalDate.of(2024, 6, 1),
                "REJECTED", "Outstanding performance",
                2L, "Jane Smith",
                LocalDateTime.of(2024, 2, 1, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 2, 1, 10, 0)
        );
        when(promotionService.reject(eq(1L), eq(2L))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/reject")
                        .param("approvedById", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.approvedById").value(2));
    }

    @Test
    void reject_shouldReturn404WhenPromotionNotFound() throws Exception {
        when(promotionService.reject(eq(999L), eq(2L)))
                .thenThrow(new ResourceNotFoundException("Promotion", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999/reject")
                        .param("approvedById", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reject_shouldReturn400WhenNotPending() throws Exception {
        when(promotionService.reject(eq(1L), eq(2L)))
                .thenThrow(new BadRequestException("Promotion can only be rejected when in PENDING status"));

        mockMvc.perform(put(BASE_URL + "/1/reject")
                        .param("approvedById", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void execute_shouldReturnExecutedPromotion() throws Exception {
        var response = new PromotionResponse(
                1L, 1L, "EMP001", "John Doe",
                1L, "Software Engineer",
                2L, "Senior Software Engineer",
                "G5", "G6",
                LocalDate.of(2024, 6, 1),
                "EXECUTED", "Outstanding performance",
                2L, "Jane Smith",
                LocalDateTime.of(2024, 2, 1, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 2, 1, 10, 0)
        );
        when(promotionService.execute(1L)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXECUTED"));
    }

    @Test
    void execute_shouldReturn404WhenPromotionNotFound() throws Exception {
        when(promotionService.execute(999L))
                .thenThrow(new ResourceNotFoundException("Promotion", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999/execute"))
                .andExpect(status().isNotFound());
    }

    @Test
    void execute_shouldReturn400WhenNotApproved() throws Exception {
        when(promotionService.execute(1L))
                .thenThrow(new BadRequestException("Promotion must be in APPROVED status before execution"));

        mockMvc.perform(put(BASE_URL + "/1/execute"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(promotionService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(promotionService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Promotion", "id", 999L))
                .when(promotionService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn400WhenNotPending() throws Exception {
        doThrow(new BadRequestException("Only promotions in PENDING status can be deleted"))
                .when(promotionService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isBadRequest());
    }
}
