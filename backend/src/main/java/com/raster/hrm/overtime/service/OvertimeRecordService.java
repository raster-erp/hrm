package com.raster.hrm.overtime.service;

import com.raster.hrm.attendance.entity.AttendancePunch;
import com.raster.hrm.attendance.entity.PunchDirection;
import com.raster.hrm.attendance.repository.AttendancePunchRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.overtime.dto.OvertimeApprovalRequest;
import com.raster.hrm.overtime.dto.OvertimeRecordRequest;
import com.raster.hrm.overtime.dto.OvertimeRecordResponse;
import com.raster.hrm.overtime.dto.OvertimeSummaryResponse;
import com.raster.hrm.overtime.entity.OvertimePolicy;
import com.raster.hrm.overtime.entity.OvertimeRecord;
import com.raster.hrm.overtime.entity.OvertimeSource;
import com.raster.hrm.overtime.entity.OvertimeStatus;
import com.raster.hrm.overtime.repository.OvertimePolicyRepository;
import com.raster.hrm.overtime.repository.OvertimeRecordRepository;
import com.raster.hrm.shift.entity.Shift;
import com.raster.hrm.shiftroster.entity.ShiftRoster;
import com.raster.hrm.shiftroster.repository.ShiftRosterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
public class OvertimeRecordService {

    private static final Logger log = LoggerFactory.getLogger(OvertimeRecordService.class);

    private final OvertimeRecordRepository overtimeRecordRepository;
    private final OvertimePolicyRepository overtimePolicyRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendancePunchRepository attendancePunchRepository;
    private final ShiftRosterRepository shiftRosterRepository;

    public OvertimeRecordService(OvertimeRecordRepository overtimeRecordRepository,
                                  OvertimePolicyRepository overtimePolicyRepository,
                                  EmployeeRepository employeeRepository,
                                  AttendancePunchRepository attendancePunchRepository,
                                  ShiftRosterRepository shiftRosterRepository) {
        this.overtimeRecordRepository = overtimeRecordRepository;
        this.overtimePolicyRepository = overtimePolicyRepository;
        this.employeeRepository = employeeRepository;
        this.attendancePunchRepository = attendancePunchRepository;
        this.shiftRosterRepository = shiftRosterRepository;
    }

    @Transactional(readOnly = true)
    public Page<OvertimeRecordResponse> getAll(Pageable pageable) {
        return overtimeRecordRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public OvertimeRecordResponse getById(Long id) {
        var record = overtimeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OvertimeRecord", "id", id));
        return mapToResponse(record);
    }

    @Transactional(readOnly = true)
    public Page<OvertimeRecordResponse> getByEmployeeId(Long employeeId, Pageable pageable) {
        return overtimeRecordRepository.findByEmployeeId(employeeId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<OvertimeRecordResponse> getByStatus(OvertimeStatus status, Pageable pageable) {
        return overtimeRecordRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<OvertimeRecordResponse> getByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return overtimeRecordRepository.findByDateRange(startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    public OvertimeRecordResponse create(OvertimeRecordRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var policy = overtimePolicyRepository.findById(request.overtimePolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("OvertimePolicy", "id", request.overtimePolicyId()));

        var record = new OvertimeRecord();
        record.setEmployee(employee);
        record.setOvertimeDate(request.overtimeDate());
        record.setOvertimePolicy(policy);
        record.setOvertimeMinutes(request.overtimeMinutes());
        record.setSource(OvertimeSource.MANUAL);
        record.setShiftStartTime(request.shiftStartTime());
        record.setShiftEndTime(request.shiftEndTime());
        record.setActualStartTime(request.actualStartTime());
        record.setActualEndTime(request.actualEndTime());
        record.setRemarks(request.remarks());

        var saved = overtimeRecordRepository.save(record);
        log.info("Created overtime record with id: {} for employee: {}", saved.getId(), employee.getEmployeeCode());
        return mapToResponse(saved);
    }

    public OvertimeRecordResponse update(Long id, OvertimeRecordRequest request) {
        var record = overtimeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OvertimeRecord", "id", id));

        if (record.getStatus() != OvertimeStatus.PENDING) {
            throw new BadRequestException("Cannot update overtime record with status: " + record.getStatus());
        }

        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var policy = overtimePolicyRepository.findById(request.overtimePolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("OvertimePolicy", "id", request.overtimePolicyId()));

        record.setEmployee(employee);
        record.setOvertimeDate(request.overtimeDate());
        record.setOvertimePolicy(policy);
        record.setOvertimeMinutes(request.overtimeMinutes());
        record.setShiftStartTime(request.shiftStartTime());
        record.setShiftEndTime(request.shiftEndTime());
        record.setActualStartTime(request.actualStartTime());
        record.setActualEndTime(request.actualEndTime());
        record.setRemarks(request.remarks());

        var saved = overtimeRecordRepository.save(record);
        log.info("Updated overtime record with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public OvertimeRecordResponse approve(Long id, OvertimeApprovalRequest request) {
        var record = overtimeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OvertimeRecord", "id", id));

        if (record.getStatus() != OvertimeStatus.PENDING) {
            throw new BadRequestException("Cannot change status of overtime record with status: " + record.getStatus());
        }

        var newStatus = OvertimeStatus.valueOf(request.status());
        if (newStatus != OvertimeStatus.APPROVED && newStatus != OvertimeStatus.REJECTED) {
            throw new BadRequestException("Status must be APPROVED or REJECTED");
        }

        record.setStatus(newStatus);
        record.setApprovedBy(request.approvedBy());
        record.setApprovedAt(LocalDateTime.now());
        if (request.remarks() != null) {
            record.setRemarks(request.remarks());
        }

        var saved = overtimeRecordRepository.save(record);
        log.info("Updated overtime record id: {} status to {}", saved.getId(), newStatus);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var record = overtimeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OvertimeRecord", "id", id));
        overtimeRecordRepository.delete(record);
        log.info("Deleted overtime record with id: {}", id);
    }

    /**
     * Auto-detect overtime from attendance punch data for a given employee and date.
     * Compares actual punch times against the employee's assigned shift to calculate overtime.
     */
    public List<OvertimeRecordResponse> detectOvertime(Long employeeId, LocalDate date, Long policyId) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        var policy = overtimePolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("OvertimePolicy", "id", policyId));

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

        if (firstIn.isEmpty() || lastOut.isEmpty()) {
            return List.of();
        }

        LocalDateTime actualStart = firstIn.get().getPunchTime();
        LocalDateTime actualEnd = lastOut.get().getPunchTime();

        // Calculate expected work duration
        long expectedMinutes = Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();
        if (expectedMinutes < 0) {
            expectedMinutes += 24 * 60; // Handle overnight shifts
        }
        expectedMinutes -= shift.getBreakDurationMinutes();

        // Calculate actual work duration
        long actualMinutes = Duration.between(actualStart, actualEnd).toMinutes();
        actualMinutes -= shift.getBreakDurationMinutes();

        long overtimeMinutes = actualMinutes - expectedMinutes;

        // Apply minimum threshold
        if (overtimeMinutes < policy.getMinOvertimeMinutes()) {
            return List.of();
        }

        // Apply daily cap
        if (policy.getMaxOvertimeMinutesPerDay() != null && overtimeMinutes > policy.getMaxOvertimeMinutesPerDay()) {
            overtimeMinutes = policy.getMaxOvertimeMinutesPerDay();
        }

        // Check for existing records for same employee and date
        List<OvertimeRecord> existing = overtimeRecordRepository.findByEmployeeIdAndOvertimeDate(employeeId, date);
        if (!existing.isEmpty()) {
            log.info("Overtime record already exists for employee {} on date {}, skipping", employeeId, date);
            return List.of();
        }

        var record = new OvertimeRecord();
        record.setEmployee(employee);
        record.setOvertimeDate(date);
        record.setOvertimePolicy(policy);
        record.setOvertimeMinutes((int) overtimeMinutes);
        record.setSource(OvertimeSource.AUTO_DETECTED);
        record.setShiftStartTime(shift.getStartTime());
        record.setShiftEndTime(shift.getEndTime());
        record.setActualStartTime(actualStart);
        record.setActualEndTime(actualEnd);
        record.setRemarks("Auto-detected from attendance punches");

        var saved = overtimeRecordRepository.save(record);
        log.info("Auto-detected overtime of {} minutes for employee {} on {}",
                overtimeMinutes, employee.getEmployeeCode(), date);

        List<OvertimeRecordResponse> result = new ArrayList<>();
        result.add(mapToResponse(saved));
        return result;
    }

    /**
     * Get overtime summary for payroll integration, grouped by employee for a date range.
     */
    @Transactional(readOnly = true)
    public List<OvertimeSummaryResponse> getSummary(LocalDate startDate, LocalDate endDate) {
        var records = overtimeRecordRepository.findByDateRange(startDate, endDate, Pageable.unpaged()).getContent();

        return records.stream()
                .collect(Collectors.groupingBy(r -> r.getEmployee().getId()))
                .entrySet().stream()
                .map(entry -> {
                    var empRecords = entry.getValue();
                    var employee = empRecords.get(0).getEmployee();

                    int totalMinutes = empRecords.stream()
                            .mapToInt(OvertimeRecord::getOvertimeMinutes).sum();

                    int approvedMinutes = empRecords.stream()
                            .filter(r -> r.getStatus() == OvertimeStatus.APPROVED)
                            .mapToInt(OvertimeRecord::getOvertimeMinutes).sum();

                    int pendingMinutes = empRecords.stream()
                            .filter(r -> r.getStatus() == OvertimeStatus.PENDING)
                            .mapToInt(OvertimeRecord::getOvertimeMinutes).sum();

                    int rejectedMinutes = empRecords.stream()
                            .filter(r -> r.getStatus() == OvertimeStatus.REJECTED)
                            .mapToInt(OvertimeRecord::getOvertimeMinutes).sum();

                    // Calculate weighted minutes (approved minutes * rate multiplier)
                    BigDecimal weightedMinutes = empRecords.stream()
                            .filter(r -> r.getStatus() == OvertimeStatus.APPROVED)
                            .map(r -> r.getOvertimePolicy().getRateMultiplier()
                                    .multiply(BigDecimal.valueOf(r.getOvertimeMinutes())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new OvertimeSummaryResponse(
                            employee.getId(),
                            employee.getEmployeeCode(),
                            employee.getFirstName() + " " + employee.getLastName(),
                            totalMinutes,
                            approvedMinutes,
                            pendingMinutes,
                            rejectedMinutes,
                            weightedMinutes,
                            empRecords.size()
                    );
                })
                .toList();
    }

    private OvertimeRecordResponse mapToResponse(OvertimeRecord record) {
        var employee = record.getEmployee();
        var policy = record.getOvertimePolicy();
        return new OvertimeRecordResponse(
                record.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                record.getOvertimeDate(),
                policy.getId(),
                policy.getName(),
                policy.getType().name(),
                record.getOvertimeMinutes(),
                record.getStatus().name(),
                record.getSource().name(),
                record.getShiftStartTime(),
                record.getShiftEndTime(),
                record.getActualStartTime(),
                record.getActualEndTime(),
                record.getRemarks(),
                record.getApprovedBy(),
                record.getApprovedAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
