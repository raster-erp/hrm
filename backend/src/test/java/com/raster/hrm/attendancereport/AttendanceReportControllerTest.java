package com.raster.hrm.attendancereport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.attendancereport.controller.AttendanceReportController;
import com.raster.hrm.attendancereport.dto.AbsenteeEntry;
import com.raster.hrm.attendancereport.dto.AbsenteeListReport;
import com.raster.hrm.attendancereport.dto.DailyMusterEntry;
import com.raster.hrm.attendancereport.dto.DailyMusterReport;
import com.raster.hrm.attendancereport.dto.MonthlySummaryEntry;
import com.raster.hrm.attendancereport.dto.MonthlySummaryReport;
import com.raster.hrm.attendancereport.dto.ReportScheduleRequest;
import com.raster.hrm.attendancereport.dto.ReportScheduleResponse;
import com.raster.hrm.attendancereport.service.AttendanceReportService;
import com.raster.hrm.attendancereport.service.ReportScheduleService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceReportController.class)
class AttendanceReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceReportService attendanceReportService;

    @MockitoBean
    private ReportScheduleService reportScheduleService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/attendance-reports";

    // ===== Daily Muster Tests =====

    @Test
    void getDailyMuster_shouldReturnReport() throws Exception {
        var date = LocalDate.of(2025, 1, 15);
        var entry = new DailyMusterEntry(
                1L, "EMP001", "John Doe", "Engineering",
                date, date.atTime(9, 0), date.atTime(18, 0), 2, "PRESENT"
        );
        var report = new DailyMusterReport(date, 1L, "Engineering", List.of(entry), 1, 0, 0);
        when(attendanceReportService.generateDailyMuster(date, 1L)).thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/daily-muster")
                        .param("date", "2025-01-15")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2025-01-15"))
                .andExpect(jsonPath("$.departmentId").value(1))
                .andExpect(jsonPath("$.departmentName").value("Engineering"))
                .andExpect(jsonPath("$.entries.length()").value(1))
                .andExpect(jsonPath("$.entries[0].employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.entries[0].status").value("PRESENT"))
                .andExpect(jsonPath("$.totalPresent").value(1))
                .andExpect(jsonPath("$.totalAbsent").value(0));
    }

    @Test
    void getDailyMuster_withoutDepartment_shouldReturnReport() throws Exception {
        var date = LocalDate.of(2025, 1, 15);
        var report = new DailyMusterReport(date, null, "All Departments", List.of(), 0, 0, 0);
        when(attendanceReportService.generateDailyMuster(eq(date), eq(null))).thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/daily-muster")
                        .param("date", "2025-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentName").value("All Departments"));
    }

    @Test
    void getDailyMuster_missingDate_shouldReturnError() throws Exception {
        mockMvc.perform(get(BASE_URL + "/daily-muster"))
                .andExpect(status().isInternalServerError());
    }

    // ===== Monthly Summary Tests =====

    @Test
    void getMonthlySummary_shouldReturnReport() throws Exception {
        var entry = new MonthlySummaryEntry(
                1L, "EMP001", "John Doe", "Engineering", 20, 3, 0, 23
        );
        var report = new MonthlySummaryReport(2025, 1, 1L, "Engineering", List.of(entry));
        when(attendanceReportService.generateMonthlySummary(2025, 1, 1L)).thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/monthly-summary")
                        .param("year", "2025")
                        .param("month", "1")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.departmentId").value(1))
                .andExpect(jsonPath("$.entries.length()").value(1))
                .andExpect(jsonPath("$.entries[0].totalPresent").value(20))
                .andExpect(jsonPath("$.entries[0].totalWorkingDays").value(23));
    }

    @Test
    void getMonthlySummary_withoutDepartment_shouldReturnReport() throws Exception {
        var report = new MonthlySummaryReport(2025, 1, null, "All Departments", List.of());
        when(attendanceReportService.generateMonthlySummary(eq(2025), eq(1), eq(null))).thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/monthly-summary")
                        .param("year", "2025")
                        .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentName").value("All Departments"));
    }

    @Test
    void getMonthlySummary_missingYear_shouldReturnError() throws Exception {
        mockMvc.perform(get(BASE_URL + "/monthly-summary")
                        .param("month", "1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getMonthlySummary_missingMonth_shouldReturnError() throws Exception {
        mockMvc.perform(get(BASE_URL + "/monthly-summary")
                        .param("year", "2025"))
                .andExpect(status().isInternalServerError());
    }

    // ===== Absentee List Tests =====

    @Test
    void getAbsenteeList_shouldReturnReport() throws Exception {
        var entry = new AbsenteeEntry(1L, "EMP001", "John Doe", "Engineering", LocalDate.of(2025, 1, 6));
        var report = new AbsenteeListReport(
                LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 10),
                1L, "Engineering", List.of(entry), 1
        );
        when(attendanceReportService.generateAbsenteeList(
                LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 10), 1L))
                .thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/absentee-list")
                        .param("startDate", "2025-01-06")
                        .param("endDate", "2025-01-10")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-06"))
                .andExpect(jsonPath("$.endDate").value("2025-01-10"))
                .andExpect(jsonPath("$.entries.length()").value(1))
                .andExpect(jsonPath("$.totalAbsentInstances").value(1));
    }

    @Test
    void getAbsenteeList_withoutDepartment_shouldReturnReport() throws Exception {
        var report = new AbsenteeListReport(
                LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 10),
                null, "All Departments", List.of(), 0
        );
        when(attendanceReportService.generateAbsenteeList(any(), any(), eq(null))).thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/absentee-list")
                        .param("startDate", "2025-01-06")
                        .param("endDate", "2025-01-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAbsentInstances").value(0));
    }

    @Test
    void getAbsenteeList_missingStartDate_shouldReturnError() throws Exception {
        mockMvc.perform(get(BASE_URL + "/absentee-list")
                        .param("endDate", "2025-01-10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAbsenteeList_missingEndDate_shouldReturnError() throws Exception {
        mockMvc.perform(get(BASE_URL + "/absentee-list")
                        .param("startDate", "2025-01-06"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAbsenteeList_startAfterEnd_shouldReturn400() throws Exception {
        when(attendanceReportService.generateAbsenteeList(any(), any(), any()))
                .thenThrow(new BadRequestException("Start date must not be after end date"));

        mockMvc.perform(get(BASE_URL + "/absentee-list")
                        .param("startDate", "2025-01-10")
                        .param("endDate", "2025-01-06"))
                .andExpect(status().isBadRequest());
    }

    // ===== Export Tests =====

    @Test
    void exportReport_csvFormat_shouldReturnCsvFile() throws Exception {
        var csvBytes = "header\ndata\n".getBytes();
        when(attendanceReportService.exportReportAsCsv(eq("DAILY_MUSTER"), any())).thenReturn(csvBytes);

        mockMvc.perform(get(BASE_URL + "/export")
                        .param("reportType", "DAILY_MUSTER")
                        .param("format", "CSV")
                        .param("date", "2025-01-15")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"daily_muster_report.csv\""))
                .andExpect(content().bytes(csvBytes));
    }

    @Test
    void exportReport_defaultFormat_shouldReturnCsv() throws Exception {
        var csvBytes = "header\ndata\n".getBytes();
        when(attendanceReportService.exportReportAsCsv(eq("DAILY_MUSTER"), any())).thenReturn(csvBytes);

        mockMvc.perform(get(BASE_URL + "/export")
                        .param("reportType", "DAILY_MUSTER")
                        .param("date", "2025-01-15"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"daily_muster_report.csv\""));
    }

    @Test
    void exportReport_pdfFormat_shouldReturn400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/export")
                        .param("reportType", "DAILY_MUSTER")
                        .param("format", "PDF")
                        .param("date", "2025-01-15"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportReport_excelFormat_shouldReturn400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/export")
                        .param("reportType", "DAILY_MUSTER")
                        .param("format", "EXCEL")
                        .param("date", "2025-01-15"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportReport_missingReportType_shouldReturnError() throws Exception {
        mockMvc.perform(get(BASE_URL + "/export")
                        .param("format", "CSV")
                        .param("date", "2025-01-15"))
                .andExpect(status().isInternalServerError());
    }

    // ===== Schedule CRUD Tests =====

    @Test
    void getAllSchedules_shouldReturnPage() throws Exception {
        var response = new ReportScheduleResponse(
                1L, "Daily Report", "DAILY_MUSTER", "DAILY",
                1L, "Engineering", "admin@example.com", "CSV",
                true, null, null,
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );
        var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(reportScheduleService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].reportName").value("Daily Report"))
                .andExpect(jsonPath("$.content[0].reportType").value("DAILY_MUSTER"));
    }

    @Test
    void getScheduleById_shouldReturnSchedule() throws Exception {
        var response = new ReportScheduleResponse(
                1L, "Daily Report", "DAILY_MUSTER", "DAILY",
                1L, "Engineering", "admin@example.com", "CSV",
                true, null, null,
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 10, 0)
        );
        when(reportScheduleService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/schedules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reportName").value("Daily Report"));
    }

    @Test
    void getScheduleById_notFound_shouldReturn404() throws Exception {
        when(reportScheduleService.getById(999L))
                .thenThrow(new ResourceNotFoundException("ReportSchedule", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/schedules/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSchedule_shouldReturn201() throws Exception {
        var request = new ReportScheduleRequest(
                "Daily Report", "DAILY_MUSTER", "DAILY", 1L, "admin@example.com", "CSV"
        );
        var response = new ReportScheduleResponse(
                1L, "Daily Report", "DAILY_MUSTER", "DAILY",
                1L, "Engineering", "admin@example.com", "CSV",
                true, null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(reportScheduleService.create(any(ReportScheduleRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reportName").value("Daily Report"));
    }

    @Test
    void createSchedule_invalidRequest_missingReportName_shouldReturn400() throws Exception {
        var request = new ReportScheduleRequest(
                "", "DAILY_MUSTER", "DAILY", null, null, null
        );

        mockMvc.perform(post(BASE_URL + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSchedule_invalidRequest_nullReportType_shouldReturn400() throws Exception {
        var request = new ReportScheduleRequest(
                "Name", null, "DAILY", null, null, null
        );

        mockMvc.perform(post(BASE_URL + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSchedule_invalidRequest_nullFrequency_shouldReturn400() throws Exception {
        var request = new ReportScheduleRequest(
                "Name", "DAILY_MUSTER", null, null, null, null
        );

        mockMvc.perform(post(BASE_URL + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSchedule_shouldReturnUpdated() throws Exception {
        var request = new ReportScheduleRequest(
                "Updated Report", "MONTHLY_SUMMARY", "WEEKLY", 1L, "new@example.com", "CSV"
        );
        var response = new ReportScheduleResponse(
                1L, "Updated Report", "MONTHLY_SUMMARY", "WEEKLY",
                1L, "Engineering", "new@example.com", "CSV",
                true, null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(reportScheduleService.update(eq(1L), any(ReportScheduleRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/schedules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportName").value("Updated Report"));
    }

    @Test
    void updateSchedule_notFound_shouldReturn404() throws Exception {
        var request = new ReportScheduleRequest(
                "Name", "DAILY_MUSTER", "DAILY", null, null, null
        );
        when(reportScheduleService.update(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("ReportSchedule", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/schedules/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void toggleScheduleActive_shouldReturnToggled() throws Exception {
        var response = new ReportScheduleResponse(
                1L, "Report", "DAILY_MUSTER", "DAILY",
                null, null, null, "CSV",
                false, null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(reportScheduleService.toggleActive(1L)).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/schedules/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void toggleScheduleActive_notFound_shouldReturn404() throws Exception {
        when(reportScheduleService.toggleActive(999L))
                .thenThrow(new ResourceNotFoundException("ReportSchedule", "id", 999L));

        mockMvc.perform(patch(BASE_URL + "/schedules/999/active"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSchedule_shouldReturn204() throws Exception {
        doNothing().when(reportScheduleService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/schedules/1"))
                .andExpect(status().isNoContent());

        verify(reportScheduleService).delete(1L);
    }

    @Test
    void deleteSchedule_notFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("ReportSchedule", "id", 999L))
                .when(reportScheduleService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/schedules/999"))
                .andExpect(status().isNotFound());
    }
}
