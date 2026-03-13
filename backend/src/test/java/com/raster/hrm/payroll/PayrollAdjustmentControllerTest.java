package com.raster.hrm.payroll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.payroll.controller.PayrollAdjustmentController;
import com.raster.hrm.payroll.dto.PayrollAdjustmentRequest;
import com.raster.hrm.payroll.dto.PayrollAdjustmentResponse;
import com.raster.hrm.payroll.service.PayrollAdjustmentService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayrollAdjustmentController.class)
class PayrollAdjustmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PayrollAdjustmentService payrollAdjustmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/payroll-adjustments";

    private PayrollAdjustmentResponse createResponse(Long id) {
        return new PayrollAdjustmentResponse(
                id, 1L, 1L, "John Doe", "EMP001",
                "ADDITION", "Bonus", new BigDecimal("5000.00"),
                "Performance bonus",
                LocalDateTime.of(2024, 6, 30, 10, 0)
        );
    }

    @Test
    void getByRunId_shouldReturnAdjustments() throws Exception {
        var adjustments = List.of(createResponse(1L));
        when(payrollAdjustmentService.getByRunId(1L)).thenReturn(adjustments);

        mockMvc.perform(get(BASE_URL + "/run/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].componentName").value("Bonus"))
                .andExpect(jsonPath("$[0].adjustmentType").value("ADDITION"));
    }

    @Test
    void getByRunId_shouldReturn404WhenRunNotFound() throws Exception {
        when(payrollAdjustmentService.getByRunId(99L))
                .thenThrow(new ResourceNotFoundException("PayrollRun", "id", 99L));

        mockMvc.perform(get(BASE_URL + "/run/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var request = new PayrollAdjustmentRequest(1L, 1L, "ADDITION", "Bonus",
                new BigDecimal("5000.00"), "Performance bonus");
        var response = createResponse(1L);
        when(payrollAdjustmentService.create(any(PayrollAdjustmentRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.componentName").value("Bonus"));
    }

    @Test
    void create_shouldReturn400ForMissingFields() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"payrollRunId\": 1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400ForInvalidRunStatus() throws Exception {
        var request = new PayrollAdjustmentRequest(1L, 1L, "ADDITION", "Bonus",
                new BigDecimal("5000.00"), null);
        when(payrollAdjustmentService.create(any(PayrollAdjustmentRequest.class)))
                .thenThrow(new BadRequestException("Run must be DRAFT or COMPUTED"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(payrollAdjustmentService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(payrollAdjustmentService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("PayrollAdjustment", "id", 99L))
                .when(payrollAdjustmentService).delete(99L);

        mockMvc.perform(delete(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn400ForInvalidRunStatus() throws Exception {
        doThrow(new BadRequestException("Run must be DRAFT or COMPUTED"))
                .when(payrollAdjustmentService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isBadRequest());
    }
}
