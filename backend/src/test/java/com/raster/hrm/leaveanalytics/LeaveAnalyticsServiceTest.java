package com.raster.hrm.leaveanalytics;

import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.designation.entity.Designation;
import com.raster.hrm.designation.repository.DesignationRepository;
import com.raster.hrm.designation.entity.Designation;
import com.raster.hrm.designation.repository.DesignationRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leaveanalytics.service.LeaveAnalyticsService;
import com.raster.hrm.leaveapplication.entity.LeaveApplication;
import com.raster.hrm.leaveapplication.entity.LeaveApplicationStatus;
import com.raster.hrm.leaveapplication.repository.LeaveApplicationRepository;
import com.raster.hrm.leavebalance.entity.LeaveBalance;
import com.raster.hrm.leavebalance.repository.LeaveBalanceRepository;
import com.raster.hrm.leavetype.entity.LeaveType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveAnalyticsServiceTest {

    @Mock
    private LeaveApplicationRepository leaveApplicationRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DesignationRepository designationRepository;

    @InjectMocks
    private LeaveAnalyticsService leaveAnalyticsService;

    private Employee createEmployee(Long id, String code, String first, String last, Department department) {
        var emp = new Employee();
        emp.setId(id);
        emp.setEmployeeCode(code);
        emp.setFirstName(first);
        emp.setLastName(last);
        emp.setDepartment(department);
        emp.setEmail(code.toLowerCase() + "@test.com");
        return emp;
    }

    private Department createDepartment(Long id, String name) {
        var dept = new Department();
        dept.setId(id);
        dept.setName(name);
        return dept;
    }

    private LeaveType createLeaveType(Long id, String name) {
        var lt = new LeaveType();
        lt.setId(id);
        lt.setName(name);
        return lt;
    }

    private Designation createDesignation(Long id, String title) {
        var d = new Designation();
        d.setId(id);
        d.setTitle(title);
        d.setCode("DESIG-" + id);
        return d;
    }

    private LeaveApplication createApplication(Long id, Employee emp, LeaveType lt,
                                                LocalDate from, LocalDate to, BigDecimal days) {
        var app = new LeaveApplication();
        app.setId(id);
        app.setEmployee(emp);
        app.setLeaveType(lt);
        app.setFromDate(from);
        app.setToDate(to);
        app.setNumberOfDays(days);
        app.setStatus(LeaveApplicationStatus.APPROVED);
        return app;
    }

    private LeaveBalance createBalance(Long id, Employee emp, LeaveType lt, int year,
                                        BigDecimal credited, BigDecimal used, BigDecimal available) {
        var bal = new LeaveBalance();
        bal.setId(id);
        bal.setEmployee(emp);
        bal.setLeaveType(lt);
        bal.setYear(year);
        bal.setCredited(credited);
        bal.setUsed(used);
        bal.setAvailable(available);
        return bal;
    }

    // ===== Leave Trend Tests =====

    @Test
    void generateLeaveTrend_shouldReturnTrendData() {
        var dept = createDepartment(1L, "Engineering");
        var emp = createEmployee(1L, "EMP001", "John", "Doe", dept);
        var lt = createLeaveType(1L, "Annual Leave");
        var app = createApplication(1L, emp, lt,
                LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 8), new BigDecimal("3"));

        when(leaveApplicationRepository.findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                eq(LeaveApplicationStatus.APPROVED), any(), any()))
                .thenReturn(List.of(app));

        var report = leaveAnalyticsService.generateLeaveTrend(2025, 1, 2025, 3, null, null, null, null);

        assertNotNull(report);
        assertEquals(2025, report.startYear());
        assertEquals(1, report.startMonth());
        assertEquals(2025, report.endYear());
        assertEquals(3, report.endMonth());
        assertEquals("All Departments", report.departmentName());
        assertTrue(report.entries().size() >= 3);
    }

    @Test
    void generateLeaveTrend_withDepartment_shouldReturnFilteredData() {
        var dept = createDepartment(1L, "Engineering");
        var emp = createEmployee(1L, "EMP001", "John", "Doe", dept);
        var lt = createLeaveType(1L, "Sick Leave");
        var app = createApplication(1L, emp, lt,
                LocalDate.of(2025, 2, 10), LocalDate.of(2025, 2, 11), new BigDecimal("2"));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(leaveApplicationRepository
                .findByEmployee_Department_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        eq(1L), eq(LeaveApplicationStatus.APPROVED), any(), any()))
                .thenReturn(List.of(app));

        var report = leaveAnalyticsService.generateLeaveTrend(2025, 1, 2025, 3, 1L, null, null, null);

        assertNotNull(report);
        assertEquals(1L, report.departmentId());
        assertEquals("Engineering", report.departmentName());
    }

    @Test
    void generateLeaveTrend_invalidMonthRange_shouldThrow() {
        assertThrows(BadRequestException.class, () ->
                leaveAnalyticsService.generateLeaveTrend(2025, 6, 2025, 1, null, null, null, null));
    }

    @Test
    void generateLeaveTrend_invalidMonth_shouldThrow() {
        assertThrows(BadRequestException.class, () ->
                leaveAnalyticsService.generateLeaveTrend(2025, 0, 2025, 13, null, null, null, null));
    }

    @Test
    void generateLeaveTrend_noApplications_shouldReturnEmptyEntries() {
        when(leaveApplicationRepository.findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                any(), any(), any()))
                .thenReturn(List.of());

        var report = leaveAnalyticsService.generateLeaveTrend(2025, 1, 2025, 1, null, null, null, null);

        assertNotNull(report);
        assertEquals(1, report.entries().size());
        assertEquals(0, report.entries().get(0).applicationCount());
    }

    @Test
    void generateLeaveTrend_withDesignationFilter_shouldFilterByDesignation() {
        var dept = createDepartment(1L, "Engineering");
        var designation = createDesignation(1L, "Senior Engineer");
        var emp1 = createEmployee(1L, "EMP001", "John", "Doe", dept);
        emp1.setDesignation(designation);
        var emp2 = createEmployee(2L, "EMP002", "Jane", "Smith", dept);
        var lt = createLeaveType(1L, "Annual Leave");
        var app1 = createApplication(1L, emp1, lt,
                LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 8), new BigDecimal("3"));
        var app2 = createApplication(2L, emp2, lt,
                LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 12), new BigDecimal("3"));

        when(leaveApplicationRepository.findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                eq(LeaveApplicationStatus.APPROVED), any(), any()))
                .thenReturn(List.of(app1, app2));
        when(employeeRepository.findByDeletedFalse(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(emp1, emp2)));
        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));

        var report = leaveAnalyticsService.generateLeaveTrend(2025, 1, 2025, 1, null, 1L, null, null);

        assertNotNull(report);
        var totalApps = report.entries().stream().mapToLong(e -> e.applicationCount()).sum();
        assertEquals(1, totalApps);
    }

    @Test
    void generateLeaveTrend_withGenderFilter_shouldFilterByGender() {
        var dept = createDepartment(1L, "Engineering");
        var emp1 = createEmployee(1L, "EMP001", "John", "Doe", dept);
        emp1.setGender("Male");
        var emp2 = createEmployee(2L, "EMP002", "Jane", "Smith", dept);
        emp2.setGender("Female");
        var lt = createLeaveType(1L, "Annual Leave");
        var app1 = createApplication(1L, emp1, lt,
                LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 8), new BigDecimal("3"));
        var app2 = createApplication(2L, emp2, lt,
                LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 12), new BigDecimal("3"));

        when(leaveApplicationRepository.findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                eq(LeaveApplicationStatus.APPROVED), any(), any()))
                .thenReturn(List.of(app1, app2));
        when(employeeRepository.findByDeletedFalse(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(emp1, emp2)));

        var report = leaveAnalyticsService.generateLeaveTrend(2025, 1, 2025, 1, null, null, "Female", null);

        assertNotNull(report);
        var totalApps = report.entries().stream().mapToLong(e -> e.applicationCount()).sum();
        assertEquals(1, totalApps);
    }

    @Test
    void generateLeaveTrend_withAgeGroupFilter_shouldFilterByAge() {
        var dept = createDepartment(1L, "Engineering");
        var emp1 = createEmployee(1L, "EMP001", "John", "Doe", dept);
        emp1.setDateOfBirth(LocalDate.now().minusYears(30));
        var emp2 = createEmployee(2L, "EMP002", "Jane", "Smith", dept);
        emp2.setDateOfBirth(LocalDate.now().minusYears(50));
        var lt = createLeaveType(1L, "Annual Leave");
        var app1 = createApplication(1L, emp1, lt,
                LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 8), new BigDecimal("3"));
        var app2 = createApplication(2L, emp2, lt,
                LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 12), new BigDecimal("3"));

        when(leaveApplicationRepository.findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                eq(LeaveApplicationStatus.APPROVED), any(), any()))
                .thenReturn(List.of(app1, app2));
        when(employeeRepository.findByDeletedFalse(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(emp1, emp2)));

        var report = leaveAnalyticsService.generateLeaveTrend(2025, 1, 2025, 1, null, null, null, "25_34");

        assertNotNull(report);
        var totalApps = report.entries().stream().mapToLong(e -> e.applicationCount()).sum();
        assertEquals(1, totalApps);
    }

    // ===== Absenteeism Rate Tests =====

    @Test
    void generateAbsenteeismRate_shouldReturnRateByDepartment() {
        var dept = createDepartment(1L, "Engineering");
        var emp = createEmployee(1L, "EMP001", "John", "Doe", dept);
        var lt = createLeaveType(1L, "Sick Leave");
        var app = createApplication(1L, emp, lt,
                LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 8), new BigDecimal("3"));

        when(departmentRepository.findAll()).thenReturn(List.of(dept));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(emp));
        when(leaveApplicationRepository
                .findByEmployee_Department_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        eq(1L), eq(LeaveApplicationStatus.APPROVED), any(), any()))
                .thenReturn(List.of(app));

        var report = leaveAnalyticsService.generateAbsenteeismRate(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), null, null, null, null);

        assertNotNull(report);
        assertEquals(1, report.entries().size());
        assertEquals("Engineering", report.entries().get(0).departmentName());
        assertEquals(1, report.entries().get(0).employeeCount());
        assertTrue(report.overallRate().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void generateAbsenteeismRate_withSpecificDepartment_shouldFilter() {
        var dept = createDepartment(2L, "HR");
        var emp = createEmployee(2L, "EMP002", "Jane", "Smith", dept);

        when(departmentRepository.findById(2L)).thenReturn(Optional.of(dept));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(2L)).thenReturn(List.of(emp));
        when(leaveApplicationRepository
                .findByEmployee_Department_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        eq(2L), eq(LeaveApplicationStatus.APPROVED), any(), any()))
                .thenReturn(List.of());

        var report = leaveAnalyticsService.generateAbsenteeismRate(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 2L, null, null, null);

        assertNotNull(report);
        assertEquals(1, report.entries().size());
        assertEquals(BigDecimal.ZERO.setScale(2), report.entries().get(0).absenteeismRate());
    }

    @Test
    void generateAbsenteeismRate_startAfterEnd_shouldThrow() {
        assertThrows(BadRequestException.class, () ->
                leaveAnalyticsService.generateAbsenteeismRate(
                        LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1), null, null, null, null));
    }

    @Test
    void generateAbsenteeismRate_departmentNotFound_shouldThrow() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                leaveAnalyticsService.generateAbsenteeismRate(
                        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 999L, null, null, null));
    }

    @Test
    void generateAbsenteeismRate_noDepartmentEmployees_shouldSkip() {
        var dept = createDepartment(1L, "Empty Dept");
        when(departmentRepository.findAll()).thenReturn(List.of(dept));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of());

        var report = leaveAnalyticsService.generateAbsenteeismRate(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), null, null, null, null);

        assertNotNull(report);
        assertTrue(report.entries().isEmpty());
        assertEquals(BigDecimal.ZERO, report.overallRate());
    }

    // ===== Leave Utilization Tests =====

    @Test
    void generateLeaveUtilization_shouldReturnUtilizationData() {
        var dept = createDepartment(1L, "Engineering");
        var emp = createEmployee(1L, "EMP001", "John", "Doe", dept);
        var lt = createLeaveType(1L, "Annual Leave");
        var balance = createBalance(1L, emp, lt, 2025,
                new BigDecimal("20"), new BigDecimal("10"), new BigDecimal("10"));

        when(leaveBalanceRepository.findByYear(2025)).thenReturn(List.of(balance));
        when(employeeRepository.findByDeletedFalse(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(emp)));

        var report = leaveAnalyticsService.generateLeaveUtilization(2025, null, null, null, null);

        assertNotNull(report);
        assertEquals(2025, report.year());
        assertEquals("All Departments", report.departmentName());
        assertEquals(1, report.entries().size());
        assertEquals(new BigDecimal("50.00"), report.entries().get(0).utilizationPercent());
        assertEquals(new BigDecimal("50.00"), report.overallUtilization());
    }

    @Test
    void generateLeaveUtilization_withDepartment_shouldFilter() {
        var dept = createDepartment(1L, "Engineering");
        var emp1 = createEmployee(1L, "EMP001", "John", "Doe", dept);
        var emp2 = createEmployee(2L, "EMP002", "Jane", "Smith", dept);
        var lt = createLeaveType(1L, "Annual Leave");
        var balance1 = createBalance(1L, emp1, lt, 2025,
                new BigDecimal("20"), new BigDecimal("15"), new BigDecimal("5"));
        var balance2 = createBalance(2L, emp2, lt, 2025,
                new BigDecimal("20"), new BigDecimal("5"), new BigDecimal("15"));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(leaveBalanceRepository.findByYear(2025)).thenReturn(List.of(balance1, balance2));
        when(employeeRepository.findByDepartmentIdAndDeletedFalse(1L)).thenReturn(List.of(emp1, emp2));

        var report = leaveAnalyticsService.generateLeaveUtilization(2025, 1L, null, null, null);

        assertNotNull(report);
        assertEquals(1L, report.departmentId());
        assertEquals(2, report.entries().size());
        assertEquals(new BigDecimal("50.00"), report.overallUtilization());
    }

    @Test
    void generateLeaveUtilization_noBalances_shouldReturnEmpty() {
        when(leaveBalanceRepository.findByYear(2025)).thenReturn(List.of());
        when(employeeRepository.findByDeletedFalse(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));

        var report = leaveAnalyticsService.generateLeaveUtilization(2025, null, null, null, null);

        assertNotNull(report);
        assertTrue(report.entries().isEmpty());
        assertEquals(BigDecimal.ZERO, report.overallUtilization());
    }

    @Test
    void generateLeaveUtilization_zeroCredited_shouldReturnZeroPercent() {
        var dept = createDepartment(1L, "Engineering");
        var emp = createEmployee(1L, "EMP001", "John", "Doe", dept);
        var lt = createLeaveType(1L, "Annual Leave");
        var balance = createBalance(1L, emp, lt, 2025,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        when(leaveBalanceRepository.findByYear(2025)).thenReturn(List.of(balance));
        when(employeeRepository.findByDeletedFalse(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(emp)));

        var report = leaveAnalyticsService.generateLeaveUtilization(2025, null, null, null, null);

        assertNotNull(report);
        assertEquals(1, report.entries().size());
        assertEquals(BigDecimal.ZERO, report.entries().get(0).utilizationPercent());
    }

    @Test
    void generateLeaveUtilization_departmentNotFound_shouldThrow() {
        when(leaveBalanceRepository.findByYear(2025)).thenReturn(List.of());
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                leaveAnalyticsService.generateLeaveUtilization(2025, 999L, null, null, null));
    }

    @Test
    void generateLeaveUtilization_withGenderFilter_shouldFilter() {
        var dept = createDepartment(1L, "Engineering");
        var emp1 = createEmployee(1L, "EMP001", "John", "Doe", dept);
        emp1.setGender("Male");
        var emp2 = createEmployee(2L, "EMP002", "Jane", "Smith", dept);
        emp2.setGender("Female");
        var lt = createLeaveType(1L, "Annual Leave");
        var bal1 = createBalance(1L, emp1, lt, 2025,
                new BigDecimal("20"), new BigDecimal("10"), new BigDecimal("10"));
        var bal2 = createBalance(2L, emp2, lt, 2025,
                new BigDecimal("20"), new BigDecimal("5"), new BigDecimal("15"));

        when(leaveBalanceRepository.findByYear(2025)).thenReturn(List.of(bal1, bal2));
        when(employeeRepository.findByDeletedFalse(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(emp1, emp2)));

        var report = leaveAnalyticsService.generateLeaveUtilization(2025, null, null, "Male", null);

        assertNotNull(report);
        assertEquals(1, report.entries().size());
        assertEquals("John Doe", report.entries().get(0).employeeName());
    }

    // ===== CSV Export Tests =====

    @Test
    void exportReportAsCsv_leaveTrend_shouldReturnCsv() {
        when(leaveApplicationRepository.findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                any(), any(), any()))
                .thenReturn(List.of());

        var params = Map.of("startYear", "2025", "startMonth", "1", "endYear", "2025", "endMonth", "1");
        var result = leaveAnalyticsService.exportReportAsCsv("LEAVE_TREND", params);

        assertNotNull(result);
        var csv = new String(result);
        assertTrue(csv.contains("Year,Month,Leave Type,Application Count,Total Days"));
    }

    @Test
    void exportReportAsCsv_absenteeismRate_shouldReturnCsv() {
        when(departmentRepository.findAll()).thenReturn(List.of());

        var params = Map.of("startDate", "2025-01-01", "endDate", "2025-01-31");
        var result = leaveAnalyticsService.exportReportAsCsv("ABSENTEEISM_RATE", params);

        assertNotNull(result);
        var csv = new String(result);
        assertTrue(csv.contains("Department ID,Department Name"));
    }

    @Test
    void exportReportAsCsv_leaveUtilization_shouldReturnCsv() {
        when(leaveBalanceRepository.findByYear(2025)).thenReturn(List.of());
        when(employeeRepository.findByDeletedFalse(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));

        var params = Map.of("year", "2025");
        var result = leaveAnalyticsService.exportReportAsCsv("LEAVE_UTILIZATION", params);

        assertNotNull(result);
        var csv = new String(result);
        assertTrue(csv.contains("Employee ID,Employee Code"));
    }

    @Test
    void exportReportAsCsv_unsupportedType_shouldThrow() {
        assertThrows(BadRequestException.class, () ->
                leaveAnalyticsService.exportReportAsCsv("UNKNOWN", Map.of()));
    }

    // ===== Excel Export Tests =====

    @Test
    void exportReportAsExcel_leaveTrend_shouldReturnExcel() {
        when(leaveApplicationRepository.findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                any(), any(), any()))
                .thenReturn(List.of());

        var params = Map.of("startYear", "2025", "startMonth", "1", "endYear", "2025", "endMonth", "1");
        var result = leaveAnalyticsService.exportReportAsExcel("LEAVE_TREND", params);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void exportReportAsExcel_absenteeismRate_shouldReturnExcel() {
        when(departmentRepository.findAll()).thenReturn(List.of());

        var params = Map.of("startDate", "2025-01-01", "endDate", "2025-01-31");
        var result = leaveAnalyticsService.exportReportAsExcel("ABSENTEEISM_RATE", params);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void exportReportAsExcel_leaveUtilization_shouldReturnExcel() {
        when(leaveBalanceRepository.findByYear(2025)).thenReturn(List.of());
        when(employeeRepository.findByDeletedFalse(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));

        var params = Map.of("year", "2025");
        var result = leaveAnalyticsService.exportReportAsExcel("LEAVE_UTILIZATION", params);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void exportReportAsExcel_unsupportedType_shouldThrow() {
        assertThrows(BadRequestException.class, () ->
                leaveAnalyticsService.exportReportAsExcel("UNKNOWN", Map.of()));
    }

    // ===== PDF Export Tests =====

    @Test
    void exportReportAsPdf_leaveTrend_shouldReturnPdf() {
        when(leaveApplicationRepository.findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                any(), any(), any()))
                .thenReturn(List.of());

        var params = Map.of("startYear", "2025", "startMonth", "1", "endYear", "2025", "endMonth", "1");
        var result = leaveAnalyticsService.exportReportAsPdf("LEAVE_TREND", params);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void exportReportAsPdf_absenteeismRate_shouldReturnPdf() {
        when(departmentRepository.findAll()).thenReturn(List.of());

        var params = Map.of("startDate", "2025-01-01", "endDate", "2025-01-31");
        var result = leaveAnalyticsService.exportReportAsPdf("ABSENTEEISM_RATE", params);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void exportReportAsPdf_leaveUtilization_shouldReturnPdf() {
        when(leaveBalanceRepository.findByYear(2025)).thenReturn(List.of());
        when(employeeRepository.findByDeletedFalse(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));

        var params = Map.of("year", "2025");
        var result = leaveAnalyticsService.exportReportAsPdf("LEAVE_UTILIZATION", params);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void exportReportAsPdf_unsupportedType_shouldThrow() {
        assertThrows(BadRequestException.class, () ->
                leaveAnalyticsService.exportReportAsPdf("UNKNOWN", Map.of()));
    }
}
