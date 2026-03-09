package com.raster.hrm.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.transfer.controller.TransferController;
import com.raster.hrm.transfer.dto.TransferRequest;
import com.raster.hrm.transfer.dto.TransferResponse;
import com.raster.hrm.transfer.service.TransferService;
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

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/transfers";

    private TransferResponse createResponse(Long id, String status) {
        return new TransferResponse(
                id, 1L, "EMP001", "John Doe",
                1L, "Engineering", 2L, "Marketing",
                "Branch A", "Branch B",
                "INTER_DEPARTMENT",
                LocalDate.of(2024, 6, 1),
                status, "Operational need",
                null, null, null,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private TransferResponse createApprovedResponse(Long id) {
        return new TransferResponse(
                id, 1L, "EMP001", "John Doe",
                1L, "Engineering", 2L, "Marketing",
                "Branch A", "Branch B",
                "INTER_DEPARTMENT",
                LocalDate.of(2024, 6, 1),
                "APPROVED", "Operational need",
                2L, "Jane Smith",
                LocalDateTime.of(2024, 2, 1, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 2, 1, 10, 0)
        );
    }

    private TransferRequest createRequest() {
        return new TransferRequest(
                1L, 1L, 2L,
                "Branch A", "Branch B",
                "INTER_DEPARTMENT",
                LocalDate.of(2024, 6, 1),
                "Operational need"
        );
    }

    @Test
    void getAll_shouldReturnPageOfTransfers() throws Exception {
        var transfers = List.of(
                createResponse(1L, "PENDING"),
                createResponse(2L, "APPROVED")
        );
        var page = new PageImpl<>(transfers, PageRequest.of(0, 20), 2);
        when(transferService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[1].status").value("APPROVED"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnTransfer() throws Exception {
        var response = createResponse(1L, "PENDING");
        when(transferService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.fromDepartmentName").value("Engineering"))
                .andExpect(jsonPath("$.toDepartmentName").value("Marketing"))
                .andExpect(jsonPath("$.transferType").value("INTER_DEPARTMENT"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(transferService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Transfer", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturnTransfers() throws Exception {
        var transfers = List.of(
                createResponse(1L, "PENDING"),
                createResponse(2L, "EXECUTED")
        );
        when(transferService.getByEmployeeId(1L)).thenReturn(transfers);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employeeName").value("John Doe"));
    }

    @Test
    void getByEmployee_shouldReturnEmptyList() throws Exception {
        when(transferService.getByEmployeeId(1L)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getPending_shouldReturnPendingTransfers() throws Exception {
        var transfers = List.of(createResponse(1L, "PENDING"));
        when(transferService.getPendingTransfers()).thenReturn(transfers);

        mockMvc.perform(get(BASE_URL + "/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getPending_shouldReturnEmptyList() throws Exception {
        when(transferService.getPendingTransfers()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void create_shouldReturn201WithCreatedTransfer() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "PENDING");
        when(transferService.create(any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.transferType").value("INTER_DEPARTMENT"));
    }

    @Test
    void create_shouldReturn400WhenEmployeeIdNull() throws Exception {
        var request = new TransferRequest(
                null, 1L, 2L,
                "Branch A", "Branch B",
                "INTER_DEPARTMENT",
                LocalDate.of(2024, 6, 1),
                "Reason"
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenTransferTypeBlank() throws Exception {
        var request = new TransferRequest(
                1L, 1L, 2L,
                "Branch A", "Branch B",
                "",
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
        var request = new TransferRequest(
                1L, 1L, 2L,
                "Branch A", "Branch B",
                "INTER_DEPARTMENT",
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
        when(transferService.create(any(TransferRequest.class)))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 1L));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void approve_shouldReturnApprovedTransfer() throws Exception {
        var response = createApprovedResponse(1L);
        when(transferService.approve(eq(1L), eq(2L))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/approve")
                        .param("approvedById", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedById").value(2))
                .andExpect(jsonPath("$.approvedByName").value("Jane Smith"));
    }

    @Test
    void approve_shouldReturn404WhenTransferNotFound() throws Exception {
        when(transferService.approve(eq(999L), eq(2L)))
                .thenThrow(new ResourceNotFoundException("Transfer", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999/approve")
                        .param("approvedById", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void approve_shouldReturn400WhenNotPending() throws Exception {
        when(transferService.approve(eq(1L), eq(2L)))
                .thenThrow(new BadRequestException("Transfer can only be approved when in PENDING status"));

        mockMvc.perform(put(BASE_URL + "/1/approve")
                        .param("approvedById", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reject_shouldReturnRejectedTransfer() throws Exception {
        var response = new TransferResponse(
                1L, 1L, "EMP001", "John Doe",
                1L, "Engineering", 2L, "Marketing",
                "Branch A", "Branch B",
                "INTER_DEPARTMENT",
                LocalDate.of(2024, 6, 1),
                "REJECTED", "Operational need",
                2L, "Jane Smith",
                LocalDateTime.of(2024, 2, 1, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 2, 1, 10, 0)
        );
        when(transferService.reject(eq(1L), eq(2L))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/reject")
                        .param("approvedById", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.approvedById").value(2));
    }

    @Test
    void reject_shouldReturn404WhenTransferNotFound() throws Exception {
        when(transferService.reject(eq(999L), eq(2L)))
                .thenThrow(new ResourceNotFoundException("Transfer", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999/reject")
                        .param("approvedById", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reject_shouldReturn400WhenNotPending() throws Exception {
        when(transferService.reject(eq(1L), eq(2L)))
                .thenThrow(new BadRequestException("Transfer can only be rejected when in PENDING status"));

        mockMvc.perform(put(BASE_URL + "/1/reject")
                        .param("approvedById", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void execute_shouldReturnExecutedTransfer() throws Exception {
        var response = new TransferResponse(
                1L, 1L, "EMP001", "John Doe",
                1L, "Engineering", 2L, "Marketing",
                "Branch A", "Branch B",
                "INTER_DEPARTMENT",
                LocalDate.of(2024, 6, 1),
                "EXECUTED", "Operational need",
                2L, "Jane Smith",
                LocalDateTime.of(2024, 2, 1, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 2, 1, 10, 0)
        );
        when(transferService.execute(1L)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXECUTED"));
    }

    @Test
    void execute_shouldReturn404WhenTransferNotFound() throws Exception {
        when(transferService.execute(999L))
                .thenThrow(new ResourceNotFoundException("Transfer", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999/execute"))
                .andExpect(status().isNotFound());
    }

    @Test
    void execute_shouldReturn400WhenNotApproved() throws Exception {
        when(transferService.execute(1L))
                .thenThrow(new BadRequestException("Transfer must be in APPROVED status before execution"));

        mockMvc.perform(put(BASE_URL + "/1/execute"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(transferService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(transferService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Transfer", "id", 999L))
                .when(transferService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn400WhenNotPending() throws Exception {
        doThrow(new BadRequestException("Only transfers in PENDING status can be deleted"))
                .when(transferService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isBadRequest());
    }
}
