package com.raster.hrm.leavecalendar.service;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.holiday.entity.Holiday;
import com.raster.hrm.holiday.repository.HolidayRepository;
import com.raster.hrm.leaveapplication.entity.LeaveApplication;
import com.raster.hrm.leaveapplication.entity.LeaveApplicationStatus;
import com.raster.hrm.leaveapplication.repository.LeaveApplicationRepository;
import com.raster.hrm.leavecalendar.dto.LeaveCalendarEntry;
import com.raster.hrm.leavecalendar.dto.TeamAvailabilityResponse;
import com.raster.hrm.leaveplan.entity.LeavePlan;
import com.raster.hrm.leaveplan.entity.LeavePlanStatus;
import com.raster.hrm.leaveplan.repository.LeavePlanRepository;
import com.raster.hrm.leavetype.entity.LeaveTypeCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class LeaveCalendarService {

    private static final Logger log = LoggerFactory.getLogger(LeaveCalendarService.class);

    private static final String TYPE_HOLIDAY = "HOLIDAY";
    private static final String TYPE_LEAVE = "LEAVE";
    private static final String TYPE_PLAN = "PLAN";

    private static final String COLOR_HOLIDAY = "#4caf50";
    private static final String COLOR_PAID = "#2196f3";
    private static final String COLOR_UNPAID = "#ff9800";
    private static final String COLOR_OPTIONAL = "#9c27b0";
    private static final String COLOR_SPECIAL = "#e91e63";
    private static final String COLOR_PLAN = "#78909c";

    private final HolidayRepository holidayRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeavePlanRepository leavePlanRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveCalendarService(HolidayRepository holidayRepository,
                                LeaveApplicationRepository leaveApplicationRepository,
                                LeavePlanRepository leavePlanRepository,
                                EmployeeRepository employeeRepository) {
        this.holidayRepository = holidayRepository;
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leavePlanRepository = leavePlanRepository;
        this.employeeRepository = employeeRepository;
    }

    // ── Calendar Entries ────────────────────────────────────────────────

    public List<LeaveCalendarEntry> getCalendarEntries(LocalDate start, LocalDate end,
                                                       Long employeeId, Long departmentId,
                                                       String region) {
        log.debug("Fetching calendar entries from {} to {} employeeId={} departmentId={} region={}",
                start, end, employeeId, departmentId, region);

        List<LeaveCalendarEntry> entries = new ArrayList<>();

        // Holidays
        List<Holiday> holidays;
        if (region != null) {
            holidays = holidayRepository.findByRegionAndDateBetweenAndActiveTrue(region, start, end);
        } else {
            holidays = holidayRepository.findByDateBetweenAndActiveTrue(start, end);
        }
        for (Holiday holiday : holidays) {
            entries.add(new LeaveCalendarEntry(
                    holiday.getId(),
                    TYPE_HOLIDAY,
                    null,
                    null,
                    holiday.getName(),
                    holiday.getType().name(),
                    holiday.getDate(),
                    holiday.getDate(),
                    "ACTIVE",
                    COLOR_HOLIDAY
            ));
        }

        // Approved leave applications
        List<LeaveApplication> leaveApplications;
        if (employeeId != null) {
            leaveApplications = leaveApplicationRepository
                    .findByEmployeeIdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                            employeeId, LeaveApplicationStatus.APPROVED, end, start);
        } else if (departmentId != null) {
            leaveApplications = leaveApplicationRepository
                    .findByEmployee_Department_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                            departmentId, LeaveApplicationStatus.APPROVED, end, start);
        } else {
            leaveApplications = leaveApplicationRepository
                    .findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                            LeaveApplicationStatus.APPROVED, end, start);
        }
        for (LeaveApplication la : leaveApplications) {
            Employee emp = la.getEmployee();
            entries.add(new LeaveCalendarEntry(
                    la.getId(),
                    TYPE_LEAVE,
                    emp.getId(),
                    emp.getFirstName() + " " + emp.getLastName(),
                    la.getLeaveType().getName(),
                    la.getLeaveType().getCategory().name(),
                    la.getFromDate(),
                    la.getToDate(),
                    la.getStatus().name(),
                    getLeaveColor(la.getLeaveType().getCategory())
            ));
        }

        // Planned leaves
        List<LeavePlan> leavePlans;
        if (employeeId != null) {
            leavePlans = leavePlanRepository
                    .findByEmployeeIdAndPlannedFromDateGreaterThanEqualAndPlannedToDateLessThanEqual(
                            employeeId, start, end);
        } else if (departmentId != null) {
            leavePlans = leavePlanRepository
                    .findByEmployee_Department_IdAndPlannedFromDateLessThanEqualAndPlannedToDateGreaterThanEqual(
                            departmentId, end, start);
        } else {
            leavePlans = leavePlanRepository
                    .findByPlannedFromDateGreaterThanEqualAndPlannedToDateLessThanEqual(start, end);
        }
        for (LeavePlan plan : leavePlans) {
            if (plan.getStatus() == LeavePlanStatus.CANCELLED) {
                continue;
            }
            Employee emp = plan.getEmployee();
            entries.add(new LeaveCalendarEntry(
                    plan.getId(),
                    TYPE_PLAN,
                    emp.getId(),
                    emp.getFirstName() + " " + emp.getLastName(),
                    plan.getLeaveType().getName(),
                    plan.getLeaveType().getCategory().name(),
                    plan.getPlannedFromDate(),
                    plan.getPlannedToDate(),
                    plan.getStatus().name(),
                    COLOR_PLAN
            ));
        }

        return entries;
    }

    // ── Team Availability ───────────────────────────────────────────────

    public List<TeamAvailabilityResponse> getTeamAvailability(Long departmentId,
                                                              LocalDate start, LocalDate end) {
        log.debug("Calculating team availability for department {} from {} to {}", departmentId, start, end);

        List<Employee> teamMembers = employeeRepository.findByDepartmentIdAndDeletedFalse(departmentId);
        int totalMembers = teamMembers.size();

        List<LeaveApplication> approvedLeaves = leaveApplicationRepository
                .findByEmployee_Department_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        departmentId, LeaveApplicationStatus.APPROVED, end, start);

        List<LeavePlan> plannedLeaves = leavePlanRepository
                .findByEmployee_Department_IdAndPlannedFromDateLessThanEqualAndPlannedToDateGreaterThanEqual(
                        departmentId, end, start);

        List<TeamAvailabilityResponse> availability = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            final LocalDate date = current;

            Set<String> absentNames = new HashSet<>();
            int onLeaveCount = 0;
            int onPlannedLeaveCount = 0;

            for (LeaveApplication la : approvedLeaves) {
                if (!date.isBefore(la.getFromDate()) && !date.isAfter(la.getToDate())) {
                    Employee emp = la.getEmployee();
                    String name = emp.getFirstName() + " " + emp.getLastName();
                    if (absentNames.add(name)) {
                        onLeaveCount++;
                    }
                }
            }

            for (LeavePlan plan : plannedLeaves) {
                if (plan.getStatus() == LeavePlanStatus.CANCELLED || plan.getStatus() == LeavePlanStatus.CONVERTED) {
                    continue;
                }
                if (!date.isBefore(plan.getPlannedFromDate()) && !date.isAfter(plan.getPlannedToDate())) {
                    Employee emp = plan.getEmployee();
                    String name = emp.getFirstName() + " " + emp.getLastName();
                    if (absentNames.add(name)) {
                        onPlannedLeaveCount++;
                    }
                }
            }

            int available = totalMembers - onLeaveCount - onPlannedLeaveCount;
            double coveragePercentage = totalMembers > 0
                    ? ((double) available / totalMembers) * 100.0
                    : 0.0;

            availability.add(new TeamAvailabilityResponse(
                    date,
                    totalMembers,
                    available,
                    onLeaveCount,
                    onPlannedLeaveCount,
                    coveragePercentage,
                    new ArrayList<>(absentNames)
            ));

            current = current.plusDays(1);
        }

        return availability;
    }

    // ── Private helpers ─────────────────────────────────────────────────

    private String getLeaveColor(LeaveTypeCategory category) {
        return switch (category) {
            case PAID -> COLOR_PAID;
            case UNPAID -> COLOR_UNPAID;
            case SPECIAL -> COLOR_SPECIAL;
            case STATUTORY -> COLOR_OPTIONAL;
        };
    }
}
