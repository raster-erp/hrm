package com.raster.hrm.attendancereport;

import com.raster.hrm.attendance.entity.AttendancePunch;
import com.raster.hrm.attendance.entity.PunchDirection;
import com.raster.hrm.attendance.repository.AttendancePunchRepository;
import com.raster.hrm.attendancereport.dto.AbsenteeListReport;
import com.raster.hrm.attendancereport.dto.DailyMusterReport;
import com.raster.hrm.attendancereport.dto.MonthlySummaryReport;
import com.raster.hrm.attendancereport.service.AttendanceReportService;
import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.shiftroster.repository.ShiftRosterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceReportServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendancePunchRepository attendancePunchRepository;

    @Mock
    private ShiftRosterRepository shiftRosterRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private AttendanceReportService attendanceReportService;

    private Employee createEmployee(Long id, String code, String first, String last) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName(first);
        employee.setLastName(last);
        var dept = createDepartment();
        employee.setDepartment(dept);
        return employee;
    }

    private Employee createEmployeeNoDept(Long id, String code, String first, String last) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName(first);
        employee.setLastName(last);
        return employee;
    }

    private Department createDepartment() {
        var dept = new Department();
        dept.setId(1L);
        dept.setName("Engineering");
        dept.setCode("ENG");
        return dept;
    }

    private AttendancePunch createPunch(Long employeeId, LocalDateTime punchTime, PunchDirection direction) {
        var punch = new AttendancePunch();
        punch.setId(1L);
        punch.setPunchTime(punchTime);
        punch.setDirection(direction);
        return punch;
    }

    // ===== Daily Muster Tests =====

    @Test
    void generateDailyMuster_withDepartment_shouldReturnReport() {
        var date = LocalDate.of(2025, 1, 15);
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));

        var punchIn = createPunch(1L, date.atTime(9, 0), PunchDirection.IN);
        var punchOut = createPunch(1L, date.atTime(18, 0), PunchDirection.OUT);
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(eq(1L), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(punchIn, punchOut)));

        var report = attendanceReportService.generateDailyMuster(date, 1L);

        assertNotNull(report);
        assertEquals(date, report.date());
        assertEquals(1L, report.departmentId());
        assertEquals("Engineering", report.departmentName());
        assertEquals(1, report.entries().size());
        assertEquals("PRESENT", report.entries().get(0).status());
        assertEquals(1, report.totalPresent());
        assertEquals(0, report.totalAbsent());
        assertEquals(0, report.totalIncomplete());
    }

    @Test
    void generateDailyMuster_withNullDepartment_shouldReturnAllEmployees() {
        var date = LocalDate.of(2025, 1, 15);
        var emp1 = createEmployee(1L, "EMP001", "John", "Doe");
        var emp2 = createEmployee(2L, "EMP002", "Jane", "Smith");

        when(employeeRepository.findByDeletedFalse(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(emp1, emp2)));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var report = attendanceReportService.generateDailyMuster(date, null);

        assertNotNull(report);
        assertNull(report.departmentId());
        assertEquals("All Departments", report.departmentName());
        assertEquals(2, report.entries().size());
        assertEquals(0, report.totalPresent());
        assertEquals(2, report.totalAbsent());
    }

    @Test
    void generateDailyMuster_employeeAbsent_shouldReturnAbsentStatus() {
        var date = LocalDate.of(2025, 1, 15);
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(eq(1L), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var report = attendanceReportService.generateDailyMuster(date, 1L);

        assertEquals(1, report.entries().size());
        var entry = report.entries().get(0);
        assertEquals("ABSENT", entry.status());
        assertNull(entry.firstPunchIn());
        assertNull(entry.lastPunchOut());
        assertEquals(0, entry.totalPunches());
        assertEquals(0, report.totalPresent());
        assertEquals(1, report.totalAbsent());
    }

    @Test
    void generateDailyMuster_onlyPunchIn_shouldReturnIncompleteStatus() {
        var date = LocalDate.of(2025, 1, 15);
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));

        var punchIn = createPunch(1L, date.atTime(9, 0), PunchDirection.IN);
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(eq(1L), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(punchIn)));

        var report = attendanceReportService.generateDailyMuster(date, 1L);

        assertEquals(1, report.entries().size());
        assertEquals("INCOMPLETE", report.entries().get(0).status());
        assertEquals(0, report.totalPresent());
        assertEquals(0, report.totalAbsent());
        assertEquals(1, report.totalIncomplete());
    }

    @Test
    void generateDailyMuster_onlyPunchOut_shouldReturnIncompleteStatus() {
        var date = LocalDate.of(2025, 1, 15);
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));

        var punchOut = createPunch(1L, date.atTime(18, 0), PunchDirection.OUT);
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(eq(1L), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(punchOut)));

        var report = attendanceReportService.generateDailyMuster(date, 1L);

        assertEquals("INCOMPLETE", report.entries().get(0).status());
        assertEquals(1, report.totalIncomplete());
    }

    @Test
    void generateDailyMuster_multiplePunches_shouldFindFirstInAndLastOut() {
        var date = LocalDate.of(2025, 1, 15);
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));

        var punch1 = createPunch(1L, date.atTime(9, 0), PunchDirection.IN);
        var punch2 = createPunch(1L, date.atTime(12, 0), PunchDirection.OUT);
        var punch3 = createPunch(1L, date.atTime(13, 0), PunchDirection.IN);
        var punch4 = createPunch(1L, date.atTime(18, 30), PunchDirection.OUT);
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(eq(1L), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(punch1, punch2, punch3, punch4)));

        var report = attendanceReportService.generateDailyMuster(date, 1L);

        var entry = report.entries().get(0);
        assertEquals("PRESENT", entry.status());
        assertEquals(date.atTime(9, 0), entry.firstPunchIn());
        assertEquals(date.atTime(18, 30), entry.lastPunchOut());
        assertEquals(4, entry.totalPunches());
    }

    @Test
    void generateDailyMuster_departmentNotFound_shouldThrowException() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceReportService.generateDailyMuster(LocalDate.now(), 999L));
    }

    @Test
    void generateDailyMuster_employeeNoDepartment_shouldHandleNullDeptName() {
        var date = LocalDate.of(2025, 1, 15);
        var employee = createEmployeeNoDept(1L, "EMP001", "John", "Doe");

        when(employeeRepository.findByDeletedFalse(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(employee)));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var report = attendanceReportService.generateDailyMuster(date, null);

        assertEquals(1, report.entries().size());
        assertNull(report.entries().get(0).departmentName());
    }

    // ===== Monthly Summary Tests =====

    @Test
    void generateMonthlySummary_shouldReturnCorrectCounts() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));

        // Jan 2025 has 23 working days (Mon-Fri)
        // Mock: first 10 working days have punches (IN+OUT), rest are empty
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(eq(1L), any(), any(), any()))
                .thenAnswer(invocation -> {
                    LocalDateTime from = invocation.getArgument(1);
                    int dayOfMonth = from.getDayOfMonth();
                    if (dayOfMonth <= 14) {
                        var punchIn = createPunch(1L, from.withHour(9), PunchDirection.IN);
                        var punchOut = createPunch(1L, from.withHour(18), PunchDirection.OUT);
                        return new PageImpl<>(List.of(punchIn, punchOut));
                    }
                    return new PageImpl<>(Collections.emptyList());
                });

        var report = attendanceReportService.generateMonthlySummary(2025, 1, 1L);

        assertNotNull(report);
        assertEquals(2025, report.year());
        assertEquals(1, report.month());
        assertEquals(1L, report.departmentId());
        assertEquals("Engineering", report.departmentName());
        assertEquals(1, report.entries().size());

        var entry = report.entries().get(0);
        assertEquals(23, entry.totalWorkingDays());
        assertTrue(entry.totalPresent() > 0);
        assertEquals(23, entry.totalPresent() + entry.totalAbsent() + entry.totalIncomplete());
    }

    @Test
    void generateMonthlySummary_withNullDepartment_shouldReturnAllEmployees() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(employeeRepository.findByDeletedFalse(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(employee)));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var report = attendanceReportService.generateMonthlySummary(2025, 1, null);

        assertNull(report.departmentId());
        assertEquals("All Departments", report.departmentName());
        assertEquals(1, report.entries().size());
    }

    @Test
    void generateMonthlySummary_allAbsent_shouldReturnAllAbsentDays() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var report = attendanceReportService.generateMonthlySummary(2025, 1, 1L);

        var entry = report.entries().get(0);
        assertEquals(0, entry.totalPresent());
        assertEquals(23, entry.totalAbsent());
        assertEquals(0, entry.totalIncomplete());
        assertEquals(23, entry.totalWorkingDays());
    }

    @Test
    void generateMonthlySummary_employeeNoDepartment_shouldHandleNull() {
        var employee = createEmployeeNoDept(1L, "EMP001", "John", "Doe");

        when(employeeRepository.findByDeletedFalse(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(employee)));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var report = attendanceReportService.generateMonthlySummary(2025, 1, null);

        assertNull(report.entries().get(0).departmentName());
    }

    // ===== Absentee List Tests =====

    @Test
    void generateAbsenteeList_shouldReturnAbsentEmployees() {
        var startDate = LocalDate.of(2025, 1, 6); // Monday
        var endDate = LocalDate.of(2025, 1, 10);   // Friday

        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var report = attendanceReportService.generateAbsenteeList(startDate, endDate, 1L);

        assertNotNull(report);
        assertEquals(startDate, report.startDate());
        assertEquals(endDate, report.endDate());
        assertEquals(1L, report.departmentId());
        assertEquals("Engineering", report.departmentName());
        assertEquals(5, report.entries().size());
        assertEquals(5, report.totalAbsentInstances());
    }

    @Test
    void generateAbsenteeList_withNullDepartment_shouldReturnAllEmployees() {
        var startDate = LocalDate.of(2025, 1, 6);
        var endDate = LocalDate.of(2025, 1, 6);

        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(employeeRepository.findByDeletedFalse(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(employee)));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var report = attendanceReportService.generateAbsenteeList(startDate, endDate, null);

        assertNull(report.departmentId());
        assertEquals("All Departments", report.departmentName());
        assertEquals(1, report.entries().size());
    }

    @Test
    void generateAbsenteeList_startDateAfterEndDate_shouldThrowException() {
        var startDate = LocalDate.of(2025, 1, 10);
        var endDate = LocalDate.of(2025, 1, 6);

        assertThrows(BadRequestException.class,
                () -> attendanceReportService.generateAbsenteeList(startDate, endDate, null));
    }

    @Test
    void generateAbsenteeList_employeePresent_shouldNotBeInList() {
        var startDate = LocalDate.of(2025, 1, 6);
        var endDate = LocalDate.of(2025, 1, 6);

        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));

        var punch = createPunch(1L, startDate.atTime(9, 0), PunchDirection.IN);
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(punch)));

        var report = attendanceReportService.generateAbsenteeList(startDate, endDate, 1L);

        assertEquals(0, report.entries().size());
        assertEquals(0, report.totalAbsentInstances());
    }

    @Test
    void generateAbsenteeList_weekendDays_shouldBeExcluded() {
        // Saturday and Sunday
        var startDate = LocalDate.of(2025, 1, 4); // Saturday
        var endDate = LocalDate.of(2025, 1, 5);   // Sunday

        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));

        var report = attendanceReportService.generateAbsenteeList(startDate, endDate, 1L);

        assertEquals(0, report.entries().size());
    }

    @Test
    void generateAbsenteeList_employeeNoDepartment_shouldHandleNull() {
        var startDate = LocalDate.of(2025, 1, 6);
        var endDate = LocalDate.of(2025, 1, 6);

        var employee = createEmployeeNoDept(1L, "EMP001", "John", "Doe");

        when(employeeRepository.findByDeletedFalse(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(employee)));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var report = attendanceReportService.generateAbsenteeList(startDate, endDate, null);

        assertEquals(1, report.entries().size());
        assertNull(report.entries().get(0).departmentName());
    }

    // ===== CSV Export Tests =====

    @Test
    void exportReportAsCsv_dailyMuster_shouldReturnCsvBytes() {
        var date = LocalDate.of(2025, 1, 15);
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));

        var punchIn = createPunch(1L, date.atTime(9, 0), PunchDirection.IN);
        var punchOut = createPunch(1L, date.atTime(18, 0), PunchDirection.OUT);
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(eq(1L), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(punchIn, punchOut)));

        var params = new HashMap<String, String>();
        params.put("date", "2025-01-15");
        params.put("departmentId", "1");

        var csv = attendanceReportService.exportReportAsCsv("DAILY_MUSTER", params);

        assertNotNull(csv);
        var csvContent = new String(csv);
        assertTrue(csvContent.contains("Employee ID,Employee Code,Employee Name"));
        assertTrue(csvContent.contains("EMP001"));
        assertTrue(csvContent.contains("PRESENT"));
    }

    @Test
    void exportReportAsCsv_monthlySummary_shouldReturnCsvBytes() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var params = new HashMap<String, String>();
        params.put("year", "2025");
        params.put("month", "1");
        params.put("departmentId", "1");

        var csv = attendanceReportService.exportReportAsCsv("MONTHLY_SUMMARY", params);

        assertNotNull(csv);
        var csvContent = new String(csv);
        assertTrue(csvContent.contains("Employee ID,Employee Code,Employee Name,Department"));
        assertTrue(csvContent.contains("EMP001"));
    }

    @Test
    void exportReportAsCsv_absenteeList_shouldReturnCsvBytes() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var params = new HashMap<String, String>();
        params.put("startDate", "2025-01-06");
        params.put("endDate", "2025-01-06");
        params.put("departmentId", "1");

        var csv = attendanceReportService.exportReportAsCsv("ABSENTEE_LIST", params);

        assertNotNull(csv);
        var csvContent = new String(csv);
        assertTrue(csvContent.contains("Employee ID,Employee Code,Employee Name,Department,Absent Date"));
        assertTrue(csvContent.contains("EMP001"));
    }

    @Test
    void exportReportAsCsv_invalidReportType_shouldThrowException() {
        var params = new HashMap<String, String>();

        assertThrows(BadRequestException.class,
                () -> attendanceReportService.exportReportAsCsv("INVALID_TYPE", params));
    }

    @Test
    void exportReportAsCsv_dailyMuster_withoutDepartmentId_shouldWork() {
        var date = LocalDate.of(2025, 1, 15);
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(employeeRepository.findByDeletedFalse(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(employee)));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var params = new HashMap<String, String>();
        params.put("date", "2025-01-15");

        var csv = attendanceReportService.exportReportAsCsv("DAILY_MUSTER", params);

        assertNotNull(csv);
        assertTrue(new String(csv).contains("ABSENT"));
    }

    @Test
    void exportReportAsCsv_dailyMuster_csvEscapingWithComma() {
        var date = LocalDate.of(2025, 1, 15);
        var employee = createEmployee(1L, "EMP001", "Doe, John", "Jr");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        var params = new HashMap<String, String>();
        params.put("date", "2025-01-15");
        params.put("departmentId", "1");

        var csv = attendanceReportService.exportReportAsCsv("DAILY_MUSTER", params);

        assertNotNull(csv);
        // Name with comma should be quoted
        assertTrue(new String(csv).contains("\"Doe, John Jr\""));
    }

    @Test
    void generateDailyMuster_noEmployees_shouldReturnEmptyReport() {
        var date = LocalDate.of(2025, 1, 15);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(Collections.emptyList());

        var report = attendanceReportService.generateDailyMuster(date, 1L);

        assertNotNull(report);
        assertTrue(report.entries().isEmpty());
        assertEquals(0, report.totalPresent());
        assertEquals(0, report.totalAbsent());
        assertEquals(0, report.totalIncomplete());
    }

    @Test
    void generateMonthlySummary_incompleteDay_shouldCountAsIncomplete() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(createDepartment()));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(employee));

        // Only IN punches (no OUT) for every day
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    LocalDateTime from = invocation.getArgument(1);
                    var punchIn = createPunch(1L, from.withHour(9), PunchDirection.IN);
                    return new PageImpl<>(List.of(punchIn));
                });

        var report = attendanceReportService.generateMonthlySummary(2025, 1, 1L);

        var entry = report.entries().get(0);
        assertEquals(0, entry.totalPresent());
        assertEquals(0, entry.totalAbsent());
        assertEquals(23, entry.totalIncomplete());
    }
}
