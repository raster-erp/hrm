package com.raster.hrm.attendancereport.service;

import com.raster.hrm.attendance.entity.AttendancePunch;
import com.raster.hrm.attendance.entity.PunchDirection;
import com.raster.hrm.attendance.repository.AttendancePunchRepository;
import com.raster.hrm.attendancereport.dto.AbsenteeEntry;
import com.raster.hrm.attendancereport.dto.AbsenteeListReport;
import com.raster.hrm.attendancereport.dto.DailyMusterEntry;
import com.raster.hrm.attendancereport.dto.DailyMusterReport;
import com.raster.hrm.attendancereport.dto.MonthlySummaryEntry;
import com.raster.hrm.attendancereport.dto.MonthlySummaryReport;
import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.shiftroster.repository.ShiftRosterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AttendanceReportService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceReportService.class);

    private static final String STATUS_PRESENT = "PRESENT";
    private static final String STATUS_ABSENT = "ABSENT";
    private static final String STATUS_INCOMPLETE = "INCOMPLETE";

    private final EmployeeRepository employeeRepository;
    private final AttendancePunchRepository attendancePunchRepository;
    private final ShiftRosterRepository shiftRosterRepository;
    private final DepartmentRepository departmentRepository;

    public AttendanceReportService(EmployeeRepository employeeRepository,
                                   AttendancePunchRepository attendancePunchRepository,
                                   ShiftRosterRepository shiftRosterRepository,
                                   DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.attendancePunchRepository = attendancePunchRepository;
        this.shiftRosterRepository = shiftRosterRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public DailyMusterReport generateDailyMuster(LocalDate date, Long departmentId) {
        var employees = getEmployees(departmentId);
        var departmentName = getDepartmentName(departmentId);

        var dayStart = date.atStartOfDay();
        var dayEnd = date.atTime(LocalTime.MAX);

        var entries = new ArrayList<DailyMusterEntry>();
        for (var employee : employees) {
            var punches = attendancePunchRepository
                    .findByEmployeeIdAndPunchTimeBetween(employee.getId(), dayStart, dayEnd, Pageable.unpaged())
                    .getContent();

            var entry = buildDailyMusterEntry(employee, date, punches);
            entries.add(entry);
        }

        int totalPresent = (int) entries.stream().filter(e -> STATUS_PRESENT.equals(e.status())).count();
        int totalAbsent = (int) entries.stream().filter(e -> STATUS_ABSENT.equals(e.status())).count();
        int totalIncomplete = (int) entries.stream().filter(e -> STATUS_INCOMPLETE.equals(e.status())).count();

        log.info("Generated daily muster report for date: {}, department: {}, employees: {}",
                date, departmentId, entries.size());

        return new DailyMusterReport(date, departmentId, departmentName, entries,
                totalPresent, totalAbsent, totalIncomplete);
    }

    @Transactional(readOnly = true)
    public MonthlySummaryReport generateMonthlySummary(int year, int month, Long departmentId) {
        var employees = getEmployees(departmentId);
        var departmentName = getDepartmentName(departmentId);

        var yearMonth = YearMonth.of(year, month);
        var workingDays = getWorkingDays(yearMonth.atDay(1), yearMonth.atEndOfMonth());
        int totalWorkingDays = workingDays.size();

        var entries = new ArrayList<MonthlySummaryEntry>();
        for (var employee : employees) {
            int present = 0;
            int absent = 0;
            int incomplete = 0;

            for (var day : workingDays) {
                var status = determineDayStatus(employee.getId(), day);
                switch (status) {
                    case STATUS_PRESENT -> present++;
                    case STATUS_ABSENT -> absent++;
                    case STATUS_INCOMPLETE -> incomplete++;
                }
            }

            var deptName = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
            entries.add(new MonthlySummaryEntry(
                    employee.getId(),
                    employee.getEmployeeCode(),
                    employee.getFirstName() + " " + employee.getLastName(),
                    deptName,
                    present, absent, incomplete, totalWorkingDays
            ));
        }

        log.info("Generated monthly summary report for {}/{}, department: {}, employees: {}",
                year, month, departmentId, entries.size());

        return new MonthlySummaryReport(year, month, departmentId, departmentName, entries);
    }

    @Transactional(readOnly = true)
    public AbsenteeListReport generateAbsenteeList(LocalDate startDate, LocalDate endDate, Long departmentId) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must not be after end date");
        }

        var employees = getEmployees(departmentId);
        var departmentName = getDepartmentName(departmentId);
        var workingDays = getWorkingDays(startDate, endDate);

        var entries = new ArrayList<AbsenteeEntry>();
        for (var day : workingDays) {
            var dayStart = day.atStartOfDay();
            var dayEnd = day.atTime(LocalTime.MAX);

            for (var employee : employees) {
                var punches = attendancePunchRepository
                        .findByEmployeeIdAndPunchTimeBetween(employee.getId(), dayStart, dayEnd, Pageable.unpaged())
                        .getContent();

                if (punches.isEmpty()) {
                    var deptName = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
                    entries.add(new AbsenteeEntry(
                            employee.getId(),
                            employee.getEmployeeCode(),
                            employee.getFirstName() + " " + employee.getLastName(),
                            deptName,
                            day
                    ));
                }
            }
        }

        log.info("Generated absentee list report from {} to {}, department: {}, absent instances: {}",
                startDate, endDate, departmentId, entries.size());

        return new AbsenteeListReport(startDate, endDate, departmentId, departmentName, entries, entries.size());
    }

    @Transactional(readOnly = true)
    public byte[] exportReportAsCsv(String reportType, Map<String, String> params) {
        var sb = new StringBuilder();

        switch (reportType) {
            case "DAILY_MUSTER" -> {
                var date = LocalDate.parse(params.get("date"));
                var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                var report = generateDailyMuster(date, departmentId);
                sb.append("Employee ID,Employee Code,Employee Name,Department,Date,First Punch In,Last Punch Out,Total Punches,Status\n");
                for (var entry : report.entries()) {
                    sb.append(escapeCsv(String.valueOf(entry.employeeId()))).append(",");
                    sb.append(escapeCsv(entry.employeeCode())).append(",");
                    sb.append(escapeCsv(entry.employeeName())).append(",");
                    sb.append(escapeCsv(entry.departmentName())).append(",");
                    sb.append(escapeCsv(String.valueOf(entry.date()))).append(",");
                    sb.append(escapeCsv(entry.firstPunchIn() != null ? entry.firstPunchIn().toString() : "")).append(",");
                    sb.append(escapeCsv(entry.lastPunchOut() != null ? entry.lastPunchOut().toString() : "")).append(",");
                    sb.append(entry.totalPunches()).append(",");
                    sb.append(escapeCsv(entry.status())).append("\n");
                }
            }
            case "MONTHLY_SUMMARY" -> {
                var year = Integer.parseInt(params.get("year"));
                var month = Integer.parseInt(params.get("month"));
                var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                var report = generateMonthlySummary(year, month, departmentId);
                sb.append("Employee ID,Employee Code,Employee Name,Department,Total Present,Total Absent,Total Incomplete,Total Working Days\n");
                for (var entry : report.entries()) {
                    sb.append(escapeCsv(String.valueOf(entry.employeeId()))).append(",");
                    sb.append(escapeCsv(entry.employeeCode())).append(",");
                    sb.append(escapeCsv(entry.employeeName())).append(",");
                    sb.append(escapeCsv(entry.departmentName())).append(",");
                    sb.append(entry.totalPresent()).append(",");
                    sb.append(entry.totalAbsent()).append(",");
                    sb.append(entry.totalIncomplete()).append(",");
                    sb.append(entry.totalWorkingDays()).append("\n");
                }
            }
            case "ABSENTEE_LIST" -> {
                var startDate = LocalDate.parse(params.get("startDate"));
                var endDate = LocalDate.parse(params.get("endDate"));
                var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                var report = generateAbsenteeList(startDate, endDate, departmentId);
                sb.append("Employee ID,Employee Code,Employee Name,Department,Absent Date\n");
                for (var entry : report.entries()) {
                    sb.append(escapeCsv(String.valueOf(entry.employeeId()))).append(",");
                    sb.append(escapeCsv(entry.employeeCode())).append(",");
                    sb.append(escapeCsv(entry.employeeName())).append(",");
                    sb.append(escapeCsv(entry.departmentName())).append(",");
                    sb.append(escapeCsv(String.valueOf(entry.absentDate()))).append("\n");
                }
            }
            default -> throw new BadRequestException("Unsupported report type: " + reportType);
        }

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private DailyMusterEntry buildDailyMusterEntry(Employee employee, LocalDate date, List<AttendancePunch> punches) {
        var deptName = employee.getDepartment() != null ? employee.getDepartment().getName() : null;

        if (punches.isEmpty()) {
            return new DailyMusterEntry(
                    employee.getId(), employee.getEmployeeCode(),
                    employee.getFirstName() + " " + employee.getLastName(),
                    deptName, date, null, null, 0, STATUS_ABSENT
            );
        }

        LocalDateTime firstIn = null;
        LocalDateTime lastOut = null;
        boolean hasIn = false;
        boolean hasOut = false;

        for (var punch : punches) {
            if (punch.getDirection() == PunchDirection.IN) {
                hasIn = true;
                if (firstIn == null || punch.getPunchTime().isBefore(firstIn)) {
                    firstIn = punch.getPunchTime();
                }
            } else if (punch.getDirection() == PunchDirection.OUT) {
                hasOut = true;
                if (lastOut == null || punch.getPunchTime().isAfter(lastOut)) {
                    lastOut = punch.getPunchTime();
                }
            }
        }

        var status = (hasIn && hasOut) ? STATUS_PRESENT : STATUS_INCOMPLETE;

        return new DailyMusterEntry(
                employee.getId(), employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                deptName, date, firstIn, lastOut, punches.size(), status
        );
    }

    private String determineDayStatus(Long employeeId, LocalDate day) {
        var dayStart = day.atStartOfDay();
        var dayEnd = day.atTime(LocalTime.MAX);

        var punches = attendancePunchRepository
                .findByEmployeeIdAndPunchTimeBetween(employeeId, dayStart, dayEnd, Pageable.unpaged())
                .getContent();

        if (punches.isEmpty()) {
            return STATUS_ABSENT;
        }

        boolean hasIn = punches.stream().anyMatch(p -> p.getDirection() == PunchDirection.IN);
        boolean hasOut = punches.stream().anyMatch(p -> p.getDirection() == PunchDirection.OUT);

        return (hasIn && hasOut) ? STATUS_PRESENT : STATUS_INCOMPLETE;
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

    private List<LocalDate> getWorkingDays(LocalDate startDate, LocalDate endDate) {
        var workingDays = new ArrayList<LocalDate>();
        var current = startDate;
        while (!current.isAfter(endDate)) {
            var dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                workingDays.add(current);
            }
            current = current.plusDays(1);
        }
        return workingDays;
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
}
