package com.raster.hrm.attendancedeviation.service;

import com.raster.hrm.attendance.entity.AttendancePunch;
import com.raster.hrm.attendance.entity.PunchDirection;
import com.raster.hrm.attendance.repository.AttendancePunchRepository;
import com.raster.hrm.attendancedeviation.dto.AttendanceDeviationRequest;
import com.raster.hrm.attendancedeviation.dto.AttendanceDeviationResponse;
import com.raster.hrm.attendancedeviation.dto.DeviationApprovalRequest;
import com.raster.hrm.attendancedeviation.dto.DeviationSummaryResponse;
import com.raster.hrm.attendancedeviation.entity.AttendanceDeviation;
import com.raster.hrm.attendancedeviation.entity.DeviationStatus;
import com.raster.hrm.attendancedeviation.entity.DeviationType;
import com.raster.hrm.attendancedeviation.entity.PenaltyAction;
import com.raster.hrm.attendancedeviation.repository.AttendanceDeviationRepository;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.shift.entity.Shift;
import com.raster.hrm.shiftroster.entity.ShiftRoster;
import com.raster.hrm.shiftroster.repository.ShiftRosterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceDeviationService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceDeviationService.class);

    private final AttendanceDeviationRepository attendanceDeviationRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendancePunchRepository attendancePunchRepository;
    private final ShiftRosterRepository shiftRosterRepository;

    public AttendanceDeviationService(AttendanceDeviationRepository attendanceDeviationRepository,
                                      EmployeeRepository employeeRepository,
                                      AttendancePunchRepository attendancePunchRepository,
                                      ShiftRosterRepository shiftRosterRepository) {
        this.attendanceDeviationRepository = attendanceDeviationRepository;
        this.employeeRepository = employeeRepository;
        this.attendancePunchRepository = attendancePunchRepository;
        this.shiftRosterRepository = shiftRosterRepository;
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDeviationResponse> getAll(Pageable pageable) {
        return attendanceDeviationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public AttendanceDeviationResponse getById(Long id) {
        var deviation = attendanceDeviationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceDeviation", "id", id));
        return mapToResponse(deviation);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDeviationResponse> getByEmployeeId(Long employeeId, Pageable pageable) {
        return attendanceDeviationRepository.findByEmployeeId(employeeId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDeviationResponse> getByType(DeviationType type, Pageable pageable) {
        return attendanceDeviationRepository.findByType(type, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDeviationResponse> getByStatus(DeviationStatus status, Pageable pageable) {
        return attendanceDeviationRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDeviationResponse> getByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return attendanceDeviationRepository.findByDateRange(startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    public AttendanceDeviationResponse create(AttendanceDeviationRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var deviationType = DeviationType.valueOf(request.type());

        var deviation = new AttendanceDeviation();
        deviation.setEmployee(employee);
        deviation.setDeviationDate(request.deviationDate());
        deviation.setType(deviationType);
        deviation.setDeviationMinutes(request.deviationMinutes());
        deviation.setScheduledTime(request.scheduledTime());
        deviation.setActualTime(request.actualTime());
        deviation.setGracePeriodMinutes(request.gracePeriodMinutes() != null ? request.gracePeriodMinutes() : 0);
        deviation.setPenaltyAction(request.penaltyAction() != null ? PenaltyAction.valueOf(request.penaltyAction()) : PenaltyAction.NONE);
        deviation.setRemarks(request.remarks());

        var saved = attendanceDeviationRepository.save(deviation);
        log.info("Created attendance deviation with id: {} for employee: {}", saved.getId(), employee.getEmployeeCode());
        return mapToResponse(saved);
    }

    public AttendanceDeviationResponse update(Long id, AttendanceDeviationRequest request) {
        var deviation = attendanceDeviationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceDeviation", "id", id));

        if (deviation.getStatus() != DeviationStatus.PENDING) {
            throw new BadRequestException("Cannot update attendance deviation with status: " + deviation.getStatus());
        }

        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var deviationType = DeviationType.valueOf(request.type());

        deviation.setEmployee(employee);
        deviation.setDeviationDate(request.deviationDate());
        deviation.setType(deviationType);
        deviation.setDeviationMinutes(request.deviationMinutes());
        deviation.setScheduledTime(request.scheduledTime());
        deviation.setActualTime(request.actualTime());
        deviation.setGracePeriodMinutes(request.gracePeriodMinutes() != null ? request.gracePeriodMinutes() : 0);
        deviation.setPenaltyAction(request.penaltyAction() != null ? PenaltyAction.valueOf(request.penaltyAction()) : PenaltyAction.NONE);
        deviation.setRemarks(request.remarks());

        var saved = attendanceDeviationRepository.save(deviation);
        log.info("Updated attendance deviation with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public AttendanceDeviationResponse approve(Long id, DeviationApprovalRequest request) {
        var deviation = attendanceDeviationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceDeviation", "id", id));

        if (deviation.getStatus() != DeviationStatus.PENDING) {
            throw new BadRequestException("Cannot change status of attendance deviation with status: " + deviation.getStatus());
        }

        var newStatus = DeviationStatus.valueOf(request.status());
        if (newStatus != DeviationStatus.APPROVED && newStatus != DeviationStatus.WAIVED) {
            throw new BadRequestException("Status must be APPROVED or WAIVED");
        }

        deviation.setStatus(newStatus);
        deviation.setApprovedBy(request.approvedBy());
        deviation.setApprovedAt(LocalDateTime.now());
        if (request.remarks() != null) {
            deviation.setRemarks(request.remarks());
        }

        var saved = attendanceDeviationRepository.save(deviation);
        log.info("Updated attendance deviation id: {} status to {}", saved.getId(), newStatus);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var deviation = attendanceDeviationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceDeviation", "id", id));
        attendanceDeviationRepository.delete(deviation);
        log.info("Deleted attendance deviation with id: {}", id);
    }

    /**
     * Auto-detect late coming and early going from attendance punch data for a given employee and date.
     * Compares actual punch times against the employee's assigned shift to identify deviations.
     */
    public List<AttendanceDeviationResponse> detectDeviations(Long employeeId, LocalDate date) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        // Find the employee's active shift roster for the given date
        List<ShiftRoster> rosters = shiftRosterRepository.findOverlapping(employeeId, date, date);
        if (rosters.isEmpty()) {
            throw new BadRequestException("No shift roster found for employee " + employeeId + " on date " + date);
        }

        var roster = rosters.get(0);
        Shift shift = roster.getShift();

        // Get punches for the employee on the given date
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        var punchPage = attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(
                employeeId, dayStart, dayEnd, Pageable.unpaged());
        List<AttendancePunch> punches = punchPage.getContent();

        if (punches.isEmpty()) {
            return List.of();
        }

        // Find first IN and last OUT punch
        var firstIn = punches.stream()
                .filter(p -> p.getDirection() == PunchDirection.IN)
                .min(Comparator.comparing(AttendancePunch::getPunchTime));

        var lastOut = punches.stream()
                .filter(p -> p.getDirection() == PunchDirection.OUT)
                .max(Comparator.comparing(AttendancePunch::getPunchTime));

        int gracePeriod = shift.getGracePeriodMinutes() != null ? shift.getGracePeriodMinutes() : 0;

        List<AttendanceDeviationResponse> results = new ArrayList<>();

        // Check for late coming
        if (firstIn.isPresent()) {
            LocalTime actualInTime = firstIn.get().getPunchTime().toLocalTime();
            LocalTime shiftStartWithGrace = shift.getStartTime().plusMinutes(gracePeriod);

            if (actualInTime.isAfter(shiftStartWithGrace)) {
                long lateMinutes = Duration.between(shift.getStartTime(), actualInTime).toMinutes();

                // Skip if record already exists
                List<AttendanceDeviation> existing = attendanceDeviationRepository
                        .findByEmployeeIdAndDeviationDateAndType(employeeId, date, DeviationType.LATE_COMING);
                if (existing.isEmpty()) {
                    var deviation = new AttendanceDeviation();
                    deviation.setEmployee(employee);
                    deviation.setDeviationDate(date);
                    deviation.setType(DeviationType.LATE_COMING);
                    deviation.setDeviationMinutes((int) lateMinutes);
                    deviation.setScheduledTime(shift.getStartTime());
                    deviation.setActualTime(firstIn.get().getPunchTime());
                    deviation.setGracePeriodMinutes(gracePeriod);
                    deviation.setRemarks("Auto-detected from attendance punches");

                    var saved = attendanceDeviationRepository.save(deviation);
                    log.info("Auto-detected late coming of {} minutes for employee {} on {}",
                            lateMinutes, employee.getEmployeeCode(), date);
                    results.add(mapToResponse(saved));
                }
            }
        }

        // Check for early going
        if (lastOut.isPresent()) {
            LocalTime actualOutTime = lastOut.get().getPunchTime().toLocalTime();
            LocalTime shiftEndWithGrace = shift.getEndTime().minusMinutes(gracePeriod);

            if (actualOutTime.isBefore(shiftEndWithGrace)) {
                long earlyMinutes = Duration.between(actualOutTime, shift.getEndTime()).toMinutes();

                // Skip if record already exists
                List<AttendanceDeviation> existing = attendanceDeviationRepository
                        .findByEmployeeIdAndDeviationDateAndType(employeeId, date, DeviationType.EARLY_GOING);
                if (existing.isEmpty()) {
                    var deviation = new AttendanceDeviation();
                    deviation.setEmployee(employee);
                    deviation.setDeviationDate(date);
                    deviation.setType(DeviationType.EARLY_GOING);
                    deviation.setDeviationMinutes((int) earlyMinutes);
                    deviation.setScheduledTime(shift.getEndTime());
                    deviation.setActualTime(lastOut.get().getPunchTime());
                    deviation.setGracePeriodMinutes(gracePeriod);
                    deviation.setRemarks("Auto-detected from attendance punches");

                    var saved = attendanceDeviationRepository.save(deviation);
                    log.info("Auto-detected early going of {} minutes for employee {} on {}",
                            earlyMinutes, employee.getEmployeeCode(), date);
                    results.add(mapToResponse(saved));
                }
            }
        }

        return results;
    }

    /**
     * Get deviation summary grouped by employee for a date range.
     */
    @Transactional(readOnly = true)
    public List<DeviationSummaryResponse> getSummary(LocalDate startDate, LocalDate endDate) {
        var deviations = attendanceDeviationRepository.findByDateRange(startDate, endDate, Pageable.unpaged()).getContent();

        return deviations.stream()
                .collect(Collectors.groupingBy(d -> d.getEmployee().getId()))
                .entrySet().stream()
                .map(entry -> {
                    var empDeviations = entry.getValue();
                    var employee = empDeviations.get(0).getEmployee();

                    int lateComingCount = (int) empDeviations.stream()
                            .filter(d -> d.getType() == DeviationType.LATE_COMING).count();

                    int earlyGoingCount = (int) empDeviations.stream()
                            .filter(d -> d.getType() == DeviationType.EARLY_GOING).count();

                    int totalDeviationMinutes = empDeviations.stream()
                            .mapToInt(AttendanceDeviation::getDeviationMinutes).sum();

                    int lateComingMinutes = empDeviations.stream()
                            .filter(d -> d.getType() == DeviationType.LATE_COMING)
                            .mapToInt(AttendanceDeviation::getDeviationMinutes).sum();

                    int earlyGoingMinutes = empDeviations.stream()
                            .filter(d -> d.getType() == DeviationType.EARLY_GOING)
                            .mapToInt(AttendanceDeviation::getDeviationMinutes).sum();

                    int warningCount = (int) empDeviations.stream()
                            .filter(d -> d.getPenaltyAction() == PenaltyAction.WARNING).count();

                    int leaveDeductionCount = (int) empDeviations.stream()
                            .filter(d -> d.getPenaltyAction() == PenaltyAction.LEAVE_DEDUCTION).count();

                    int payCutCount = (int) empDeviations.stream()
                            .filter(d -> d.getPenaltyAction() == PenaltyAction.PAY_CUT).count();

                    return new DeviationSummaryResponse(
                            employee.getId(),
                            employee.getEmployeeCode(),
                            employee.getFirstName() + " " + employee.getLastName(),
                            lateComingCount,
                            earlyGoingCount,
                            totalDeviationMinutes,
                            lateComingMinutes,
                            earlyGoingMinutes,
                            warningCount,
                            leaveDeductionCount,
                            payCutCount
                    );
                })
                .toList();
    }

    private AttendanceDeviationResponse mapToResponse(AttendanceDeviation deviation) {
        var employee = deviation.getEmployee();
        return new AttendanceDeviationResponse(
                deviation.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                deviation.getDeviationDate(),
                deviation.getType().name(),
                deviation.getDeviationMinutes(),
                deviation.getScheduledTime(),
                deviation.getActualTime(),
                deviation.getGracePeriodMinutes(),
                deviation.getPenaltyAction().name(),
                deviation.getStatus().name(),
                deviation.getRemarks(),
                deviation.getApprovedBy(),
                deviation.getApprovedAt(),
                deviation.getCreatedAt(),
                deviation.getUpdatedAt()
        );
    }
}
