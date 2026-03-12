package com.raster.hrm.leaveanalytics.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.designation.repository.DesignationRepository;
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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(LeaveAnalyticsService.class);

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;

    public LeaveAnalyticsService(LeaveApplicationRepository leaveApplicationRepository,
                                 LeaveBalanceRepository leaveBalanceRepository,
                                 EmployeeRepository employeeRepository,
                                 DepartmentRepository departmentRepository,
                                 DesignationRepository designationRepository) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
    }

    @Transactional(readOnly = true)
    public LeaveTrendReport generateLeaveTrend(int startYear, int startMonth,
                                                int endYear, int endMonth,
                                                Long departmentId, Long designationId,
                                                String gender, String ageGroup) {
        validateMonthRange(startYear, startMonth, endYear, endMonth);
        var departmentName = getDepartmentName(departmentId);

        var startDate = LocalDate.of(startYear, startMonth, 1);
        var endYm = YearMonth.of(endYear, endMonth);
        var endDate = endYm.atEndOfMonth();

        var applications = getApprovedApplicationsInRange(startDate, endDate, departmentId);
        var filteredEmployeeIds = getFilteredEmployeeIds(departmentId, designationId, gender, ageGroup);

        var grouped = new LinkedHashMap<String, Map<String, LeaveTrendAccumulator>>();
        var current = YearMonth.of(startYear, startMonth);
        var end = YearMonth.of(endYear, endMonth);
        while (!current.isAfter(end)) {
            grouped.put(current.getYear() + "-" + current.getMonthValue(), new LinkedHashMap<>());
            current = current.plusMonths(1);
        }

        for (var app : applications) {
            if (filteredEmployeeIds != null && !filteredEmployeeIds.contains(app.getEmployee().getId())) {
                continue;
            }
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
                                                          Long departmentId, Long designationId,
                                                          String gender, String ageGroup) {
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

        var filteredEmployeeIds = getFilteredEmployeeIds(departmentId, designationId, gender, ageGroup);

        var entries = new ArrayList<AbsenteeismRateEntry>();
        BigDecimal overallLeaveDays = BigDecimal.ZERO;
        int overallEmployeeCount = 0;

        for (var dept : departments) {
            var employees = employeeRepository.findByDepartmentIdAndDeletedFalse(dept.getId());
            if (filteredEmployeeIds != null) {
                employees = employees.stream()
                        .filter(e -> filteredEmployeeIds.contains(e.getId()))
                        .collect(Collectors.toList());
            }
            if (employees.isEmpty()) {
                continue;
            }

            var applications = leaveApplicationRepository
                    .findByEmployee_Department_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                            dept.getId(), LeaveApplicationStatus.APPROVED, endDate, startDate);

            BigDecimal deptLeaveDays = BigDecimal.ZERO;
            var empIds = employees.stream().map(Employee::getId).collect(Collectors.toSet());
            for (var app : applications) {
                if (empIds.contains(app.getEmployee().getId())) {
                    deptLeaveDays = deptLeaveDays.add(app.getNumberOfDays());
                }
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
    public LeaveUtilizationReport generateLeaveUtilization(int year, Long departmentId,
                                                            Long designationId, String gender,
                                                            String ageGroup) {
        var departmentName = getDepartmentName(departmentId);
        var balances = leaveBalanceRepository.findByYear(year);
        var employees = getEmployees(departmentId);
        var filteredEmployeeIds = getFilteredEmployeeIds(departmentId, designationId, gender, ageGroup);
        var employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toSet());

        var entries = new ArrayList<LeaveUtilizationEntry>();
        BigDecimal totalEntitled = BigDecimal.ZERO;
        BigDecimal totalUsed = BigDecimal.ZERO;

        for (var balance : balances) {
            if (!employeeIds.contains(balance.getEmployee().getId())) {
                continue;
            }
            if (filteredEmployeeIds != null && !filteredEmployeeIds.contains(balance.getEmployee().getId())) {
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
        var dimensionParams = extractDimensionParams(params);

        switch (reportType) {
            case "LEAVE_TREND" -> {
                int startYear = Integer.parseInt(params.get("startYear"));
                int startMonth = Integer.parseInt(params.get("startMonth"));
                int endYear = Integer.parseInt(params.get("endYear"));
                int endMonth = Integer.parseInt(params.get("endMonth"));
                var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                var report = generateLeaveTrend(startYear, startMonth, endYear, endMonth, departmentId,
                        dimensionParams.designationId(), dimensionParams.gender(), dimensionParams.ageGroup());
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
                var report = generateAbsenteeismRate(startDate, endDate, departmentId,
                        dimensionParams.designationId(), dimensionParams.gender(), dimensionParams.ageGroup());
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
                var report = generateLeaveUtilization(year, departmentId,
                        dimensionParams.designationId(), dimensionParams.gender(), dimensionParams.ageGroup());
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

    @Transactional(readOnly = true)
    public byte[] exportReportAsExcel(String reportType, Map<String, String> params) {
        var dimensionParams = extractDimensionParams(params);

        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet(reportType);

            var headerStyle = workbook.createCellStyle();
            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            switch (reportType) {
                case "LEAVE_TREND" -> {
                    int startYear = Integer.parseInt(params.get("startYear"));
                    int startMonth = Integer.parseInt(params.get("startMonth"));
                    int endYear = Integer.parseInt(params.get("endYear"));
                    int endMonth = Integer.parseInt(params.get("endMonth"));
                    var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                    var report = generateLeaveTrend(startYear, startMonth, endYear, endMonth, departmentId,
                            dimensionParams.designationId(), dimensionParams.gender(), dimensionParams.ageGroup());

                    var headers = new String[]{"Year", "Month", "Leave Type", "Application Count", "Total Days"};
                    createExcelHeaderRow(sheet, headerStyle, headers);
                    int rowNum = 1;
                    for (var entry : report.entries()) {
                        var row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(entry.year());
                        row.createCell(1).setCellValue(entry.month());
                        row.createCell(2).setCellValue(entry.leaveTypeName());
                        row.createCell(3).setCellValue(entry.applicationCount());
                        row.createCell(4).setCellValue(entry.totalDays().doubleValue());
                    }
                }
                case "ABSENTEEISM_RATE" -> {
                    var startDate = LocalDate.parse(params.get("startDate"));
                    var endDate = LocalDate.parse(params.get("endDate"));
                    var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                    var report = generateAbsenteeismRate(startDate, endDate, departmentId,
                            dimensionParams.designationId(), dimensionParams.gender(), dimensionParams.ageGroup());

                    var headers = new String[]{"Department ID", "Department Name", "Employee Count", "Total Leave Days", "Working Days", "Absenteeism Rate (%)"};
                    createExcelHeaderRow(sheet, headerStyle, headers);
                    int rowNum = 1;
                    for (var entry : report.entries()) {
                        var row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(entry.departmentId());
                        row.createCell(1).setCellValue(entry.departmentName());
                        row.createCell(2).setCellValue(entry.employeeCount());
                        row.createCell(3).setCellValue(entry.totalLeaveDays().doubleValue());
                        row.createCell(4).setCellValue(entry.totalWorkingDays());
                        row.createCell(5).setCellValue(entry.absenteeismRate().doubleValue());
                    }
                }
                case "LEAVE_UTILIZATION" -> {
                    int year = Integer.parseInt(params.get("year"));
                    var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                    var report = generateLeaveUtilization(year, departmentId,
                            dimensionParams.designationId(), dimensionParams.gender(), dimensionParams.ageGroup());

                    var headers = new String[]{"Employee ID", "Employee Code", "Employee Name", "Department", "Leave Type", "Entitled", "Used", "Available", "Utilization (%)"};
                    createExcelHeaderRow(sheet, headerStyle, headers);
                    int rowNum = 1;
                    for (var entry : report.entries()) {
                        var row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(entry.employeeId());
                        row.createCell(1).setCellValue(entry.employeeCode());
                        row.createCell(2).setCellValue(entry.employeeName());
                        row.createCell(3).setCellValue(entry.departmentName() != null ? entry.departmentName() : "");
                        row.createCell(4).setCellValue(entry.leaveTypeName());
                        row.createCell(5).setCellValue(entry.entitled().doubleValue());
                        row.createCell(6).setCellValue(entry.used().doubleValue());
                        row.createCell(7).setCellValue(entry.available().doubleValue());
                        row.createCell(8).setCellValue(entry.utilizationPercent().doubleValue());
                    }
                }
                default -> throw new BadRequestException("Unsupported report type: " + reportType);
            }

            var out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate Excel report", e);
            throw new BadRequestException("Failed to generate Excel report");
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportReportAsPdf(String reportType, Map<String, String> params) {
        var dimensionParams = extractDimensionParams(params);
        var out = new ByteArrayOutputStream();
        var document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            var titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            var headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            var cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

            switch (reportType) {
                case "LEAVE_TREND" -> {
                    int startYear = Integer.parseInt(params.get("startYear"));
                    int startMonth = Integer.parseInt(params.get("startMonth"));
                    int endYear = Integer.parseInt(params.get("endYear"));
                    int endMonth = Integer.parseInt(params.get("endMonth"));
                    var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                    var report = generateLeaveTrend(startYear, startMonth, endYear, endMonth, departmentId,
                            dimensionParams.designationId(), dimensionParams.gender(), dimensionParams.ageGroup());

                    document.add(new Paragraph("Leave Trend Report", titleFont));
                    document.add(new Paragraph(" "));
                    var table = new PdfPTable(5);
                    table.setWidthPercentage(100);
                    addPdfHeaderCell(table, "Year", headerFont);
                    addPdfHeaderCell(table, "Month", headerFont);
                    addPdfHeaderCell(table, "Leave Type", headerFont);
                    addPdfHeaderCell(table, "Applications", headerFont);
                    addPdfHeaderCell(table, "Total Days", headerFont);
                    for (var entry : report.entries()) {
                        table.addCell(new Phrase(String.valueOf(entry.year()), cellFont));
                        table.addCell(new Phrase(String.valueOf(entry.month()), cellFont));
                        table.addCell(new Phrase(entry.leaveTypeName(), cellFont));
                        table.addCell(new Phrase(String.valueOf(entry.applicationCount()), cellFont));
                        table.addCell(new Phrase(entry.totalDays().toString(), cellFont));
                    }
                    document.add(table);
                }
                case "ABSENTEEISM_RATE" -> {
                    var startDate = LocalDate.parse(params.get("startDate"));
                    var endDate = LocalDate.parse(params.get("endDate"));
                    var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                    var report = generateAbsenteeismRate(startDate, endDate, departmentId,
                            dimensionParams.designationId(), dimensionParams.gender(), dimensionParams.ageGroup());

                    document.add(new Paragraph("Absenteeism Rate Report", titleFont));
                    document.add(new Paragraph(" "));
                    var table = new PdfPTable(6);
                    table.setWidthPercentage(100);
                    addPdfHeaderCell(table, "Dept ID", headerFont);
                    addPdfHeaderCell(table, "Department", headerFont);
                    addPdfHeaderCell(table, "Employees", headerFont);
                    addPdfHeaderCell(table, "Leave Days", headerFont);
                    addPdfHeaderCell(table, "Working Days", headerFont);
                    addPdfHeaderCell(table, "Rate (%)", headerFont);
                    for (var entry : report.entries()) {
                        table.addCell(new Phrase(String.valueOf(entry.departmentId()), cellFont));
                        table.addCell(new Phrase(entry.departmentName(), cellFont));
                        table.addCell(new Phrase(String.valueOf(entry.employeeCount()), cellFont));
                        table.addCell(new Phrase(entry.totalLeaveDays().toString(), cellFont));
                        table.addCell(new Phrase(String.valueOf(entry.totalWorkingDays()), cellFont));
                        table.addCell(new Phrase(entry.absenteeismRate().toString(), cellFont));
                    }
                    document.add(table);
                }
                case "LEAVE_UTILIZATION" -> {
                    int year = Integer.parseInt(params.get("year"));
                    var departmentId = params.containsKey("departmentId") ? Long.valueOf(params.get("departmentId")) : null;
                    var report = generateLeaveUtilization(year, departmentId,
                            dimensionParams.designationId(), dimensionParams.gender(), dimensionParams.ageGroup());

                    document.add(new Paragraph("Leave Utilization Report", titleFont));
                    document.add(new Paragraph(" "));
                    var table = new PdfPTable(9);
                    table.setWidthPercentage(100);
                    addPdfHeaderCell(table, "Emp ID", headerFont);
                    addPdfHeaderCell(table, "Code", headerFont);
                    addPdfHeaderCell(table, "Name", headerFont);
                    addPdfHeaderCell(table, "Department", headerFont);
                    addPdfHeaderCell(table, "Leave Type", headerFont);
                    addPdfHeaderCell(table, "Entitled", headerFont);
                    addPdfHeaderCell(table, "Used", headerFont);
                    addPdfHeaderCell(table, "Available", headerFont);
                    addPdfHeaderCell(table, "Util (%)", headerFont);
                    for (var entry : report.entries()) {
                        table.addCell(new Phrase(String.valueOf(entry.employeeId()), cellFont));
                        table.addCell(new Phrase(entry.employeeCode(), cellFont));
                        table.addCell(new Phrase(entry.employeeName(), cellFont));
                        table.addCell(new Phrase(entry.departmentName() != null ? entry.departmentName() : "", cellFont));
                        table.addCell(new Phrase(entry.leaveTypeName(), cellFont));
                        table.addCell(new Phrase(entry.entitled().toString(), cellFont));
                        table.addCell(new Phrase(entry.used().toString(), cellFont));
                        table.addCell(new Phrase(entry.available().toString(), cellFont));
                        table.addCell(new Phrase(entry.utilizationPercent().toString(), cellFont));
                    }
                    document.add(table);
                }
                default -> throw new BadRequestException("Unsupported report type: " + reportType);
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            log.error("Failed to generate PDF report", e);
            throw new BadRequestException("Failed to generate PDF report");
        }
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

    private Set<Long> getFilteredEmployeeIds(Long departmentId, Long designationId,
                                              String gender, String ageGroup) {
        if (designationId == null && gender == null && ageGroup == null) {
            return null;
        }

        var employees = getEmployees(departmentId);
        var filtered = employees.stream();

        if (designationId != null) {
            designationRepository.findById(designationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", designationId));
            filtered = filtered.filter(e -> e.getDesignation() != null
                    && designationId.equals(e.getDesignation().getId()));
        }

        if (gender != null) {
            filtered = filtered.filter(e -> gender.equalsIgnoreCase(e.getGender()));
        }

        if (ageGroup != null) {
            filtered = filtered.filter(e -> matchesAgeGroup(e, ageGroup));
        }

        return filtered.map(Employee::getId).collect(Collectors.toSet());
    }

    private boolean matchesAgeGroup(Employee employee, String ageGroup) {
        if (employee.getDateOfBirth() == null) {
            return false;
        }
        int age = Period.between(employee.getDateOfBirth(), LocalDate.now()).getYears();
        return switch (ageGroup) {
            case "UNDER_25" -> age < 25;
            case "25_34" -> age >= 25 && age <= 34;
            case "35_44" -> age >= 35 && age <= 44;
            case "45_54" -> age >= 45 && age <= 54;
            case "55_PLUS" -> age >= 55;
            default -> throw new BadRequestException("Unsupported age group: " + ageGroup
                    + ". Supported values: UNDER_25, 25_34, 35_44, 45_54, 55_PLUS");
        };
    }

    private DimensionParams extractDimensionParams(Map<String, String> params) {
        var designationId = params.containsKey("designationId") ? Long.valueOf(params.get("designationId")) : null;
        var gender = params.getOrDefault("gender", null);
        var ageGroup = params.getOrDefault("ageGroup", null);
        return new DimensionParams(designationId, gender, ageGroup);
    }

    private void createExcelHeaderRow(org.apache.poi.ss.usermodel.Sheet sheet,
                                       CellStyle headerStyle, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            var cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void addPdfHeaderCell(PdfPTable table, String text, Font font) {
        var cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private record DimensionParams(Long designationId, String gender, String ageGroup) {
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
