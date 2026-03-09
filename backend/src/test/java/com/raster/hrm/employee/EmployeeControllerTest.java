package com.raster.hrm.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.employee.controller.EmployeeController;
import com.raster.hrm.employee.dto.EmployeeDocumentResponse;
import com.raster.hrm.employee.dto.EmployeeRequest;
import com.raster.hrm.employee.dto.EmployeeResponse;
import com.raster.hrm.employee.dto.EmployeeSearchCriteria;
import com.raster.hrm.employee.service.EmployeeService;
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

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/employees";

    private EmployeeResponse createEmployeeResponse(Long id, String code, String firstName, String lastName) {
        return new EmployeeResponse(
                id, code, firstName, lastName, firstName.toLowerCase() + "@test.com",
                "1234567890", LocalDate.of(1990, 1, 1), "Male",
                "123 Main St", null, "Springfield", "IL", "US", "62701",
                "Jane Doe", "0987654321", "Spouse",
                "Test Bank", "123456789", "TEST0001",
                1L, "Engineering", 1L, "Software Engineer",
                LocalDate.of(2023, 1, 15), "ACTIVE", null,
                LocalDateTime.of(2023, 1, 15, 10, 0),
                LocalDateTime.of(2023, 1, 15, 10, 0)
        );
    }

    private EmployeeRequest createEmployeeRequest(String code, String firstName, String lastName) {
        return new EmployeeRequest(
                code, firstName, lastName, firstName.toLowerCase() + "@test.com",
                "1234567890", LocalDate.of(1990, 1, 1), "Male",
                "123 Main St", null, "Springfield", "IL", "US", "62701",
                "Jane Doe", "0987654321", "Spouse",
                "Test Bank", "123456789", "TEST0001",
                1L, 1L, LocalDate.of(2023, 1, 15), "ACTIVE"
        );
    }

    @Test
    void getAll_shouldReturnPageOfEmployees() throws Exception {
        var employees = List.of(
                createEmployeeResponse(1L, "EMP001", "John", "Doe"),
                createEmployeeResponse(2L, "EMP002", "Jane", "Smith")
        );
        var page = new PageImpl<>(employees, PageRequest.of(0, 20), 2);
        when(employeeService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[1].firstName").value("Jane"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnEmployee() throws Exception {
        var response = createEmployeeResponse(1L, "EMP001", "John", "Doe");
        when(employeeService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.departmentName").value("Engineering"))
                .andExpect(jsonPath("$.designationTitle").value("Software Engineer"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(employeeService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_shouldReturnFilteredResults() throws Exception {
        var employees = List.of(createEmployeeResponse(1L, "EMP001", "John", "Doe"));
        var page = new PageImpl<>(employees, PageRequest.of(0, 20), 1);
        when(employeeService.search(any(EmployeeSearchCriteria.class), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/search")
                        .param("name", "John")
                        .param("departmentId", "1")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    @Test
    void search_shouldReturnResultsWithDateRange() throws Exception {
        var employees = List.of(createEmployeeResponse(1L, "EMP001", "John", "Doe"));
        var page = new PageImpl<>(employees, PageRequest.of(0, 20), 1);
        when(employeeService.search(any(EmployeeSearchCriteria.class), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/search")
                        .param("joiningDateFrom", "2023-01-01")
                        .param("joiningDateTo", "2023-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void search_shouldReturnEmptyPageWhenNoMatch() throws Exception {
        var page = new PageImpl<EmployeeResponse>(List.of(), PageRequest.of(0, 20), 0);
        when(employeeService.search(any(EmployeeSearchCriteria.class), any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/search")
                        .param("name", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void create_shouldReturn201WithCreatedEmployee() throws Exception {
        var request = createEmployeeRequest("EMP001", "John", "Doe");
        var response = createEmployeeResponse(1L, "EMP001", "John", "Doe");
        when(employeeService.create(any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void create_shouldReturn400WhenDuplicateCode() throws Exception {
        var request = createEmployeeRequest("EMP001", "John", "Doe");
        when(employeeService.create(any(EmployeeRequest.class)))
                .thenThrow(new BadRequestException("Employee with code 'EMP001' already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenDuplicateEmail() throws Exception {
        var request = createEmployeeRequest("EMP001", "John", "Doe");
        when(employeeService.create(any(EmployeeRequest.class)))
                .thenThrow(new BadRequestException("Employee with email 'john@test.com' already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenFirstNameBlank() throws Exception {
        var request = new EmployeeRequest(
                "EMP001", "", "Doe", "john@test.com",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenLastNameBlank() throws Exception {
        var request = new EmployeeRequest(
                "EMP001", "John", "", "john@test.com",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenEmailBlank() throws Exception {
        var request = new EmployeeRequest(
                "EMP001", "John", "Doe", "",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenEmployeeCodeBlank() throws Exception {
        var request = new EmployeeRequest(
                "", "John", "Doe", "john@test.com",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenEmailInvalid() throws Exception {
        var request = new EmployeeRequest(
                "EMP001", "John", "Doe", "not-an-email",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedEmployee() throws Exception {
        var request = createEmployeeRequest("EMP001", "John", "Updated");
        var response = createEmployeeResponse(1L, "EMP001", "John", "Updated");
        when(employeeService.update(eq(1L), any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.lastName").value("Updated"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createEmployeeRequest("EMP001", "John", "Doe");
        when(employeeService.update(eq(999L), any(EmployeeRequest.class)))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn400WhenValidationFails() throws Exception {
        var request = new EmployeeRequest(
                "", "", "", "",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
        );

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void softDelete_shouldReturn204() throws Exception {
        doNothing().when(employeeService).softDelete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(employeeService).softDelete(1L);
    }

    @Test
    void softDelete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Employee", "id", 999L))
                .when(employeeService).softDelete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDocuments_shouldReturnListOfDocuments() throws Exception {
        var documents = List.of(
                new EmployeeDocumentResponse(
                        1L, 1L, "ID_PROOF", "passport.pdf", "/uploads/passport.pdf",
                        1024L, "application/pdf", LocalDateTime.of(2023, 6, 1, 10, 0)
                ),
                new EmployeeDocumentResponse(
                        2L, 1L, "PHOTO", "photo.jpg", "/uploads/photo.jpg",
                        2048L, "image/jpeg", LocalDateTime.of(2023, 6, 1, 10, 0)
                )
        );
        when(employeeService.getDocuments(1L)).thenReturn(documents);

        mockMvc.perform(get(BASE_URL + "/1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].documentType").value("ID_PROOF"))
                .andExpect(jsonPath("$[0].documentName").value("passport.pdf"))
                .andExpect(jsonPath("$[1].documentType").value("PHOTO"));
    }

    @Test
    void getDocuments_shouldReturn404WhenEmployeeNotFound() throws Exception {
        when(employeeService.getDocuments(999L))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999/documents"))
                .andExpect(status().isNotFound());
    }
}
