package com.raster.hrm.overtime;

import com.raster.hrm.attendance.entity.AttendancePunch;
import com.raster.hrm.attendance.entity.PunchDirection;
import com.raster.hrm.attendance.repository.AttendancePunchRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.overtime.dto.OvertimeApprovalRequest;
import com.raster.hrm.overtime.dto.OvertimeRecordRequest;
import com.raster.hrm.overtime.entity.OvertimePolicy;
import com.raster.hrm.overtime.entity.OvertimePolicyType;
import com.raster.hrm.overtime.entity.OvertimeRecord;
import com.raster.hrm.overtime.entity.OvertimeSource;
import com.raster.hrm.overtime.entity.OvertimeStatus;
import com.raster.hrm.overtime.repository.OvertimePolicyRepository;
import com.raster.hrm.overtime.repository.OvertimeRecordRepository;
import com.raster.hrm.overtime.service.OvertimeRecordService;
import com.raster.hrm.shift.entity.Shift;
import com.raster.hrm.shiftroster.entity.ShiftRoster;
import com.raster.hrm.shiftroster.repository.ShiftRosterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OvertimeRecordServiceTest {

    @Mock
    private OvertimeRecordRepository overtimeRecordRepository;

    @Mock
    private OvertimePolicyRepository overtimePolicyRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendancePunchRepository attendancePunchRepository;

    @Mock
    private ShiftRosterRepository shiftRosterRepository;

    @InjectMocks
    private OvertimeRecordService overtimeRecordService;

    private Employee createEmployee() {
        var employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        return employee;
    }

    private OvertimePolicy createPolicy() {
        var policy = new OvertimePolicy();
        policy.setId(1L);
        policy.setName("Weekday Overtime");
        policy.setType(OvertimePolicyType.WEEKDAY);
        policy.setRateMultiplier(new BigDecimal("1.50"));
        policy.setMinOvertimeMinutes(30);
        policy.setMaxOvertimeMinutesPerDay(480);
        policy.setMaxOvertimeMinutesPerMonth(2400);
        policy.setRequiresApproval(true);
        policy.setActive(true);
        policy.setDescription("Test policy");
        policy.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        policy.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return policy;
    }

    private OvertimeRecord createRecord(Long id, Employee employee, OvertimePolicy policy) {
        var record = new OvertimeRecord();
        record.setId(id);
        record.setEmployee(employee);
        record.setOvertimeDate(LocalDate.of(2024, 1, 15));
        record.setOvertimePolicy(policy);
        record.setOvertimeMinutes(60);
        record.setStatus(OvertimeStatus.PENDING);
        record.setSource(OvertimeSource.MANUAL);
        record.setShiftStartTime(LocalTime.of(9, 0));
        record.setShiftEndTime(LocalTime.of(17, 0));
        record.setActualStartTime(LocalDateTime.of(2024, 1, 15, 8, 45));
        record.setActualEndTime(LocalDateTime.of(2024, 1, 15, 18, 0));
        record.setRemarks("Test overtime");
        record.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        record.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return record;
    }

    private OvertimeRecordRequest createRequest() {
        return new OvertimeRecordRequest(
                1L,
                LocalDate.of(2024, 1, 15),
                1L,
                60,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                LocalDateTime.of(2024, 1, 15, 8, 45),
                LocalDateTime.of(2024, 1, 15, 18, 0),
                "Test overtime"
        );
    }

    private Shift createShift() {
        var shift = new Shift();
        shift.setId(1L);
        shift.setName("Morning");
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(17, 0));
        shift.setBreakDurationMinutes(60);
        return shift;
    }

    @Test
    void getAll_shouldReturnPageOfRecords() {
        var employee = createEmployee();
        var policy = createPolicy();
        var records = List.of(
                createRecord(1L, employee, policy),
                createRecord(2L, employee, policy)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(records, pageable, 2);
        when(overtimeRecordRepository.findAll(pageable)).thenReturn(page);

        var result = overtimeRecordService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).employeeName());
        verify(overtimeRecordRepository).findAll(pageable);
    }

    @Test
    void getById_shouldReturnRecord() {
        var employee = createEmployee();
        var policy = createPolicy();
        var record = createRecord(1L, employee, policy);
        when(overtimeRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        var result = overtimeRecordService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals(60, result.overtimeMinutes());
        assertEquals("PENDING", result.status());
        assertEquals("MANUAL", result.source());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(overtimeRecordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> overtimeRecordService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnPage() {
        var employee = createEmployee();
        var policy = createPolicy();
        var records = List.of(createRecord(1L, employee, policy));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(records, pageable, 1);
        when(overtimeRecordRepository.findByEmployeeId(1L, pageable)).thenReturn(page);

        var result = overtimeRecordService.getByEmployeeId(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).employeeId());
    }

    @Test
    void getByStatus_shouldReturnPage() {
        var employee = createEmployee();
        var policy = createPolicy();
        var records = List.of(createRecord(1L, employee, policy));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(records, pageable, 1);
        when(overtimeRecordRepository.findByStatus(OvertimeStatus.PENDING, pageable)).thenReturn(page);

        var result = overtimeRecordService.getByStatus(OvertimeStatus.PENDING, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("PENDING", result.getContent().get(0).status());
    }

    @Test
    void getByDateRange_shouldReturnPage() {
        var employee = createEmployee();
        var policy = createPolicy();
        var records = List.of(createRecord(1L, employee, policy));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(records, pageable, 1);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        when(overtimeRecordRepository.findByDateRange(startDate, endDate, pageable)).thenReturn(page);

        var result = overtimeRecordService.getByDateRange(startDate, endDate, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void create_shouldCreateAndReturnRecord() {
        var employee = createEmployee();
        var policy = createPolicy();
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(overtimeRecordRepository.save(any(OvertimeRecord.class))).thenAnswer(invocation -> {
            OvertimeRecord r = invocation.getArgument(0);
            r.setId(1L);
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(LocalDateTime.now());
            return r;
        });

        var result = overtimeRecordService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("John Doe", result.employeeName());
        assertEquals(60, result.overtimeMinutes());
        assertEquals("MANUAL", result.source());
        verify(overtimeRecordRepository).save(any(OvertimeRecord.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> overtimeRecordService.create(request));
        verify(overtimeRecordRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenPolicyNotFound() {
        var employee = createEmployee();
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> overtimeRecordService.create(request));
        verify(overtimeRecordRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateAndReturnRecord() {
        var employee = createEmployee();
        var policy = createPolicy();
        var record = createRecord(1L, employee, policy);
        var request = createRequest();
        when(overtimeRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(overtimeRecordRepository.save(any(OvertimeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = overtimeRecordService.update(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(60, result.overtimeMinutes());
        verify(overtimeRecordRepository).save(any(OvertimeRecord.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var request = createRequest();
        when(overtimeRecordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> overtimeRecordService.update(999L, request));
        verify(overtimeRecordRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNotPending() {
        var employee = createEmployee();
        var policy = createPolicy();
        var record = createRecord(1L, employee, policy);
        record.setStatus(OvertimeStatus.APPROVED);
        var request = createRequest();
        when(overtimeRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        var ex = assertThrows(BadRequestException.class,
                () -> overtimeRecordService.update(1L, request));
        assertTrue(ex.getMessage().contains("APPROVED"));
        verify(overtimeRecordRepository, never()).save(any());
    }

    @Test
    void approve_shouldApproveRecord() {
        var employee = createEmployee();
        var policy = createPolicy();
        var record = createRecord(1L, employee, policy);
        var approvalRequest = new OvertimeApprovalRequest("APPROVED", "admin", "Looks good");
        when(overtimeRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(overtimeRecordRepository.save(any(OvertimeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = overtimeRecordService.approve(1L, approvalRequest);

        assertEquals("APPROVED", result.status());
        assertEquals("admin", result.approvedBy());
        verify(overtimeRecordRepository).save(any(OvertimeRecord.class));
    }

    @Test
    void approve_shouldRejectRecord() {
        var employee = createEmployee();
        var policy = createPolicy();
        var record = createRecord(1L, employee, policy);
        var approvalRequest = new OvertimeApprovalRequest("REJECTED", "admin", "Not valid");
        when(overtimeRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(overtimeRecordRepository.save(any(OvertimeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = overtimeRecordService.approve(1L, approvalRequest);

        assertEquals("REJECTED", result.status());
        assertEquals("admin", result.approvedBy());
    }

    @Test
    void approve_shouldThrowWhenNotFound() {
        var approvalRequest = new OvertimeApprovalRequest("APPROVED", "admin", null);
        when(overtimeRecordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> overtimeRecordService.approve(999L, approvalRequest));
        verify(overtimeRecordRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenAlreadyApproved() {
        var employee = createEmployee();
        var policy = createPolicy();
        var record = createRecord(1L, employee, policy);
        record.setStatus(OvertimeStatus.APPROVED);
        var approvalRequest = new OvertimeApprovalRequest("REJECTED", "admin", null);
        when(overtimeRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        var ex = assertThrows(BadRequestException.class,
                () -> overtimeRecordService.approve(1L, approvalRequest));
        assertTrue(ex.getMessage().contains("APPROVED"));
        verify(overtimeRecordRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteRecord() {
        var employee = createEmployee();
        var policy = createPolicy();
        var record = createRecord(1L, employee, policy);
        when(overtimeRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        overtimeRecordService.delete(1L);

        verify(overtimeRecordRepository).delete(record);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(overtimeRecordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> overtimeRecordService.delete(999L));
        verify(overtimeRecordRepository, never()).delete(any());
    }

    @Test
    void detectOvertime_shouldDetectAndCreateRecord() {
        var employee = createEmployee();
        var policy = createPolicy();
        var shift = createShift();
        var date = LocalDate.of(2024, 1, 15);

        var roster = new ShiftRoster();
        roster.setShift(shift);
        roster.setEmployee(employee);

        var punchIn = new AttendancePunch();
        punchIn.setDirection(PunchDirection.IN);
        punchIn.setPunchTime(LocalDateTime.of(2024, 1, 15, 8, 0));
        punchIn.setEmployee(employee);

        var punchOut = new AttendancePunch();
        punchOut.setDirection(PunchDirection.OUT);
        punchOut.setPunchTime(LocalDateTime.of(2024, 1, 15, 19, 0));
        punchOut.setEmployee(employee);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(shiftRosterRepository.findOverlapping(1L, date, date)).thenReturn(List.of(roster));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(punchIn, punchOut)));
        when(overtimeRecordRepository.findByEmployeeIdAndOvertimeDate(1L, date)).thenReturn(List.of());
        when(overtimeRecordRepository.save(any(OvertimeRecord.class))).thenAnswer(invocation -> {
            OvertimeRecord r = invocation.getArgument(0);
            r.setId(1L);
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(LocalDateTime.now());
            return r;
        });

        // Actual work: 8:00 to 19:00 = 660 min - 60 break = 600 min
        // Expected: 9:00 to 17:00 = 480 min - 60 break = 420 min
        // Overtime: 600 - 420 = 180 min (capped at maxOvertimeMinutesPerDay=480)
        var result = overtimeRecordService.detectOvertime(1L, date, 1L);

        assertEquals(1, result.size());
        assertEquals("AUTO_DETECTED", result.get(0).source());
        verify(overtimeRecordRepository).save(any(OvertimeRecord.class));
    }

    @Test
    void detectOvertime_shouldThrowWhenNoShiftRoster() {
        var employee = createEmployee();
        var policy = createPolicy();
        var date = LocalDate.of(2024, 1, 15);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(shiftRosterRepository.findOverlapping(1L, date, date)).thenReturn(List.of());

        assertThrows(BadRequestException.class,
                () -> overtimeRecordService.detectOvertime(1L, date, 1L));
        verify(overtimeRecordRepository, never()).save(any());
    }

    @Test
    void detectOvertime_shouldReturnEmptyWhenNoPunches() {
        var employee = createEmployee();
        var policy = createPolicy();
        var shift = createShift();
        var date = LocalDate.of(2024, 1, 15);

        var roster = new ShiftRoster();
        roster.setShift(shift);
        roster.setEmployee(employee);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(shiftRosterRepository.findOverlapping(1L, date, date)).thenReturn(List.of(roster));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var result = overtimeRecordService.detectOvertime(1L, date, 1L);

        assertTrue(result.isEmpty());
        verify(overtimeRecordRepository, never()).save(any());
    }

    @Test
    void detectOvertime_shouldReturnEmptyWhenBelowThreshold() {
        var employee = createEmployee();
        var policy = createPolicy();
        var shift = createShift();
        var date = LocalDate.of(2024, 1, 15);

        var roster = new ShiftRoster();
        roster.setShift(shift);
        roster.setEmployee(employee);

        // Only 10 minutes overtime (below 30 min threshold)
        var punchIn = new AttendancePunch();
        punchIn.setDirection(PunchDirection.IN);
        punchIn.setPunchTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        punchIn.setEmployee(employee);

        var punchOut = new AttendancePunch();
        punchOut.setDirection(PunchDirection.OUT);
        punchOut.setPunchTime(LocalDateTime.of(2024, 1, 15, 17, 10));
        punchOut.setEmployee(employee);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(shiftRosterRepository.findOverlapping(1L, date, date)).thenReturn(List.of(roster));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(punchIn, punchOut)));

        // Actual: 9:00 to 17:10 = 490 min - 60 break = 430 min
        // Expected: 9:00 to 17:00 = 480 min - 60 break = 420 min
        // Overtime: 430 - 420 = 10 min (below 30 min threshold)
        var result = overtimeRecordService.detectOvertime(1L, date, 1L);

        assertTrue(result.isEmpty());
        verify(overtimeRecordRepository, never()).save(any());
    }

    @Test
    void detectOvertime_shouldReturnEmptyWhenExistingRecord() {
        var employee = createEmployee();
        var policy = createPolicy();
        var shift = createShift();
        var date = LocalDate.of(2024, 1, 15);

        var roster = new ShiftRoster();
        roster.setShift(shift);
        roster.setEmployee(employee);

        var punchIn = new AttendancePunch();
        punchIn.setDirection(PunchDirection.IN);
        punchIn.setPunchTime(LocalDateTime.of(2024, 1, 15, 8, 0));
        punchIn.setEmployee(employee);

        var punchOut = new AttendancePunch();
        punchOut.setDirection(PunchDirection.OUT);
        punchOut.setPunchTime(LocalDateTime.of(2024, 1, 15, 19, 0));
        punchOut.setEmployee(employee);

        var existingRecord = createRecord(99L, employee, policy);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(overtimePolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(shiftRosterRepository.findOverlapping(1L, date, date)).thenReturn(List.of(roster));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(punchIn, punchOut)));
        when(overtimeRecordRepository.findByEmployeeIdAndOvertimeDate(1L, date))
                .thenReturn(List.of(existingRecord));

        var result = overtimeRecordService.detectOvertime(1L, date, 1L);

        assertTrue(result.isEmpty());
        verify(overtimeRecordRepository, never()).save(any());
    }

    @Test
    void getSummary_shouldReturnSummaries() {
        var employee = createEmployee();
        var policy = createPolicy();

        var record1 = createRecord(1L, employee, policy);
        record1.setStatus(OvertimeStatus.APPROVED);
        record1.setOvertimeMinutes(60);

        var record2 = createRecord(2L, employee, policy);
        record2.setStatus(OvertimeStatus.PENDING);
        record2.setOvertimeMinutes(45);

        var record3 = createRecord(3L, employee, policy);
        record3.setStatus(OvertimeStatus.REJECTED);
        record3.setOvertimeMinutes(30);

        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var page = new PageImpl<>(List.of(record1, record2, record3));
        when(overtimeRecordRepository.findByDateRange(eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(page);

        var result = overtimeRecordService.getSummary(startDate, endDate);

        assertEquals(1, result.size());
        var summary = result.get(0);
        assertEquals(1L, summary.employeeId());
        assertEquals("EMP001", summary.employeeCode());
        assertEquals("John Doe", summary.employeeName());
        assertEquals(135, summary.totalOvertimeMinutes());
        assertEquals(60, summary.approvedOvertimeMinutes());
        assertEquals(45, summary.pendingOvertimeMinutes());
        assertEquals(30, summary.rejectedOvertimeMinutes());
        assertEquals(new BigDecimal("90.00"), summary.weightedOvertimeMinutes());
        assertEquals(3, summary.recordCount());
    }
}
