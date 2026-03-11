package com.raster.hrm.attendancereport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.attendancereport.dto.ReportScheduleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/attendancereport/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/attendancereport/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AttendanceReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String REPORTS_URL = "/api/v1/attendance-reports";

    @Test
    void shouldGenerateDailyMusterReport() throws Exception {
        mockMvc.perform(get(REPORTS_URL + "/daily-muster")
                        .param("date", "2025-06-02")
                        .param("departmentId", "9701"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2025-06-02"))
                .andExpect(jsonPath("$.departmentId").value(9701))
                .andExpect(jsonPath("$.entries.length()").value(2))
                .andExpect(jsonPath("$.totalPresent").value(1))
                .andExpect(jsonPath("$.totalAbsent").value(1));
    }

    @Test
    void shouldGenerateMonthlySummary() throws Exception {
        mockMvc.perform(get(REPORTS_URL + "/monthly-summary")
                        .param("year", "2025")
                        .param("month", "6")
                        .param("departmentId", "9701"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.month").value(6))
                .andExpect(jsonPath("$.entries.length()").value(2))
                .andExpect(jsonPath("$.entries[?(@.employeeCode == 'EMP-RPT-001')].totalPresent").value(1));
    }

    @Test
    void shouldGenerateAbsenteeList() throws Exception {
        mockMvc.perform(get(REPORTS_URL + "/absentee-list")
                        .param("startDate", "2025-06-02")
                        .param("endDate", "2025-06-02")
                        .param("departmentId", "9701"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries.length()").value(1))
                .andExpect(jsonPath("$.entries[0].employeeCode").value("EMP-RPT-002"))
                .andExpect(jsonPath("$.entries[0].absentDate").value("2025-06-02"))
                .andExpect(jsonPath("$.totalAbsentInstances").value(1));
    }

    @Test
    void shouldExportDailyMusterAsCsv() throws Exception {
        mockMvc.perform(get(REPORTS_URL + "/export")
                        .param("reportType", "DAILY_MUSTER")
                        .param("format", "CSV")
                        .param("date", "2025-06-02")
                        .param("departmentId", "9701"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void shouldRejectUnsupportedExportFormat() throws Exception {
        mockMvc.perform(get(REPORTS_URL + "/export")
                        .param("reportType", "DAILY_MUSTER")
                        .param("format", "PDF")
                        .param("date", "2025-06-02"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateAndRetrieveSchedule() throws Exception {
        var request = new ReportScheduleRequest(
                "Daily Muster Schedule", "DAILY_MUSTER", "DAILY",
                9701L, "admin@test.com", "CSV");

        var result = mockMvc.perform(post(REPORTS_URL + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportName").value("Daily Muster Schedule"))
                .andExpect(jsonPath("$.reportType").value("DAILY_MUSTER"))
                .andExpect(jsonPath("$.frequency").value("DAILY"))
                .andExpect(jsonPath("$.departmentId").value(9701))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get(REPORTS_URL + "/schedules/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportName").value("Daily Muster Schedule"));
    }

    @Test
    void shouldToggleScheduleActive() throws Exception {
        var request = new ReportScheduleRequest(
                "Toggle Test", "MONTHLY_SUMMARY", "MONTHLY",
                9701L, null, "CSV");

        var result = mockMvc.perform(post(REPORTS_URL + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch(REPORTS_URL + "/schedules/" + id + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldDeleteSchedule() throws Exception {
        var request = new ReportScheduleRequest(
                "Delete Test", "ABSENTEE_LIST", "WEEKLY",
                9701L, null, "CSV");

        var result = mockMvc.perform(post(REPORTS_URL + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        var id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete(REPORTS_URL + "/schedules/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(REPORTS_URL + "/schedules/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400ForInvalidScheduleRequest() throws Exception {
        var invalidRequest = new ReportScheduleRequest(
                "", null, null, null, null, null);

        mockMvc.perform(post(REPORTS_URL + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
