package com.raster.hrm.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.contract.controller.ContractController;
import com.raster.hrm.contract.dto.ContractAmendmentRequest;
import com.raster.hrm.contract.dto.ContractAmendmentResponse;
import com.raster.hrm.contract.dto.ContractRequest;
import com.raster.hrm.contract.dto.ContractResponse;
import com.raster.hrm.contract.service.ContractService;
import com.raster.hrm.exception.BadRequestException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContractController.class)
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContractService contractService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/contracts";

    private ContractResponse createContractResponse(Long id, String contractType, String status) {
        return new ContractResponse(
                id, 1L, "EMP001", "John Doe",
                contractType, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                "Standard terms", status,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 1, 10, 0)
        );
    }

    private ContractRequest createContractRequest() {
        return new ContractRequest(
                1L, "PERMANENT", LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31), "Standard terms", "ACTIVE"
        );
    }

    private ContractAmendmentResponse createAmendmentResponse(Long id) {
        return new ContractAmendmentResponse(
                id, 1L, LocalDate.of(2024, 6, 1),
                "Updated salary clause", "Old terms", "New terms",
                LocalDateTime.of(2024, 6, 1, 10, 0)
        );
    }

    @Test
    void getAll_shouldReturnPageOfContracts() throws Exception {
        var contracts = List.of(
                createContractResponse(1L, "PERMANENT", "ACTIVE"),
                createContractResponse(2L, "FIXED_TERM", "ACTIVE")
        );
        var page = new PageImpl<>(contracts, PageRequest.of(0, 20), 2);
        when(contractService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].contractType").value("PERMANENT"))
                .andExpect(jsonPath("$.content[1].contractType").value("FIXED_TERM"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnContract() throws Exception {
        var response = createContractResponse(1L, "PERMANENT", "ACTIVE");
        when(contractService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.contractType").value("PERMANENT"))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(contractService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Contract", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmployee_shouldReturnContracts() throws Exception {
        var contracts = List.of(
                createContractResponse(1L, "PERMANENT", "ACTIVE"),
                createContractResponse(2L, "PROBATION", "EXPIRED")
        );
        when(contractService.getByEmployee(1L)).thenReturn(contracts);

        mockMvc.perform(get(BASE_URL + "/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].contractType").value("PERMANENT"))
                .andExpect(jsonPath("$[1].contractType").value("PROBATION"));
    }

    @Test
    void getByEmployee_shouldReturn404WhenEmployeeNotFound() throws Exception {
        when(contractService.getByEmployee(999L))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/employee/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExpiringContracts_shouldReturnContracts() throws Exception {
        var contracts = List.of(createContractResponse(1L, "FIXED_TERM", "ACTIVE"));
        when(contractService.getExpiringContracts(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(contracts);

        mockMvc.perform(get(BASE_URL + "/expiring")
                        .param("startDate", "2024-11-01")
                        .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].contractType").value("FIXED_TERM"));
    }

    @Test
    void getExpiringContracts_shouldReturn400WhenInvalidDateRange() throws Exception {
        when(contractService.getExpiringContracts(any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new BadRequestException("Start date must be before or equal to end date"));

        mockMvc.perform(get(BASE_URL + "/expiring")
                        .param("startDate", "2024-12-31")
                        .param("endDate", "2024-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn201WithCreatedContract() throws Exception {
        var request = createContractRequest();
        var response = createContractResponse(1L, "PERMANENT", "ACTIVE");
        when(contractService.create(any(ContractRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.contractType").value("PERMANENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void create_shouldReturn400WhenEmployeeIdNull() throws Exception {
        var request = new ContractRequest(
                null, "PERMANENT", LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31), "Terms", "ACTIVE"
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenContractTypeBlank() throws Exception {
        var request = new ContractRequest(
                1L, "", LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31), "Terms", "ACTIVE"
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenStartDateNull() throws Exception {
        var request = new ContractRequest(
                1L, "PERMANENT", null,
                LocalDate.of(2024, 12, 31), "Terms", "ACTIVE"
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn404WhenEmployeeNotFound() throws Exception {
        var request = createContractRequest();
        when(contractService.create(any(ContractRequest.class)))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 1L));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturnUpdatedContract() throws Exception {
        var request = createContractRequest();
        var response = createContractResponse(1L, "PERMANENT", "ACTIVE");
        when(contractService.update(eq(1L), any(ContractRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.contractType").value("PERMANENT"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createContractRequest();
        when(contractService.update(eq(999L), any(ContractRequest.class)))
                .thenThrow(new ResourceNotFoundException("Contract", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn400WhenValidationFails() throws Exception {
        var request = new ContractRequest(
                null, "", null, null, null, null
        );

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void renewContract_shouldReturn201WithRenewedContract() throws Exception {
        var request = createContractRequest();
        var response = createContractResponse(2L, "PERMANENT", "ACTIVE");
        when(contractService.renewContract(eq(1L), any(ContractRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/1/renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void renewContract_shouldReturn404WhenNotFound() throws Exception {
        var request = createContractRequest();
        when(contractService.renewContract(eq(999L), any(ContractRequest.class)))
                .thenThrow(new ResourceNotFoundException("Contract", "id", 999L));

        mockMvc.perform(post(BASE_URL + "/999/renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void renewContract_shouldReturn400WhenNotActive() throws Exception {
        var request = createContractRequest();
        when(contractService.renewContract(eq(1L), any(ContractRequest.class)))
                .thenThrow(new BadRequestException("Only active contracts can be renewed"));

        mockMvc.perform(post(BASE_URL + "/1/renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAmendment_shouldReturn201WithCreatedAmendment() throws Exception {
        var request = new ContractAmendmentRequest(
                LocalDate.of(2024, 6, 1), "Updated salary clause",
                "Old terms", "New terms"
        );
        var response = createAmendmentResponse(1L);
        when(contractService.addAmendment(eq(1L), any(ContractAmendmentRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/1/amendments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.contractId").value(1))
                .andExpect(jsonPath("$.description").value("Updated salary clause"));
    }

    @Test
    void addAmendment_shouldReturn404WhenContractNotFound() throws Exception {
        var request = new ContractAmendmentRequest(
                LocalDate.of(2024, 6, 1), "Updated salary clause",
                "Old terms", "New terms"
        );
        when(contractService.addAmendment(eq(999L), any(ContractAmendmentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Contract", "id", 999L));

        mockMvc.perform(post(BASE_URL + "/999/amendments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addAmendment_shouldReturn400WhenAmendmentDateNull() throws Exception {
        var request = new ContractAmendmentRequest(
                null, "Description", "Old terms", "New terms"
        );

        mockMvc.perform(post(BASE_URL + "/1/amendments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAmendments_shouldReturnAmendments() throws Exception {
        var amendments = List.of(
                createAmendmentResponse(1L),
                createAmendmentResponse(2L)
        );
        when(contractService.getAmendments(1L)).thenReturn(amendments);

        mockMvc.perform(get(BASE_URL + "/1/amendments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getAmendments_shouldReturn404WhenContractNotFound() throws Exception {
        when(contractService.getAmendments(999L))
                .thenThrow(new ResourceNotFoundException("Contract", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999/amendments"))
                .andExpect(status().isNotFound());
    }
}
