package com.raster.hrm.shiftroster;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.shiftroster.dto.BulkShiftRosterRequest;
import com.raster.hrm.shiftroster.dto.ShiftRosterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/shiftroster/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/shiftroster/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ShiftRosterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/shift-rosters";

    @Test
    void shouldCreateAndRetrieveRoster() throws Exception {
        var request = new ShiftRosterRequest(
                9001L, 9001L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), null);

        var result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.shiftName").value("Morning Shift"))
                .andReturn();

        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCode").value("EMP-001"))
                .andExpect(jsonPath("$.shiftName").value("Morning Shift"));
    }

    @Test
    void shouldRejectOverlappingRosterAssignment() throws Exception {
        var request1 = new ShiftRosterRequest(
                9001L, 9001L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), null);
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        var request2 = new ShiftRosterRequest(
                9001L, 9002L, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 9, 30), null);
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectEndDateBeforeEffectiveDate() throws Exception {
        var request = new ShiftRosterRequest(
                9001L, 9001L, LocalDate.of(2025, 6, 30), LocalDate.of(2025, 1, 1), null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBulkCreateRosters() throws Exception {
        var request = new BulkShiftRosterRequest(
                List.of(9001L, 9002L), 9001L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), null);

        mockMvc.perform(post(BASE_URL + "/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldSkipConflictingEmployeesInBulkCreate() throws Exception {
        var individual = new ShiftRosterRequest(
                9001L, 9001L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), null);
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(individual)))
                .andExpect(status().isCreated());

        var bulkRequest = new BulkShiftRosterRequest(
                List.of(9001L, 9002L), 9002L, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 9, 30), null);

        mockMvc.perform(post(BASE_URL + "/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeCode").value("EMP-002"));
    }

    @Test
    void shouldCreateRosterWithRotationPattern() throws Exception {
        var request = new ShiftRosterRequest(
                9001L, 9001L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), 9001L);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rotationPatternId").value(9001))
                .andExpect(jsonPath("$.rotationPatternName").value("Weekly Rotation"));
    }

    @Test
    void shouldUpdateRoster() throws Exception {
        var createRequest = new ShiftRosterRequest(
                9001L, 9001L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), null);
        var createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        var updateRequest = new ShiftRosterRequest(
                9001L, 9002L, LocalDate.of(2025, 2, 1), LocalDate.of(2025, 12, 31), null);
        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shiftName").value("Evening Shift"));
    }

    @Test
    void shouldDeleteRoster() throws Exception {
        var createRequest = new ShiftRosterRequest(
                9001L, 9001L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), null);
        var createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetRostersByDateRange() throws Exception {
        var request = new ShiftRosterRequest(
                9001L, 9001L, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 6, 30), null);
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "/date-range")
                        .param("startDate", "2025-04-01")
                        .param("endDate", "2025-04-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get(BASE_URL + "/date-range")
                        .param("startDate", "2025-07-01")
                        .param("endDate", "2025-07-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetRostersByEmployeeId() throws Exception {
        var request = new ShiftRosterRequest(
                9001L, 9001L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), null);
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "/employee/9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeCode").value("EMP-001"));
    }
}
