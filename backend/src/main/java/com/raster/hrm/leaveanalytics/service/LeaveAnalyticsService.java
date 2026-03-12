package com.raster.hrm.leaveanalytics.service;

import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leaveanalytics.dto.AbsenteeismRateEntry;
import com.raster.hrm.leaveanalytics.dto.AbsenteeismRateReport;
import com.raster.hrm.leaveanalytics.dto.LeaveTrendEntry;
import com.raster.hrm.leaveanalytics.dto.LeaveTrendReport;
import com.raster.hrm.leaveanalytics.dto.LeaveUtilizationEntry;
import com.raster.hrm.leaveanalytics.dto.LeaveUtilizationReport;
import com.raster.hrm.leaveapplication.entity.LeaveApplication;
import com.raster.hrm.leaveapplication.entity.LeaveApplicationStatus;
import com.raster.hrm.leaveapplication.repository.LeaveApplicationRepository;
import com.raster.hrm.leavebalance.entity.LeaveBalance;
import com.raster.hrm.leavebalance.repository.LeaveBalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(LeaveAnalyticsService.class);

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public LeaveAnalyticsService(LeaveApplicationRepository leaveApplicationRepository,
                                 LeaveBalanceRepository leaveBalanceRepository,
                                 EmployeeRepository employeeRepository,
                                 DepartmentRepository departmentRepository) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public LeaveTrendReport generateLeaveTrend(int startYear, int startMonth,
                                                int endYear, int endMonth,
                                                Long departmentId) {
        validateMonthRange(startYear, startMonth, endYear, endMonth);
        var departmentName = getDepartmentName(departmentId);

        var startDate = LocalDate.of(startYear, startMonth, 1);
        var endYm = YearMonth.of(endYear, endMonth);
        var endDate = endYm.atEndOfMonth();

        var applications = getApprovedApplicationsInRange(startDate, endDate, departmentId);

        var grouped = new LinkedHashMap<String, Map<String, LeaveTrendAccumulator>>();
        var current = YearMonth.of(startYear, startMonth);
        var end = YearMonth.of(endYear, endMonth);
        while (!current.isAfter(end)) {
            grouped.put(current.getYear() + "-" + current.getMonthValue(), new LinkedHashMap<>());
            current = current.plusMonths(1);
        }

        for (var app : applications) {
            var appStart = app.getFromDate().isBefore(startDate) ? startDate : app.getFromDate();
            var appEnd = app.getToDate().isAfter(endDate) ? endDate : app.getToDate();
            var leaveTypeName = app.getLeaveType().getName();

            var curDate = appStart;
            while (!curDate.isAfter(appEnd)) {
                var ym = YearMonth.from(curDate);
                var key = ym.getYear() + "-" + ym.getMonthValue();
                var monthMap = grouped.get(key);
                if (monthMap != null) {
                    monthMap.computeIfAbsent(leaveTypeName, k -> new LeaveTrendAccumulator())
                            .addDay();
                }
                curDate = curDate.plusDays(1);
            }

            var appYm = YearMonth.from(appStart);
            var appKey = appYm.getYear() + "-" + appYm.getMonthValue();
            var appMonthMap = grouped.get(appKey);
            if (appMonthMap != null) {
                appMonthMap.computeIfAbsent(leaveTypeName, k -> new LeaveTrendAccumulator())
                        .addApplication();
            }
        }

        var entries = new ArrayList<LeaveTrendEntry>();
        for (var monthEntry : grouped.entrySet()) {
            var parts = monthEntry.getKey().split("-");
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            for (var typeEntry : monthEntry.getValue().entrySet()) {
                var acc = typeEntry.getValue();
                entries.add(new LeaveTrendEntry(y, m, typeEntry.getKey(), acc.applicationCount, acc.totalDays));
            }
            if (monthEntry.getValue().isEmpty()) {
                entries.add(new LeaveTrendEntry(y, m, "All", 0, BigDecimal.ZERO));
            }
        }

        log.info("Generated leave trend report from {}/{} to {}/{}, department: {}",
                startYear, startMonth, endYear, endMonth, departmentId);

        return new LeaveTrendReport(startYear, startMonth, endYear, endMonth,
                departmentId, departmentName, entries);
    }

    @Transactional(readOnly = true)
    public AbsenteeismRateReport generateAbsenteeismRate(LocalDate startDate, LocalDate endDate,
                                                          Long departmentId) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must not be after end date");
        }

        int totalWorkingDays = countWorkingDays(startDate, endDate);
        List<Department> departments;
        if (departmentId != null) {
            var dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
            departments = List.of(dept);
        } else {
            departments = departmentRepository.findAll();
        }

        var entries = new ArrayList<AbsenteeismRateEntry>();
        BigDecimal overallLeaveDays = BigDecimal.ZERO;
        int overallEmployeeCount = 0;

        for (var dept : departments) {
            var employees = employeeRepository.findByDepartmentIdAndDeletedFalse(dept.getId());
            if (employees.isEmpty()) {
                continue;
            }

            var applications = leaveApplicationRepository
                    .findByEmployee_Department_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                            dept.getId(), LeaveApplicationStatus.APPROVED, endDate, startDate);

            BigDecimal deptLeaveDays = BigDecimal.ZERO;
            for (var app : applications) {
                deptLeaveDays = deptLeaveDays.add(app.getNumberOfDays());
            }

            int empCount = employees.size();
            int totalPossibleDays = empCount * totalWorkingDays;
            var rate = totalPossibleDays > 0
                    ? deptLeaveDays.multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalPossibleDays), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            entries.add(new AbsenteeismRateEntry(
                    dept.getId(), dept.getName(), empCount,
                    deptLeaveDays, totalWorkingDays, rate));

            overallLeaveDays = overallLeaveDays.add(deptLeaveDays);
            overallEmployeeCount += empCount;
        }

        int overallPossibleDays = overallEmployeeCount * totalWorkingDays;
        var overallRate = overallPossibleDays > 0
                ? overallLeaveDays.multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(overallPossibleDays), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        log.info("Generated absenteeism rate report from {} to {}, department: {}, overall rate: {}%",
                startDate, endDate, departmentId, overallRate);

        return new AbsenteeismRateReport(startDate, endDate, overallRate, entries);
    }

    @Transactional(readOnly = true)
    public LeaveUtilizationReport generateLeaveUtilization(int year, Long departmentId) {
        var departmentName = getDepartmentName(departmentId);
        var balances = leaveBalanceRepository.findByYear(year);
        var employees = getEmployees(departmentId);
        var employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toSet());

        var entries = new ArrayList<LeaveUtilizationEntry>();
        BigDecimal totalEntitled = BigDecimal.ZERO;
        BigDecimal totalUsed = BigDecimal.ZERO;

        for (var balance : balances) {
            if (!employeeIds.contains(balance.getEmployee().getId())) {
                continue;
            }

            var emp = balance.getEmployee();
            var deptName = emp.getDepartment() != null ? emp.getDepartment().getName() : null;
            var leaveTypeName = balance.getLeaveType().getName();
            var entitled = balance.getCredited();
            var used = balance.getUsed();
            var available = balance.getAvailable();
            var utilPercent = entitled.compareTo(BigDecimal.ZERO) > 0
                    ? used.multiply(BigDecimal.valueOf(100))
                            .divide(entitled, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            entries.add(new LeaveUtilizationEntry(
                    emp.getId(), emp.getEmployeeCode(),
                    emp.getFirstName() + " " + emp.getLastName(),
                    deptName, leaveTypeName,
                    entitled, used, available, utilPercent));

            totalEntitled = totalEntitled.add(entitled);
            totalUsed = totalUsed.add(used);
        }

        var overallUtilization = totalEntitled.compareTo(BigDecimal.ZERO) > 0
                ? totalUsed.multiply(BigDecimal.valueOf(100))
                        .divide(totalEntitled, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        log.info("Generated leave utilization report for year: {}, department: {}, overall utilization: {}%",
                year, departmentId, overallUtilization);

        return new LeaveUtilizationReport(year, departmentId, departmentName, overallUtilization, entries);
    }

    @Transactional(readOnly = true)
    public byte[] exportReportAsCsv(String reportType, Map<String, String> params) {
        var sb = new StringBuilder();

        switch (reportType) {
            case "LEAVE_TREND" -> {
                int startYear = Integer.parseInt(params.get("startYear"));
                int startMonth = Integer.parseInt(params.get("startMonth"));
                int endYear = Integer.parseInt(params.get("endYear"));
                int endMonth = Integer.parseInt(params.get("endMonth"));
                var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                var report = generateLeaveTrend(startYear, startMonth, endYear, endMonth, departmentId);
                sb.append("Year,Month,Leave Type,Application Count,Total Days\n");
                for (var entry : report.entries()) {
                    sb.append(entry.year()).append(",");
                    sb.append(entry.month()).append(",");
                    sb.append(escapeCsv(entry.leaveTypeName())).append(",");
                    sb.append(entry.applicationCount()).append(",");
                    sb.append(entry.totalDays()).append("\n");
                }
            }
            case "ABSENTEEISM_RATE" -> {
                var startDate = LocalDate.parse(params.get("startDate"));
                var endDate = LocalDate.parse(params.get("endDate"));
                var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                var report = generateAbsenteeismRate(startDate, endDate, departmentId);
                sb.append("Department ID,Department Name,Employee Count,Total Leave Days,Working Days,Absenteeism Rate (%)\n");
                for (var entry : report.entries()) {
                    sb.append(entry.departmentId()).append(",");
                    sb.append(escapeCsv(entry.departmentName())).append(",");
                    sb.append(entry.employeeCount()).append(",");
                    sb.append(entry.totalLeaveDays()).append(",");
                    sb.append(entry.totalWorkingDays()).append(",");
                    sb.append(entry.absenteeismRate()).append("\n");
                }
            }
            case "LEAVE_UTILIZATION" -> {
                int year = Integer.parseInt(params.get("year"));
                var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                var report = generateLeaveUtilization(year, departmentId);
                sb.append("Employee ID,Employee Code,Employee Name,Department,Leave Type,Entitled,Used,Available,Utilization (%)\n");
                for (var entry : report.entries()) {
                    sb.append(entry.employeeId()).append(",");
                    sb.append(escapeCsv(entry.employeeCode())).append(",");
                    sb.append(escapeCsv(entry.employeeName())).append(",");
                    sb.append(escapeCsv(entry.departmentName())).append(",");
                    sb.append(escapeCsv(entry.leaveTypeName())).append(",");
                    sb.append(entry.entitled()).append(",");
                    sb.append(entry.used()).append(",");
                    sb.append(entry.available()).append(",");
                    sb.append(entry.utilizationPercent()).append("\n");
                }
            }
            default -> throw new BadRequestException("Unsupported report type: " + reportType);
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<LeaveApplication> getApprovedApplicationsInRange(LocalDate startDate, LocalDate endDate,
                                                                   Long departmentId) {
        if (departmentId != null) {
            return leaveApplicationRepository
                    .findByEmployee_Department_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                            departmentId, LeaveApplicationStatus.APPROVED, endDate, startDate);
        }
        return leaveApplicationRepository
                .findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        LeaveApplicationStatus.APPROVED, endDate, startDate);
    }

    private List<Employee> getEmployees(Long departmentId) {
        if (departmentId != null) {
            departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
            return employeeRepository.findByDepartmentIdAndDeletedFalse(departmentId);
        }
        return employeeRepository.findByDeletedFalse(Pageable.unpaged()).getContent();
    }

    private String getDepartmentName(Long departmentId) {
        if (departmentId == null) {
            return "All Departments";
        }
        return departmentRepository.findById(departmentId)
                .map(Department::getName)
                .orElse(null);
    }

    private int countWorkingDays(LocalDate startDate, LocalDate endDate) {
        int count = 0;
        var current = startDate;
        while (!current.isAfter(endDate)) {
            var dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    private void validateMonthRange(int startYear, int startMonth, int endYear, int endMonth) {
        if (startMonth < 1 || startMonth > 12 || endMonth < 1 || endMonth > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        var start = YearMonth.of(startYear, startMonth);
        var end = YearMonth.of(endYear, endMonth);
        if (start.isAfter(end)) {
            throw new BadRequestException("Start period must not be after end period");
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static class LeaveTrendAccumulator {
        long applicationCount;
        BigDecimal totalDays = BigDecimal.ZERO;

        void addApplication() {
            applicationCount++;
        }

        void addDay() {
            totalDays = totalDays.add(BigDecimal.ONE);
        }
    }
}
