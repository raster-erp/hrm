package com.raster.hrm.uniform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.uniform.controller.UniformAllocationController;
import com.raster.hrm.uniform.dto.UniformAllocationRequest;
import com.raster.hrm.uniform.dto.UniformAllocationResponse;
import com.raster.hrm.uniform.service.UniformAllocationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UniformAllocationController.class)
class UniformAllocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UniformAllocationService allocationService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/uniform-allocations";

    private UniformAllocationResponse createResponse(Long id, String status) {
        return new UniformAllocationResponse(
                id, 1L, "EMP001", "John Doe",
                1L, "Safety Vest", "PPE", "M",
                LocalDate.of(2024, 1, 1), null,
                status,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
    }

    private UniformAllocationRequest createRequest() {
        return new UniformAllocationRequest(
                1L, 1L, LocalDate.of(2024, 1, 1)
        );
    }

    @Test
    void getAll_shouldReturnPageOfAllocations() throws Exception {
        var allocations = List.of(
                createResponse(1L, "ALLOCATED"),
                createResponse(2L, "RETURNED")
        );
        var page = new PageImpl<>(allocations, PageRequest.of(0, 20), 2);
        when(allocationService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].status").value("ALLOCATED"))
                .andExpect(jsonPath("$.content[1].status").value("RETURNED"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnAllocation() throws Exception {
        var response = createResponse(1L, "ALLOCATED");
        when(allocationService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ALLOCATED"))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.uniformName").value("Safety Vest"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(allocationService.getById(999L))
                .thenThrow(new ResourceNotFoundException("UniformAllocation", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturnAllocations() throws Exception {
        var allocations = List.of(
                createResponse(1L, "ALLOCATED"),
                createResponse(2L, "RETURNED")
        );
        when(allocationService.getByEmployeeId(1L)).thenReturn(allocations);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("ALLOCATED"));
    }

    @Test
    void getByEmployee_shouldReturn404WhenEmployeeNotFound() throws Exception {
        when(allocationService.getByEmployeeId(999L))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/employee/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPendingReturns_shouldReturnAllocations() throws Exception {
        var allocations = List.of(
                createResponse(1L, "ALLOCATED"),
                createResponse(2L, "ALLOCATED")
        );
        when(allocationService.getPendingReturns()).thenReturn(allocations);

        mockMvc.perform(get(BASE_URL + "/pending-returns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("ALLOCATED"))
                .andExpect(jsonPath("$[1].status").value("ALLOCATED"));
    }

    @Test
    void getPendingReturns_shouldReturnEmptyList() throws Exception {
        when(allocationService.getPendingReturns()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/pending-returns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void allocate_shouldReturn201WithCreatedAllocation() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "ALLOCATED");
        when(allocationService.allocate(any(UniformAllocationRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ALLOCATED"))
                .andExpect(jsonPath("$.uniformName").value("Safety Vest"));
    }

    @Test
    void allocate_shouldReturn400WhenEmployeeIdNull() throws Exception {
        var request = new UniformAllocationRequest(null, 1L, LocalDate.of(2024, 1, 1));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allocate_shouldReturn400WhenUniformIdNull() throws Exception {
        var request = new UniformAllocationRequest(1L, null, LocalDate.of(2024, 1, 1));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allocate_shouldReturn400WhenAllocatedDateNull() throws Exception {
        var request = new UniformAllocationRequest(1L, 1L, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allocate_shouldReturn404WhenEmployeeNotFound() throws Exception {
        var request = createRequest();
        when(allocationService.allocate(any(UniformAllocationRequest.class)))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 1L));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void allocate_shouldReturn404WhenUniformNotFound() throws Exception {
        var request = createRequest();
        when(allocationService.allocate(any(UniformAllocationRequest.class)))
                .thenThrow(new ResourceNotFoundException("Uniform", "id", 1L));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void markReturned_shouldReturnUpdatedAllocation() throws Exception {
        var response = new UniformAllocationResponse(
                1L, 1L, "EMP001", "John Doe",
                1L, "Safety Vest", "PPE", "M",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 1),
                "RETURNED",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 6, 1, 10, 0)
        );
        when(allocationService.markReturned(1L)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnedDate").value("2024-06-01"));
    }

    @Test
    void markReturned_shouldReturn404WhenNotFound() throws Exception {
        when(allocationService.markReturned(999L))
                .thenThrow(new ResourceNotFoundException("UniformAllocation", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999/return"))
                .andExpect(status().isNotFound());
    }
}
