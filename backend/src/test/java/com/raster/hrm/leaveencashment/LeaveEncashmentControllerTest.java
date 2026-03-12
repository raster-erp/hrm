package com.raster.hrm.leaveencashment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.leaveencashment.controller.LeaveEncashmentController;
import com.raster.hrm.leaveencashment.dto.EncashmentEligibilityResponse;
import com.raster.hrm.leaveencashment.dto.LeaveEncashmentApprovalRequest;
import com.raster.hrm.leaveencashment.dto.LeaveEncashmentRequest;
import com.raster.hrm.leaveencashment.dto.LeaveEncashmentResponse;
import com.raster.hrm.leaveencashment.entity.EncashmentStatus;
import com.raster.hrm.leaveencashment.service.LeaveEncashmentService;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaveEncashmentController.class)
class LeaveEncashmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaveEncashmentService leaveEncashmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/leave-encashments";

    private LeaveEncashmentResponse createResponse() {
        return new LeaveEncashmentResponse(
                1L, 1L, "EMP001", "John Doe",
                1L, "Casual Leave",
                2025,
                new BigDecimal("5.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("5000.00"),
                "PENDING",
                null, null,
                "Test encashment",
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 1, 0, 0)
        );
    }

    private EncashmentEligibilityResponse createEligibilityResponse() {
        return new EncashmentEligibilityResponse(
                1L, "John Doe",
                1L, "Casual Leave",
                2025, true,
                new BigDecimal("15.00"),
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                new BigDecimal("1000.00"),
                "Eligible for encashment"
        );
    }

    @Test
    void checkEligibility_shouldReturn200() throws Exception {
        when(leaveEncashmentService.checkEligibility(1L, 1L, 2025))
                .thenReturn(createEligibilityResponse());

        mockMvc.perform(get(BASE_URL + "/eligibility")
                        .param("employeeId", "1")
                        .param("leaveTypeId", "1")
                        .param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.maxEncashableDays").value(10.00))
                .andExpect(jsonPath("$.perDaySalary").value(1000.00));
    }

    @Test
    void createRequest_shouldReturn201() throws Exception {
        var request = new LeaveEncashmentRequest(1L, 1L, new BigDecimal("5.00"), "Test");
        when(leaveEncashmentService.createRequest(any(LeaveEncashmentRequest.class)))
                .thenReturn(createResponse());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(5000.00));
    }

    @Test
    void createRequest_shouldReturn400_whenInvalid() throws Exception {
        var request = new LeaveEncashmentRequest(null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        var page = new PageImpl<>(List.of(createResponse()));
        when(leaveEncashmentService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(leaveEncashmentService.getById(1L)).thenReturn(createResponse());

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(leaveEncashmentService.getById(999L))
                .thenThrow(new ResourceNotFoundException("LeaveEncashment", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturn200() throws Exception {
        var page = new PageImpl<>(List.of(createResponse()));
        when(leaveEncashmentService.getByEmployee(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getByStatus_shouldReturn200() throws Exception {
        var page = new PageImpl<>(List.of(createResponse()));
        when(leaveEncashmentService.getByStatus(eq(EncashmentStatus.PENDING), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void approve_shouldReturn200() throws Exception {
        var request = new LeaveEncashmentApprovalRequest(EncashmentStatus.APPROVED, "Admin", "Approved");
        var response = createResponse();
        when(leaveEncashmentService.approve(eq(1L), any(LeaveEncashmentApprovalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void markAsPaid_shouldReturn200() throws Exception {
        var response = createResponse();
        when(leaveEncashmentService.markAsPaid(1L, "Finance")).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/pay")
                        .param("approvedBy", "Finance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void markAsPaid_shouldReturn200_withoutApprovedBy() throws Exception {
        var response = createResponse();
        when(leaveEncashmentService.markAsPaid(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/pay"))
                .andExpect(status().isOk());
    }
}
