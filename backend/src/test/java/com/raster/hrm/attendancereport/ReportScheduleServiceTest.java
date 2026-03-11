package com.raster.hrm.attendancereport;

import com.raster.hrm.attendancereport.dto.ReportScheduleRequest;
import com.raster.hrm.attendancereport.dto.ReportScheduleResponse;
import com.raster.hrm.attendancereport.entity.ReportFormat;
import com.raster.hrm.attendancereport.entity.ReportSchedule;
import com.raster.hrm.attendancereport.entity.ReportType;
import com.raster.hrm.attendancereport.entity.ScheduleFrequency;
import com.raster.hrm.attendancereport.repository.ReportScheduleRepository;
import com.raster.hrm.attendancereport.service.ReportScheduleService;
import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportScheduleServiceTest {

    @Mock
    private ReportScheduleRepository reportScheduleRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private ReportScheduleService reportScheduleService;

    private Department createDepartment() {
        var dept = new Department();
        dept.setId(1L);
        dept.setName("Engineering");
        dept.setCode("ENG");
        return dept;
    }

    private ReportSchedule createSchedule(Long id) {
        var schedule = new ReportSchedule();
        schedule.setId(id);
        schedule.setReportName("Daily Muster Report");
        schedule.setReportType(ReportType.DAILY_MUSTER);
        schedule.setFrequency(ScheduleFrequency.DAILY);
        schedule.setDepartment(createDepartment());
        schedule.setRecipients("admin@example.com");
        schedule.setExportFormat(ReportFormat.CSV);
        schedule.setActive(true);
        schedule.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        schedule.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        return schedule;
    }

    private ReportScheduleRequest createRequest() {
        return new ReportScheduleRequest(
                "Daily Muster Report",
                "DAILY_MUSTER",
                "DAILY",
                1L,
                "admin@example.com",
                "CSV"
        );
    }

    // ===== getAll tests =====

    @Test
    void getAll_shouldReturnPageOfSchedules() {
        var schedules = List.of(createSchedule(1L), createSchedule(2L));
        var page = new PageImpl<>(schedules, PageRequest.of(0, 20), 2);
        when(reportScheduleRepository.findAll(any(Pageable.class))).thenReturn(page);

        var result = reportScheduleService.getAll(PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Daily Muster Report", result.getContent().get(0).reportName());
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var page = new PageImpl<ReportSchedule>(List.of(), PageRequest.of(0, 20), 0);
        when(reportScheduleRepository.findAll(any(Pageable.class))).thenReturn(page);

        var result = reportScheduleService.getAll(PageRequest.of(0, 20));

        assertTrue(result.getContent().isEmpty());
    }

    // ===== getById tests =====

    @Test
    void getById_shouldReturnSchedule() {
        var schedule = createSchedule(1L);
        when(reportScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        var result = reportScheduleService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Daily Muster Report", result.reportName());
        assertEquals("DAILY_MUSTER", result.reportType());
        assertEquals("DAILY", result.frequency());
        assertEquals(1L, result.departmentId());
        assertEquals("Engineering", result.departmentName());
        assertEquals("admin@example.com", result.recipients());
        assertEquals("CSV", result.exportFormat());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowNotFound() {
        when(reportScheduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reportScheduleService.getById(999L));
    }

    // ===== getByReportType tests =====

    @Test
    void getByReportType_shouldReturnFilteredSchedules() {
        var schedules = List.of(createSchedule(1L));
        var page = new PageImpl<>(schedules);
        when(reportScheduleRepository.findByReportType(eq(ReportType.DAILY_MUSTER), any()))
                .thenReturn(page);

        var result = reportScheduleService.getByReportType("DAILY_MUSTER", PageRequest.of(0, 20));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getByReportType_invalidType_shouldThrowException() {
        assertThrows(BadRequestException.class,
                () -> reportScheduleService.getByReportType("INVALID", PageRequest.of(0, 20)));
    }

    // ===== create tests =====

    @Test
    void create_shouldCreateAndReturnSchedule() {
        var request = createRequest();
        var department = createDepartment();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(reportScheduleRepository.save(any(ReportSchedule.class))).thenAnswer(invocation -> {
            var schedule = (ReportSchedule) invocation.getArgument(0);
            schedule.setId(1L);
            schedule.setCreatedAt(LocalDateTime.now());
            schedule.setUpdatedAt(LocalDateTime.now());
            return schedule;
        });

        var result = reportScheduleService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Daily Muster Report", result.reportName());
        assertEquals("DAILY_MUSTER", result.reportType());
        assertEquals("DAILY", result.frequency());
        assertEquals("CSV", result.exportFormat());
        verify(reportScheduleRepository).save(any(ReportSchedule.class));
    }

    @Test
    void create_withNullDepartment_shouldCreateWithoutDepartment() {
        var request = new ReportScheduleRequest(
                "All Depts Report", "MONTHLY_SUMMARY", "MONTHLY", null, "admin@example.com", "CSV"
        );
        when(reportScheduleRepository.save(any(ReportSchedule.class))).thenAnswer(invocation -> {
            var schedule = (ReportSchedule) invocation.getArgument(0);
            schedule.setId(1L);
            schedule.setCreatedAt(LocalDateTime.now());
            schedule.setUpdatedAt(LocalDateTime.now());
            return schedule;
        });

        var result = reportScheduleService.create(request);

        assertNotNull(result);
        assertNull(result.departmentId());
        assertNull(result.departmentName());
    }

    @Test
    void create_withNullExportFormat_shouldDefaultToCsv() {
        var request = new ReportScheduleRequest(
                "Report", "DAILY_MUSTER", "DAILY", null, null, null
        );
        when(reportScheduleRepository.save(any(ReportSchedule.class))).thenAnswer(invocation -> {
            var schedule = (ReportSchedule) invocation.getArgument(0);
            schedule.setId(1L);
            schedule.setCreatedAt(LocalDateTime.now());
            schedule.setUpdatedAt(LocalDateTime.now());
            return schedule;
        });

        var result = reportScheduleService.create(request);

        assertEquals("CSV", result.exportFormat());
    }

    @Test
    void create_withBlankExportFormat_shouldDefaultToCsv() {
        var request = new ReportScheduleRequest(
                "Report", "DAILY_MUSTER", "DAILY", null, null, "  "
        );
        when(reportScheduleRepository.save(any(ReportSchedule.class))).thenAnswer(invocation -> {
            var schedule = (ReportSchedule) invocation.getArgument(0);
            schedule.setId(1L);
            schedule.setCreatedAt(LocalDateTime.now());
            schedule.setUpdatedAt(LocalDateTime.now());
            return schedule;
        });

        var result = reportScheduleService.create(request);

        assertEquals("CSV", result.exportFormat());
    }

    @Test
    void create_invalidReportType_shouldThrowException() {
        var request = new ReportScheduleRequest("Name", "INVALID", "DAILY", null, null, null);

        assertThrows(BadRequestException.class, () -> reportScheduleService.create(request));
    }

    @Test
    void create_invalidFrequency_shouldThrowException() {
        var request = new ReportScheduleRequest("Name", "DAILY_MUSTER", "INVALID", null, null, null);

        assertThrows(BadRequestException.class, () -> reportScheduleService.create(request));
    }

    @Test
    void create_invalidExportFormat_shouldThrowException() {
        var request = new ReportScheduleRequest("Name", "DAILY_MUSTER", "DAILY", null, null, "INVALID");

        assertThrows(BadRequestException.class, () -> reportScheduleService.create(request));
    }

    @Test
    void create_departmentNotFound_shouldThrowException() {
        var request = new ReportScheduleRequest("Name", "DAILY_MUSTER", "DAILY", 999L, null, "CSV");
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reportScheduleService.create(request));
    }

    // ===== update tests =====

    @Test
    void update_shouldUpdateAndReturnSchedule() {
        var schedule = createSchedule(1L);
        var request = new ReportScheduleRequest(
                "Updated Report", "MONTHLY_SUMMARY", "WEEKLY", 1L, "new@example.com", "EXCEL"
        );
        var department = createDepartment();

        when(reportScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(reportScheduleRepository.save(any(ReportSchedule.class))).thenReturn(schedule);

        var result = reportScheduleService.update(1L, request);

        assertNotNull(result);
        verify(reportScheduleRepository).save(any(ReportSchedule.class));
    }

    @Test
    void update_notFound_shouldThrowException() {
        var request = createRequest();
        when(reportScheduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reportScheduleService.update(999L, request));
    }

    // ===== toggleActive tests =====

    @Test
    void toggleActive_shouldToggleFromTrueToFalse() {
        var schedule = createSchedule(1L);
        schedule.setActive(true);

        when(reportScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(reportScheduleRepository.save(any(ReportSchedule.class))).thenReturn(schedule);

        var result = reportScheduleService.toggleActive(1L);

        assertNotNull(result);
        assertFalse(result.active());
    }

    @Test
    void toggleActive_shouldToggleFromFalseToTrue() {
        var schedule = createSchedule(1L);
        schedule.setActive(false);

        when(reportScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(reportScheduleRepository.save(any(ReportSchedule.class))).thenReturn(schedule);

        var result = reportScheduleService.toggleActive(1L);

        assertNotNull(result);
        assertTrue(result.active());
    }

    @Test
    void toggleActive_notFound_shouldThrowException() {
        when(reportScheduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reportScheduleService.toggleActive(999L));
    }

    // ===== delete tests =====

    @Test
    void delete_shouldDeleteSchedule() {
        var schedule = createSchedule(1L);
        when(reportScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        reportScheduleService.delete(1L);

        verify(reportScheduleRepository).delete(schedule);
    }

    @Test
    void delete_notFound_shouldThrowException() {
        when(reportScheduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reportScheduleService.delete(999L));
    }

    // ===== mapToResponse tests =====

    @Test
    void getById_withNoDepartment_shouldReturnNullDepartmentFields() {
        var schedule = createSchedule(1L);
        schedule.setDepartment(null);
        when(reportScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        var result = reportScheduleService.getById(1L);

        assertNull(result.departmentId());
        assertNull(result.departmentName());
    }

    @Test
    void getById_shouldMapAllFields() {
        var schedule = createSchedule(1L);
        schedule.setLastRunAt(LocalDateTime.of(2025, 1, 15, 8, 0));
        schedule.setNextRunAt(LocalDateTime.of(2025, 1, 16, 8, 0));
        when(reportScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        var result = reportScheduleService.getById(1L);

        assertEquals(LocalDateTime.of(2025, 1, 15, 8, 0), result.lastRunAt());
        assertEquals(LocalDateTime.of(2025, 1, 16, 8, 0), result.nextRunAt());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), result.updatedAt());
    }
}
