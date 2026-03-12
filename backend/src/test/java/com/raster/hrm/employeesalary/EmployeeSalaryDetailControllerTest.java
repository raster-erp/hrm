package com.raster.hrm.employeesalary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.employeesalary.controller.EmployeeSalaryDetailController;
import com.raster.hrm.employeesalary.dto.EmployeeSalaryDetailRequest;
import com.raster.hrm.employeesalary.dto.EmployeeSalaryDetailResponse;
import com.raster.hrm.employeesalary.dto.SalaryRevisionRequest;
import com.raster.hrm.employeesalary.service.EmployeeSalaryDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeSalaryDetailController.class)
class EmployeeSalaryDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeSalaryDetailService employeeSalaryDetailService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/employee-salary-details";

    private EmployeeSalaryDetailResponse createResponse(Long id) {
        return new EmployeeSalaryDetailResponse(
                id, 1L, "John Doe", "EMP001",
                1L, "Standard",
                new BigDecimal("1200000.00"), new BigDecimal("50000.00"),
                LocalDate.of(2024, 4, 1), "Initial",
                true,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    @Test
    void getAll_shouldReturnPageOfDetails() throws Exception {
        var details = List.of(createResponse(1L));
        var page = new PageImpl<>(details, PageRequest.of(0, 20), 1);
        when(employeeSalaryDetailService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getById_shouldReturnDetail() throws Exception {
        var response = createResponse(1L);
        when(employeeSalaryDetailService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.ctc").value(1200000.00));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(employeeSalaryDetailService.getById(999L))
                .thenThrow(new ResourceNotFoundException("EmployeeSalaryDetail", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployeeId_shouldReturnDetails() throws Exception {
        var details = List.of(createResponse(1L));
        when(employeeSalaryDetailService.getByEmployeeId(1L)).thenReturn(details);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeName").value("John Doe"));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var request = new EmployeeSalaryDetailRequest(1L, 1L,
                new BigDecimal("1200000.00"), new BigDecimal("50000.00"),
                LocalDate.of(2024, 4, 1), "Initial");
        var response = createResponse(1L);
        when(employeeSalaryDetailService.create(any(EmployeeSalaryDetailRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new EmployeeSalaryDetailRequest(null, null, null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void revise_shouldReturn201() throws Exception {
        var request = new SalaryRevisionRequest(1L, new BigDecimal("1500000.00"),
                new BigDecimal("60000.00"), LocalDate.of(2025, 4, 1), "Promotion");
        var response = new EmployeeSalaryDetailResponse(
                2L, 1L, "John Doe", "EMP001", 1L, "Standard",
                new BigDecimal("1500000.00"), new BigDecimal("60000.00"),
                LocalDate.of(2025, 4, 1), "Promotion", true,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0));
        when(employeeSalaryDetailService.revise(eq(1L), any(SalaryRevisionRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/employee/1/revise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ctc").value(1500000.00))
                .andExpect(jsonPath("$.basicSalary").value(60000.00));
    }

    @Test
    void revise_shouldReturn404WhenEmployeeNotFound() throws Exception {
        var request = new SalaryRevisionRequest(1L, new BigDecimal("1500000.00"),
                new BigDecimal("60000.00"), LocalDate.of(2025, 4, 1), null);
        when(employeeSalaryDetailService.revise(eq(999L), any(SalaryRevisionRequest.class)))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        mockMvc.perform(post(BASE_URL + "/employee/999/revise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(employeeSalaryDetailService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(employeeSalaryDetailService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("EmployeeSalaryDetail", "id", 999L))
                .when(employeeSalaryDetailService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
