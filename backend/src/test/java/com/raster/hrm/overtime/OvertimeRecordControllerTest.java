package com.raster.hrm.overtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.overtime.controller.OvertimeRecordController;
import com.raster.hrm.overtime.dto.OvertimeApprovalRequest;
import com.raster.hrm.overtime.dto.OvertimeRecordRequest;
import com.raster.hrm.overtime.dto.OvertimeRecordResponse;
import com.raster.hrm.overtime.dto.OvertimeSummaryResponse;
import com.raster.hrm.overtime.entity.OvertimeStatus;
import com.raster.hrm.overtime.service.OvertimeRecordService;
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
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OvertimeRecordController.class)
class OvertimeRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OvertimeRecordService overtimeRecordService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/overtime-records";

    private OvertimeRecordResponse createResponse(Long id, Long employeeId, String employeeName) {
        return new OvertimeRecordResponse(
                id, employeeId, "EMP001", employeeName,
                LocalDate.of(2024, 1, 15),
                1L, "Weekday Overtime", "WEEKDAY",
                60, "PENDING", "MANUAL",
                LocalTime.of(9, 0), LocalTime.of(17, 0),
                LocalDateTime.of(2024, 1, 15, 8, 45),
                LocalDateTime.of(2024, 1, 15, 18, 0),
                "Test overtime",
                null, null,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private OvertimeRecordRequest createRequest() {
        return new OvertimeRecordRequest(
                1L,
                LocalDate.of(2024, 1, 15),
                1L,
                60,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                LocalDateTime.of(2024, 1, 15, 8, 45),
                LocalDateTime.of(2024, 1, 15, 18, 0),
                "Test overtime"
        );
    }

    @Test
    void getAll_shouldReturnPageOfRecords() throws Exception {
        var records = List.of(
                createResponse(1L, 1L, "John Doe"),
                createResponse(2L, 2L, "Jane Smith")
        );
        var page = new PageImpl<>(records, PageRequest.of(0, 20), 2);
        when(overtimeRecordService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.content[1].employeeName").value("Jane Smith"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnRecord() throws Exception {
        var response = createResponse(1L, 1L, "John Doe");
        when(overtimeRecordService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.overtimeMinutes").value(60));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(overtimeRecordService.getById(999L))
                .thenThrow(new ResourceNotFoundException("OvertimeRecord", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturnPage() throws Exception {
        var records = List.of(createResponse(1L, 1L, "John Doe"));
        var page = new PageImpl<>(records, PageRequest.of(0, 20), 1);
        when(overtimeRecordService.getByEmployeeId(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].employeeId").value(1));
    }

    @Test
    void getByStatus_shouldReturnPage() throws Exception {
        var records = List.of(createResponse(1L, 1L, "John Doe"));
        var page = new PageImpl<>(records, PageRequest.of(0, 20), 1);
        when(overtimeRecordService.getByStatus(eq(OvertimeStatus.PENDING), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getByDateRange_shouldReturnPage() throws Exception {
        var records = List.of(createResponse(1L, 1L, "John Doe"));
        var page = new PageImpl<>(records, PageRequest.of(0, 20), 1);
        when(overtimeRecordService.getByDateRange(
                eq(LocalDate.of(2024, 1, 1)),
                eq(LocalDate.of(2024, 1, 31)),
                any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void create_shouldReturn201WithCreatedRecord() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, 1L, "John Doe");
        when(overtimeRecordService.create(any(OvertimeRecordRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void update_shouldReturnUpdatedRecord() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, 1L, "John Doe");
        when(overtimeRecordService.update(eq(1L), any(OvertimeRecordRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(overtimeRecordService.update(eq(999L), any(OvertimeRecordRequest.class)))
                .thenThrow(new ResourceNotFoundException("OvertimeRecord", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void approve_shouldReturnApprovedRecord() throws Exception {
        var approvalRequest = new OvertimeApprovalRequest("APPROVED", "admin", "Approved");
        var response = new OvertimeRecordResponse(
                1L, 1L, "EMP001", "John Doe",
                LocalDate.of(2024, 1, 15),
                1L, "Weekday Overtime", "WEEKDAY",
                60, "APPROVED", "MANUAL",
                LocalTime.of(9, 0), LocalTime.of(17, 0),
                LocalDateTime.of(2024, 1, 15, 8, 45),
                LocalDateTime.of(2024, 1, 15, 18, 0),
                "Approved",
                "admin", LocalDateTime.of(2024, 1, 16, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 16, 10, 0)
        );
        when(overtimeRecordService.approve(eq(1L), any(OvertimeApprovalRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").value("admin"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(overtimeRecordService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(overtimeRecordService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("OvertimeRecord", "id", 999L))
                .when(overtimeRecordService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void detectOvertime_shouldReturn201WithDetectedRecords() throws Exception {
        var response = new OvertimeRecordResponse(
                1L, 1L, "EMP001", "John Doe",
                LocalDate.of(2024, 1, 15),
                1L, "Weekday Overtime", "WEEKDAY",
                45, "PENDING", "AUTO_DETECTED",
                LocalTime.of(9, 0), LocalTime.of(17, 0),
                LocalDateTime.of(2024, 1, 15, 8, 45),
                LocalDateTime.of(2024, 1, 15, 18, 30),
                "Auto-detected from attendance punches",
                null, null,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
        when(overtimeRecordService.detectOvertime(eq(1L), eq(LocalDate.of(2024, 1, 15)), eq(1L)))
                .thenReturn(List.of(response));

        mockMvc.perform(post(BASE_URL + "/detect")
                        .param("employeeId", "1")
                        .param("date", "2024-01-15")
                        .param("policyId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].source").value("AUTO_DETECTED"))
                .andExpect(jsonPath("$[0].overtimeMinutes").value(45));
    }

    @Test
    void getSummary_shouldReturnSummaries() throws Exception {
        var summary = new OvertimeSummaryResponse(
                1L, "EMP001", "John Doe",
                120, 60, 30, 30,
                new BigDecimal("90.00"), 3
        );
        when(overtimeRecordService.getSummary(
                eq(LocalDate.of(2024, 1, 1)),
                eq(LocalDate.of(2024, 1, 31))))
                .thenReturn(List.of(summary));

        mockMvc.perform(get(BASE_URL + "/summary")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeCode").value("EMP001"))
                .andExpect(jsonPath("$[0].totalOvertimeMinutes").value(120))
                .andExpect(jsonPath("$[0].approvedOvertimeMinutes").value(60))
                .andExpect(jsonPath("$[0].recordCount").value(3));
    }
}
