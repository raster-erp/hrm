package com.raster.hrm.payroll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.payroll.controller.PayrollRunController;
import com.raster.hrm.payroll.dto.PayrollDetailResponse;
import com.raster.hrm.payroll.dto.PayrollRunRequest;
import com.raster.hrm.payroll.dto.PayrollRunResponse;
import com.raster.hrm.payroll.service.PayrollRunService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayrollRunController.class)
class PayrollRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PayrollRunService payrollRunService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/payroll-runs";

    private PayrollRunResponse createRunResponse(Long id, String status) {
        return new PayrollRunResponse(
                id, 2024, 6, LocalDate.of(2024, 6, 30), status,
                new BigDecimal("150000.00"), new BigDecimal("18000.00"), new BigDecimal("132000.00"),
                2, "June payroll",
                LocalDateTime.of(2024, 6, 30, 10, 0),
                LocalDateTime.of(2024, 6, 30, 10, 0)
        );
    }

    private PayrollDetailResponse createDetailResponse(Long id) {
        return new PayrollDetailResponse(
                id, 1L, 1L, "John Doe", "EMP001",
                1L, "Standard",
                new BigDecimal("50000.00"), new BigDecimal("75000.00"),
                new BigDecimal("6000.00"), new BigDecimal("69000.00"),
                "[]", 30, 0,
                LocalDateTime.of(2024, 6, 30, 10, 0),
                LocalDateTime.of(2024, 6, 30, 10, 0)
        );
    }

    @Test
    void getAll_shouldReturnPageOfRuns() throws Exception {
        var runs = List.of(createRunResponse(1L, "DRAFT"));
        var page = new PageImpl<>(runs, PageRequest.of(0, 20), 1);
        when(payrollRunService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("DRAFT"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getById_shouldReturnRun() throws Exception {
        var response = createRunResponse(1L, "COMPUTED");
        when(payrollRunService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("COMPUTED"))
                .andExpect(jsonPath("$.periodYear").value(2024));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(payrollRunService.getById(99L)).thenThrow(new ResourceNotFoundException("PayrollRun", "id", 99L));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void initialize_shouldReturn201() throws Exception {
        var request = new PayrollRunRequest(2024, 6, "June payroll");
        var response = createRunResponse(1L, "DRAFT");
        when(payrollRunService.initialize(any(PayrollRunRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void initialize_shouldReturn400ForInvalidMonth() throws Exception {
        var request = new PayrollRunRequest(2024, 13, "Bad month");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initialize_shouldReturn400ForDuplicatePeriod() throws Exception {
        var request = new PayrollRunRequest(2024, 6, "June payroll");
        when(payrollRunService.initialize(any(PayrollRunRequest.class)))
                .thenThrow(new BadRequestException("Payroll run already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initialize_shouldReturn400ForMissingYear() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"periodMonth\": 6}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void computePayroll_shouldReturnComputedRun() throws Exception {
        var response = createRunResponse(1L, "COMPUTED");
        when(payrollRunService.computePayroll(1L)).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/1/compute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPUTED"))
                .andExpect(jsonPath("$.employeeCount").value(2));
    }

    @Test
    void computePayroll_shouldReturn400ForInvalidStatus() throws Exception {
        when(payrollRunService.computePayroll(1L))
                .thenThrow(new BadRequestException("Payroll run must be in DRAFT or COMPUTED status"));

        mockMvc.perform(post(BASE_URL + "/1/compute"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDetails_shouldReturnPageOfDetails() throws Exception {
        var details = List.of(createDetailResponse(1L));
        var page = new PageImpl<>(details, PageRequest.of(0, 20), 1);
        when(payrollRunService.getDetails(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].employeeName").value("John Doe"));
    }

    @Test
    void getDetailByEmployee_shouldReturnDetail() throws Exception {
        var detail = createDetailResponse(1L);
        when(payrollRunService.getDetailByEmployee(1L, 1L)).thenReturn(detail);

        mockMvc.perform(get(BASE_URL + "/1/details/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.grossSalary").value(75000.00));
    }

    @Test
    void getDetailByEmployee_shouldReturn404WhenNotFound() throws Exception {
        when(payrollRunService.getDetailByEmployee(1L, 99L))
                .thenThrow(new ResourceNotFoundException("PayrollDetail", "employeeId", 99L));

        mockMvc.perform(get(BASE_URL + "/1/details/employee/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void verify_shouldReturnVerifiedRun() throws Exception {
        var response = createRunResponse(1L, "VERIFIED");
        when(payrollRunService.verify(1L)).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/1/verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    @Test
    void verify_shouldReturn400ForInvalidTransition() throws Exception {
        when(payrollRunService.verify(1L))
                .thenThrow(new BadRequestException("Must be COMPUTED status"));

        mockMvc.perform(post(BASE_URL + "/1/verify"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void finalizeRun_shouldReturnFinalizedRun() throws Exception {
        var response = createRunResponse(1L, "FINALIZED");
        when(payrollRunService.finalizeRun(1L)).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/1/finalize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZED"));
    }

    @Test
    void finalizeRun_shouldReturn400ForInvalidTransition() throws Exception {
        when(payrollRunService.finalizeRun(1L))
                .thenThrow(new BadRequestException("Must be VERIFIED status"));

        mockMvc.perform(post(BASE_URL + "/1/finalize"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reverse_shouldReturnReversedRun() throws Exception {
        var response = createRunResponse(1L, "REVERSED");
        when(payrollRunService.reverse(1L)).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/1/reverse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVERSED"));
    }

    @Test
    void reverse_shouldReturn400ForInvalidTransition() throws Exception {
        when(payrollRunService.reverse(1L))
                .thenThrow(new BadRequestException("Must be COMPUTED or VERIFIED status"));

        mockMvc.perform(post(BASE_URL + "/1/reverse"))
                .andExpect(status().isBadRequest());
    }
}
