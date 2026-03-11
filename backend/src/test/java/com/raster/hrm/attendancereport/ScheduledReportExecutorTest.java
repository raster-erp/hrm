package com.raster.hrm.attendancereport;

import com.raster.hrm.attendancereport.entity.ReportFormat;
import com.raster.hrm.attendancereport.entity.ReportSchedule;
import com.raster.hrm.attendancereport.entity.ReportType;
import com.raster.hrm.attendancereport.entity.ScheduleFrequency;
import com.raster.hrm.attendancereport.repository.ReportScheduleRepository;
import com.raster.hrm.attendancereport.service.AttendanceReportService;
import com.raster.hrm.attendancereport.service.ReportEmailService;
import com.raster.hrm.attendancereport.service.ScheduledReportExecutor;
import com.raster.hrm.department.entity.Department;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledReportExecutorTest {

    @Mock
    private ReportScheduleRepository reportScheduleRepository;

    @Mock
    private AttendanceReportService attendanceReportService;

    @Mock
    private ReportEmailService reportEmailService;

    @InjectMocks
    private ScheduledReportExecutor executor;

    private Department createDepartment() {
        var dept = new Department();
        dept.setId(1L);
        dept.setName("Engineering");
        dept.setCode("ENG");
        return dept;
    }

    private ReportSchedule createSchedule(ReportType type, ScheduleFrequency frequency) {
        var schedule = new ReportSchedule();
        schedule.setId(1L);
        schedule.setReportName("Test Report");
        schedule.setReportType(type);
        schedule.setFrequency(frequency);
        schedule.setDepartment(createDepartment());
        schedule.setRecipients("admin@test.com,hr@test.com");
        schedule.setExportFormat(ReportFormat.CSV);
        schedule.setActive(true);
        schedule.setNextRunAt(LocalDateTime.of(2025, 6, 1, 8, 0));
        return schedule;
    }

    // ===== executeDueSchedules tests =====

    @Test
    void executeDueSchedules_withNoDueSchedules_shouldDoNothing() {
        when(reportScheduleRepository.findByActiveTrueAndNextRunAtBefore(any()))
                .thenReturn(Collections.emptyList());

        executor.executeDueSchedules();

        verify(reportScheduleRepository, never()).save(any());
    }

    @Test
    void executeDueSchedules_withDueSchedules_shouldExecuteAll() {
        var schedule = createSchedule(ReportType.DAILY_MUSTER, ScheduleFrequency.DAILY);
        when(reportScheduleRepository.findByActiveTrueAndNextRunAtBefore(any()))
                .thenReturn(List.of(schedule));
        when(attendanceReportService.exportReportAsCsv(anyString(), any())).thenReturn("data".getBytes());

        executor.executeDueSchedules();

        verify(reportScheduleRepository).save(schedule);
    }

    // ===== executeSchedule tests =====

    @Test
    void executeSchedule_dailyMuster_shouldGenerateAndSendReport() throws MessagingException {
        var schedule = createSchedule(ReportType.DAILY_MUSTER, ScheduleFrequency.DAILY);
        var now = LocalDateTime.of(2025, 6, 2, 8, 0);
        byte[] csvData = "csv,data".getBytes();

        when(attendanceReportService.exportReportAsCsv(eq("DAILY_MUSTER"), any())).thenReturn(csvData);

        executor.executeSchedule(schedule, now);

        verify(reportEmailService).sendReportEmail(
                any(String[].class), anyString(), anyString(), anyString(), eq(csvData));
        verify(reportScheduleRepository).save(schedule);
        assertEquals(now, schedule.getLastRunAt());
        assertNotNull(schedule.getNextRunAt());
    }

    @Test
    void executeSchedule_monthlySummary_shouldGenerateAndSendReport() throws MessagingException {
        var schedule = createSchedule(ReportType.MONTHLY_SUMMARY, ScheduleFrequency.MONTHLY);
        var now = LocalDateTime.of(2025, 7, 1, 8, 0);
        byte[] csvData = "monthly,data".getBytes();

        when(attendanceReportService.exportReportAsCsv(eq("MONTHLY_SUMMARY"), any())).thenReturn(csvData);

        executor.executeSchedule(schedule, now);

        verify(reportEmailService).sendReportEmail(
                any(String[].class), anyString(), anyString(), anyString(), eq(csvData));
        verify(reportScheduleRepository).save(schedule);
    }

    @Test
    void executeSchedule_absenteeList_shouldGenerateAndSendReport() throws MessagingException {
        var schedule = createSchedule(ReportType.ABSENTEE_LIST, ScheduleFrequency.WEEKLY);
        var now = LocalDateTime.of(2025, 6, 9, 8, 0);
        byte[] csvData = "absentee,data".getBytes();

        when(attendanceReportService.exportReportAsCsv(eq("ABSENTEE_LIST"), any())).thenReturn(csvData);

        executor.executeSchedule(schedule, now);

        verify(reportEmailService).sendReportEmail(
                any(String[].class), anyString(), anyString(), anyString(), eq(csvData));
        verify(reportScheduleRepository).save(schedule);
    }

    @Test
    void executeSchedule_withNoRecipients_shouldNotSendEmail() throws MessagingException {
        var schedule = createSchedule(ReportType.DAILY_MUSTER, ScheduleFrequency.DAILY);
        schedule.setRecipients(null);
        var now = LocalDateTime.of(2025, 6, 2, 8, 0);

        when(attendanceReportService.exportReportAsCsv(anyString(), any())).thenReturn("data".getBytes());

        executor.executeSchedule(schedule, now);

        verify(reportEmailService, never()).sendReportEmail(any(), anyString(), anyString(), anyString(), any());
        verify(reportScheduleRepository).save(schedule);
    }

    @Test
    void executeSchedule_withBlankRecipients_shouldNotSendEmail() throws MessagingException {
        var schedule = createSchedule(ReportType.DAILY_MUSTER, ScheduleFrequency.DAILY);
        schedule.setRecipients("   ");
        var now = LocalDateTime.of(2025, 6, 2, 8, 0);

        when(attendanceReportService.exportReportAsCsv(anyString(), any())).thenReturn("data".getBytes());

        executor.executeSchedule(schedule, now);

        verify(reportEmailService, never()).sendReportEmail(any(), anyString(), anyString(), anyString(), any());
        verify(reportScheduleRepository).save(schedule);
    }

    @Test
    void executeSchedule_withNoDepartment_shouldBuildParamsWithoutDepartmentId() {
        var schedule = createSchedule(ReportType.DAILY_MUSTER, ScheduleFrequency.DAILY);
        schedule.setDepartment(null);
        schedule.setRecipients(null);
        var now = LocalDateTime.of(2025, 6, 2, 8, 0);

        when(attendanceReportService.exportReportAsCsv(anyString(), any())).thenReturn("data".getBytes());

        executor.executeSchedule(schedule, now);

        verify(reportScheduleRepository).save(schedule);
    }

    @Test
    void executeSchedule_whenReportGenerationFails_shouldLogErrorAndContinue() {
        var schedule = createSchedule(ReportType.DAILY_MUSTER, ScheduleFrequency.DAILY);
        var now = LocalDateTime.of(2025, 6, 2, 8, 0);

        when(attendanceReportService.exportReportAsCsv(anyString(), any()))
                .thenThrow(new RuntimeException("Report generation failed"));

        executor.executeSchedule(schedule, now);

        verify(reportScheduleRepository, never()).save(any());
    }

    @Test
    void executeSchedule_whenEmailFails_shouldLogErrorAndContinue() throws MessagingException {
        var schedule = createSchedule(ReportType.DAILY_MUSTER, ScheduleFrequency.DAILY);
        var now = LocalDateTime.of(2025, 6, 2, 8, 0);

        when(attendanceReportService.exportReportAsCsv(anyString(), any())).thenReturn("data".getBytes());
        doThrow(new MessagingException("SMTP error"))
                .when(reportEmailService).sendReportEmail(any(), anyString(), anyString(), anyString(), any());

        executor.executeSchedule(schedule, now);

        verify(reportScheduleRepository, never()).save(any());
    }

    @Test
    void executeSchedule_shouldUpdateLastRunAtAndNextRunAt() {
        var schedule = createSchedule(ReportType.DAILY_MUSTER, ScheduleFrequency.DAILY);
        schedule.setRecipients(null);
        var now = LocalDateTime.of(2025, 6, 2, 8, 0);

        when(attendanceReportService.exportReportAsCsv(anyString(), any())).thenReturn("data".getBytes());

        executor.executeSchedule(schedule, now);

        assertEquals(now, schedule.getLastRunAt());
        assertEquals(LocalDateTime.of(2025, 6, 3, 8, 0), schedule.getNextRunAt());
    }

    // ===== buildReportParams tests =====

    @Test
    void buildReportParams_dailyMuster_shouldSetYesterdayDate() {
        var schedule = createSchedule(ReportType.DAILY_MUSTER, ScheduleFrequency.DAILY);
        var now = LocalDateTime.of(2025, 6, 3, 8, 0);

        Map<String, String> params = executor.buildReportParams(schedule, now);

        assertEquals("2025-06-02", params.get("date"));
        assertEquals("1", params.get("departmentId"));
    }

    @Test
    void buildReportParams_monthlySummary_shouldSetPreviousMonth() {
        var schedule = createSchedule(ReportType.MONTHLY_SUMMARY, ScheduleFrequency.MONTHLY);
        var now = LocalDateTime.of(2025, 7, 1, 8, 0);

        Map<String, String> params = executor.buildReportParams(schedule, now);

        assertEquals("2025", params.get("year"));
        assertEquals("6", params.get("month"));
        assertEquals("1", params.get("departmentId"));
    }

    @Test
    void buildReportParams_monthlySummary_january_shouldUsePreviousYearDecember() {
        var schedule = createSchedule(ReportType.MONTHLY_SUMMARY, ScheduleFrequency.MONTHLY);
        var now = LocalDateTime.of(2025, 1, 1, 8, 0);

        Map<String, String> params = executor.buildReportParams(schedule, now);

        assertEquals("2024", params.get("year"));
        assertEquals("12", params.get("month"));
    }

    @Test
    void buildReportParams_absenteeList_shouldSetYesterdayDateRange() {
        var schedule = createSchedule(ReportType.ABSENTEE_LIST, ScheduleFrequency.DAILY);
        var now = LocalDateTime.of(2025, 6, 3, 8, 0);

        Map<String, String> params = executor.buildReportParams(schedule, now);

        assertEquals("2025-06-02", params.get("startDate"));
        assertEquals("2025-06-02", params.get("endDate"));
        assertEquals("1", params.get("departmentId"));
    }

    @Test
    void buildReportParams_withNoDepartment_shouldOmitDepartmentId() {
        var schedule = createSchedule(ReportType.DAILY_MUSTER, ScheduleFrequency.DAILY);
        schedule.setDepartment(null);
        var now = LocalDateTime.of(2025, 6, 3, 8, 0);

        Map<String, String> params = executor.buildReportParams(schedule, now);

        assertFalse(params.containsKey("departmentId"));
    }

    // ===== calculateNextRunAt tests =====

    @Test
    void calculateNextRunAt_daily_shouldAddOneDay() {
        var from = LocalDateTime.of(2025, 6, 1, 8, 0);

        var next = ScheduledReportExecutor.calculateNextRunAt(from, ScheduleFrequency.DAILY);

        assertEquals(LocalDateTime.of(2025, 6, 2, 8, 0), next);
    }

    @Test
    void calculateNextRunAt_weekly_shouldAddOneWeek() {
        var from = LocalDateTime.of(2025, 6, 1, 8, 0);

        var next = ScheduledReportExecutor.calculateNextRunAt(from, ScheduleFrequency.WEEKLY);

        assertEquals(LocalDateTime.of(2025, 6, 8, 8, 0), next);
    }

    @Test
    void calculateNextRunAt_monthly_shouldAddOneMonth() {
        var from = LocalDateTime.of(2025, 6, 1, 8, 0);

        var next = ScheduledReportExecutor.calculateNextRunAt(from, ScheduleFrequency.MONTHLY);

        assertEquals(LocalDateTime.of(2025, 7, 1, 8, 0), next);
    }

    @Test
    void calculateNextRunAt_monthly_endOfMonth_shouldHandleCorrectly() {
        var from = LocalDateTime.of(2025, 1, 31, 8, 0);

        var next = ScheduledReportExecutor.calculateNextRunAt(from, ScheduleFrequency.MONTHLY);

        assertEquals(LocalDateTime.of(2025, 2, 28, 8, 0), next);
    }

    // ===== parseRecipients tests =====

    @Test
    void parseRecipients_commaDelimited_shouldSplit() {
        var result = ScheduledReportExecutor.parseRecipients("a@test.com,b@test.com");
        assertArrayEquals(new String[]{"a@test.com", "b@test.com"}, result);
    }

    @Test
    void parseRecipients_semicolonDelimited_shouldSplit() {
        var result = ScheduledReportExecutor.parseRecipients("a@test.com;b@test.com");
        assertArrayEquals(new String[]{"a@test.com", "b@test.com"}, result);
    }

    @Test
    void parseRecipients_spaceDelimited_shouldSplit() {
        var result = ScheduledReportExecutor.parseRecipients("a@test.com b@test.com");
        assertArrayEquals(new String[]{"a@test.com", "b@test.com"}, result);
    }

    @Test
    void parseRecipients_singleRecipient_shouldReturnArray() {
        var result = ScheduledReportExecutor.parseRecipients("admin@test.com");
        assertArrayEquals(new String[]{"admin@test.com"}, result);
    }

    @Test
    void parseRecipients_null_shouldReturnEmptyArray() {
        var result = ScheduledReportExecutor.parseRecipients(null);
        assertEquals(0, result.length);
    }

    @Test
    void parseRecipients_blank_shouldReturnEmptyArray() {
        var result = ScheduledReportExecutor.parseRecipients("   ");
        assertEquals(0, result.length);
    }
}
