package com.raster.hrm.idcard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.idcard.controller.IdCardController;
import com.raster.hrm.idcard.dto.IdCardRequest;
import com.raster.hrm.idcard.dto.IdCardResponse;
import com.raster.hrm.idcard.entity.IdCardStatus;
import com.raster.hrm.idcard.service.IdCardService;
import com.raster.hrm.exception.ResourceNotFoundException;
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
import java.util.Map;

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

@WebMvcTest(IdCardController.class)
class IdCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IdCardService idCardService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/id-cards";

    private IdCardResponse createResponse(Long id, String cardNumber, String status) {
        return new IdCardResponse(
                id, 1L, "EMP001", "John Doe",
                cardNumber,
                LocalDate.of(2024, 1, 1), LocalDate.of(2026, 12, 31),
                status,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private IdCardRequest createRequest() {
        return new IdCardRequest(
                1L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    @Test
    void getAll_shouldReturnPageOfIdCards() throws Exception {
        var idCards = List.of(
                createResponse(1L, "IDC-ABC1234567", "ACTIVE"),
                createResponse(2L, "IDC-DEF7890123", "ACTIVE")
        );
        var page = new PageImpl<>(idCards, PageRequest.of(0, 20), 2);
        when(idCardService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].cardNumber").value("IDC-ABC1234567"))
                .andExpect(jsonPath("$.content[1].cardNumber").value("IDC-DEF7890123"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnIdCard() throws Exception {
        var response = createResponse(1L, "IDC-ABC1234567", "ACTIVE");
        when(idCardService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardNumber").value("IDC-ABC1234567"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(idCardService.getById(999L))
                .thenThrow(new ResourceNotFoundException("IdCard", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturnIdCards() throws Exception {
        var idCards = List.of(
                createResponse(1L, "IDC-ABC1234567", "ACTIVE"),
                createResponse(2L, "IDC-DEF7890123", "EXPIRED")
        );
        when(idCardService.getByEmployeeId(1L)).thenReturn(idCards);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cardNumber").value("IDC-ABC1234567"));
    }

    @Test
    void getByEmployee_shouldReturn404WhenEmployeeNotFound() throws Exception {
        when(idCardService.getByEmployeeId(999L))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/employee/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201WithCreatedIdCard() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "IDC-ABC1234567", "ACTIVE");
        when(idCardService.create(any(IdCardRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardNumber").value("IDC-ABC1234567"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void create_shouldReturn400WhenEmployeeIdNull() throws Exception {
        var request = new IdCardRequest(null, LocalDate.of(2024, 1, 1), null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenIssueDateNull() throws Exception {
        var request = new IdCardRequest(1L, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn404WhenEmployeeNotFound() throws Exception {
        var request = createRequest();
        when(idCardService.create(any(IdCardRequest.class)))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 1L));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturnUpdatedIdCard() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "IDC-ABC1234567", "ACTIVE");
        when(idCardService.update(eq(1L), any(IdCardRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardNumber").value("IDC-ABC1234567"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(idCardService.update(eq(999L), any(IdCardRequest.class)))
                .thenThrow(new ResourceNotFoundException("IdCard", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn400WhenValidationFails() throws Exception {
        var request = new IdCardRequest(null, null, null);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_shouldReturnUpdatedIdCard() throws Exception {
        var response = createResponse(1L, "IDC-ABC1234567", "EXPIRED");
        when(idCardService.updateStatus(1L, IdCardStatus.EXPIRED)).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "EXPIRED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"));
    }

    @Test
    void updateStatus_shouldReturn404WhenNotFound() throws Exception {
        when(idCardService.updateStatus(999L, IdCardStatus.CANCELLED))
                .thenThrow(new ResourceNotFoundException("IdCard", "id", 999L));

        mockMvc.perform(patch(BASE_URL + "/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "CANCELLED"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(idCardService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(idCardService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("IdCard", "id", 999L))
                .when(idCardService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
