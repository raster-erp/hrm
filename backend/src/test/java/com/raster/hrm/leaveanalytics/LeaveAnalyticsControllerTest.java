package com.raster.hrm.leaveanalytics;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.leaveanalytics.controller.LeaveAnalyticsController;
import com.raster.hrm.leaveanalytics.dto.AbsenteeismRateEntry;
import com.raster.hrm.leaveanalytics.dto.AbsenteeismRateReport;
import com.raster.hrm.leaveanalytics.dto.LeaveTrendEntry;
import com.raster.hrm.leaveanalytics.dto.LeaveTrendReport;
import com.raster.hrm.leaveanalytics.dto.LeaveUtilizationEntry;
import com.raster.hrm.leaveanalytics.dto.LeaveUtilizationReport;
import com.raster.hrm.leaveanalytics.service.LeaveAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaveAnalyticsController.class)
class LeaveAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaveAnalyticsService leaveAnalyticsService;

    private static final String BASE_URL = "/api/v1/leave-analytics";

    // ===== Leave Trend Tests =====

    @Test
    void getLeaveTrend_shouldReturnReport() throws Exception {
        var entry = new LeaveTrendEntry(2025, 1, "Annual Leave", 5, new BigDecimal("10"));
        var report = new LeaveTrendReport(2025, 1, 2025, 3, null, "All Departments", List.of(entry));
        when(leaveAnalyticsService.generateLeaveTrend(2025, 1, 2025, 3, null, null, null, null)).thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/trend")
                        .param("startYear", "2025")
                        .param("startMonth", "1")
                        .param("endYear", "2025")
                        .param("endMonth", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startYear").value(2025))
                .andExpect(jsonPath("$.startMonth").value(1))
                .andExpect(jsonPath("$.endYear").value(2025))
                .andExpect(jsonPath("$.endMonth").value(3))
                .andExpect(jsonPath("$.departmentName").value("All Departments"))
                .andExpect(jsonPath("$.entries.length()").value(1))
                .andExpect(jsonPath("$.entries[0].leaveTypeName").value("Annual Leave"))
                .andExpect(jsonPath("$.entries[0].applicationCount").value(5));
    }

    @Test
    void getLeaveTrend_withDepartment_shouldReturnReport() throws Exception {
        var report = new LeaveTrendReport(2025, 1, 2025, 3, 1L, "Engineering", List.of());
        when(leaveAnalyticsService.generateLeaveTrend(2025, 1, 2025, 3, 1L, null, null, null)).thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/trend")
                        .param("startYear", "2025")
                        .param("startMonth", "1")
                        .param("endYear", "2025")
                        .param("endMonth", "3")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentId").value(1))
                .andExpect(jsonPath("$.departmentName").value("Engineering"));
    }

    @Test
    void getLeaveTrend_withDimensionFilters_shouldReturnReport() throws Exception {
        var report = new LeaveTrendReport(2025, 1, 2025, 3, null, "All Departments", List.of());
        when(leaveAnalyticsService.generateLeaveTrend(2025, 1, 2025, 3, null, 1L, "Male", "25_34"))
                .thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/trend")
                        .param("startYear", "2025")
                        .param("startMonth", "1")
                        .param("endYear", "2025")
                        .param("endMonth", "3")
                        .param("designationId", "1")
                        .param("gender", "Male")
                        .param("ageGroup", "25_34"))
                .andExpect(status().isOk());
    }

    @Test
    void getLeaveTrend_missingStartYear_shouldReturnError() throws Exception {
        mockMvc.perform(get(BASE_URL + "/trend")
                        .param("startMonth", "1")
                        .param("endYear", "2025")
                        .param("endMonth", "3"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getLeaveTrend_invalidRange_shouldReturn400() throws Exception {
        when(leaveAnalyticsService.generateLeaveTrend(anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any(), any()))
                .thenThrow(new BadRequestException("Start period must not be after end period"));

        mockMvc.perform(get(BASE_URL + "/trend")
                        .param("startYear", "2025")
                        .param("startMonth", "6")
                        .param("endYear", "2025")
                        .param("endMonth", "1"))
                .andExpect(status().isBadRequest());
    }

    // ===== Absenteeism Rate Tests =====

    @Test
    void getAbsenteeismRate_shouldReturnReport() throws Exception {
        var entry = new AbsenteeismRateEntry(1L, "Engineering", 10,
                new BigDecimal("15"), 22, new BigDecimal("6.82"));
        var report = new AbsenteeismRateReport(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new BigDecimal("6.82"), List.of(entry));
        when(leaveAnalyticsService.generateAbsenteeismRate(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), null, null, null, null))
                .thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/absenteeism-rate")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-01"))
                .andExpect(jsonPath("$.endDate").value("2025-01-31"))
                .andExpect(jsonPath("$.overallRate").value(6.82))
                .andExpect(jsonPath("$.entries.length()").value(1))
                .andExpect(jsonPath("$.entries[0].departmentName").value("Engineering"))
                .andExpect(jsonPath("$.entries[0].employeeCount").value(10));
    }

    @Test
    void getAbsenteeismRate_withDepartment_shouldReturnReport() throws Exception {
        var entry = new AbsenteeismRateEntry(2L, "HR", 5,
                new BigDecimal("3"), 22, new BigDecimal("2.73"));
        var report = new AbsenteeismRateReport(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                new BigDecimal("2.73"), List.of(entry));
        when(leaveAnalyticsService.generateAbsenteeismRate(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 2L, null, null, null))
                .thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/absenteeism-rate")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31")
                        .param("departmentId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries[0].departmentName").value("HR"));
    }

    @Test
    void getAbsenteeismRate_missingStartDate_shouldReturnError() throws Exception {
        mockMvc.perform(get(BASE_URL + "/absenteeism-rate")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAbsenteeismRate_startAfterEnd_shouldReturn400() throws Exception {
        when(leaveAnalyticsService.generateAbsenteeismRate(any(), any(), any(), any(), any(), any()))
                .thenThrow(new BadRequestException("Start date must not be after end date"));

        mockMvc.perform(get(BASE_URL + "/absenteeism-rate")
                        .param("startDate", "2025-02-01")
                        .param("endDate", "2025-01-01"))
                .andExpect(status().isBadRequest());
    }

    // ===== Leave Utilization Tests =====

    @Test
    void getLeaveUtilization_shouldReturnReport() throws Exception {
        var entry = new LeaveUtilizationEntry(1L, "EMP001", "John Doe", "Engineering",
                "Annual Leave", new BigDecimal("20"), new BigDecimal("10"),
                new BigDecimal("10"), new BigDecimal("50.00"));
        var report = new LeaveUtilizationReport(2025, null, "All Departments",
                new BigDecimal("50.00"), List.of(entry));
        when(leaveAnalyticsService.generateLeaveUtilization(2025, null, null, null, null)).thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/utilization")
                        .param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.departmentName").value("All Departments"))
                .andExpect(jsonPath("$.overallUtilization").value(50.00))
                .andExpect(jsonPath("$.entries.length()").value(1))
                .andExpect(jsonPath("$.entries[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.entries[0].utilizationPercent").value(50.00));
    }

    @Test
    void getLeaveUtilization_withDepartment_shouldReturnReport() throws Exception {
        var report = new LeaveUtilizationReport(2025, 1L, "Engineering",
                BigDecimal.ZERO, List.of());
        when(leaveAnalyticsService.generateLeaveUtilization(2025, 1L, null, null, null)).thenReturn(report);

        mockMvc.perform(get(BASE_URL + "/utilization")
                        .param("year", "2025")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentId").value(1))
                .andExpect(jsonPath("$.departmentName").value("Engineering"));
    }

    @Test
    void getLeaveUtilization_missingYear_shouldReturnError() throws Exception {
        mockMvc.perform(get(BASE_URL + "/utilization"))
                .andExpect(status().isInternalServerError());
    }

    // ===== Export Tests =====

    @Test
    void exportReport_csvFormat_shouldReturnCsvFile() throws Exception {
        var csvBytes = "header\ndata\n".getBytes();
        when(leaveAnalyticsService.exportReportAsCsv(eq("LEAVE_TREND"), any())).thenReturn(csvBytes);

        mockMvc.perform(get(BASE_URL + "/export")
                        .param("reportType", "LEAVE_TREND")
                        .param("format", "CSV")
                        .param("startYear", "2025")
                        .param("startMonth", "1")
                        .param("endYear", "2025")
                        .param("endMonth", "3"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"leave_trend_report.csv\""))
                .andExpect(content().bytes(csvBytes));
    }

    @Test
    void exportReport_defaultFormat_shouldReturnCsv() throws Exception {
        var csvBytes = "header\ndata\n".getBytes();
        when(leaveAnalyticsService.exportReportAsCsv(eq("LEAVE_UTILIZATION"), any())).thenReturn(csvBytes);

        mockMvc.perform(get(BASE_URL + "/export")
                        .param("reportType", "LEAVE_UTILIZATION")
                        .param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"leave_utilization_report.csv\""));
    }

    @Test
    void exportReport_pdfFormat_shouldReturnPdf() throws Exception {
        var pdfBytes = new byte[]{0x25, 0x50, 0x44, 0x46};
        when(leaveAnalyticsService.exportReportAsPdf(eq("LEAVE_TREND"), any())).thenReturn(pdfBytes);

        mockMvc.perform(get(BASE_URL + "/export")
                        .param("reportType", "LEAVE_TREND")
                        .param("format", "PDF")
                        .param("startYear", "2025")
                        .param("startMonth", "1")
                        .param("endYear", "2025")
                        .param("endMonth", "3"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"leave_trend_report.pdf\""));
    }

    @Test
    void exportReport_excelFormat_shouldReturnExcel() throws Exception {
        var excelBytes = new byte[]{0x50, 0x4B};
        when(leaveAnalyticsService.exportReportAsExcel(eq("LEAVE_TREND"), any())).thenReturn(excelBytes);

        mockMvc.perform(get(BASE_URL + "/export")
                        .param("reportType", "LEAVE_TREND")
                        .param("format", "EXCEL")
                        .param("startYear", "2025")
                        .param("startMonth", "1")
                        .param("endYear", "2025")
                        .param("endMonth", "3"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"leave_trend_report.xlsx\""));
    }

    @Test
    void exportReport_unsupportedFormat_shouldReturn400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/export")
                        .param("reportType", "LEAVE_TREND")
                        .param("format", "XML")
                        .param("startYear", "2025")
                        .param("startMonth", "1")
                        .param("endYear", "2025")
                        .param("endMonth", "3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportReport_withDimensionFilters_shouldPassParams() throws Exception {
        var csvBytes = "header\ndata\n".getBytes();
        when(leaveAnalyticsService.exportReportAsCsv(eq("LEAVE_TREND"), any())).thenReturn(csvBytes);

        mockMvc.perform(get(BASE_URL + "/export")
                        .param("reportType", "LEAVE_TREND")
                        .param("format", "CSV")
                        .param("startYear", "2025")
                        .param("startMonth", "1")
                        .param("endYear", "2025")
                        .param("endMonth", "3")
                        .param("designationId", "1")
                        .param("gender", "Female")
                        .param("ageGroup", "25_34"))
                .andExpect(status().isOk());
    }

    @Test
    void exportReport_missingReportType_shouldReturnError() throws Exception {
        mockMvc.perform(get(BASE_URL + "/export")
                        .param("format", "CSV"))
                .andExpect(status().isInternalServerError());
    }
}
