package com.raster.hrm.attendancedeviation;

import com.raster.hrm.attendance.entity.AttendancePunch;
import com.raster.hrm.attendance.entity.PunchDirection;
import com.raster.hrm.attendance.repository.AttendancePunchRepository;
import com.raster.hrm.attendancedeviation.dto.AttendanceDeviationRequest;
import com.raster.hrm.attendancedeviation.dto.DeviationApprovalRequest;
import com.raster.hrm.attendancedeviation.entity.AttendanceDeviation;
import com.raster.hrm.attendancedeviation.entity.DeviationStatus;
import com.raster.hrm.attendancedeviation.entity.DeviationType;
import com.raster.hrm.attendancedeviation.entity.PenaltyAction;
import com.raster.hrm.attendancedeviation.repository.AttendanceDeviationRepository;
import com.raster.hrm.attendancedeviation.service.AttendanceDeviationService;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
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
class AttendanceDeviationServiceTest {

    @Mock
    private AttendanceDeviationRepository attendanceDeviationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendancePunchRepository attendancePunchRepository;

    @Mock
    private ShiftRosterRepository shiftRosterRepository;

    @InjectMocks
    private AttendanceDeviationService attendanceDeviationService;

    private Employee createEmployee() {
        var employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        return employee;
    }

    private AttendanceDeviation createDeviation(Long id, Employee employee, DeviationType type) {
        var deviation = new AttendanceDeviation();
        deviation.setId(id);
        deviation.setEmployee(employee);
        deviation.setDeviationDate(LocalDate.of(2024, 1, 15));
        deviation.setType(type);
        deviation.setDeviationMinutes(15);
        deviation.setScheduledTime(type == DeviationType.LATE_COMING ? LocalTime.of(9, 0) : LocalTime.of(17, 0));
        deviation.setActualTime(type == DeviationType.LATE_COMING
                ? LocalDateTime.of(2024, 1, 15, 9, 15)
                : LocalDateTime.of(2024, 1, 15, 16, 45));
        deviation.setGracePeriodMinutes(5);
        deviation.setPenaltyAction(PenaltyAction.NONE);
        deviation.setStatus(DeviationStatus.PENDING);
        deviation.setRemarks("Test deviation");
        deviation.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        deviation.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return deviation;
    }

    private AttendanceDeviationRequest createRequest() {
        return new AttendanceDeviationRequest(
                1L,
                LocalDate.of(2024, 1, 15),
                "LATE_COMING",
                15,
                LocalTime.of(9, 0),
                LocalDateTime.of(2024, 1, 15, 9, 15),
                5,
                "NONE",
                "Test deviation"
        );
    }

    private Shift createShift() {
        var shift = new Shift();
        shift.setId(1L);
        shift.setName("Morning");
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(17, 0));
        shift.setBreakDurationMinutes(60);
        shift.setGracePeriodMinutes(5);
        return shift;
    }

    @Test
    void getAll_shouldReturnPageOfDeviations() {
        var employee = createEmployee();
        var deviations = List.of(
                createDeviation(1L, employee, DeviationType.LATE_COMING),
                createDeviation(2L, employee, DeviationType.EARLY_GOING)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(deviations, pageable, 2);
        when(attendanceDeviationRepository.findAll(pageable)).thenReturn(page);

        var result = attendanceDeviationService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).employeeName());
        verify(attendanceDeviationRepository).findAll(pageable);
    }

    @Test
    void getById_shouldReturnDeviation() {
        var employee = createEmployee();
        var deviation = createDeviation(1L, employee, DeviationType.LATE_COMING);
        when(attendanceDeviationRepository.findById(1L)).thenReturn(Optional.of(deviation));

        var result = attendanceDeviationService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals(15, result.deviationMinutes());
        assertEquals("PENDING", result.status());
        assertEquals("LATE_COMING", result.type());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(attendanceDeviationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceDeviationService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnPage() {
        var employee = createEmployee();
        var deviations = List.of(createDeviation(1L, employee, DeviationType.LATE_COMING));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(deviations, pageable, 1);
        when(attendanceDeviationRepository.findByEmployeeId(1L, pageable)).thenReturn(page);

        var result = attendanceDeviationService.getByEmployeeId(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).employeeId());
    }

    @Test
    void getByType_shouldReturnPage() {
        var employee = createEmployee();
        var deviations = List.of(createDeviation(1L, employee, DeviationType.LATE_COMING));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(deviations, pageable, 1);
        when(attendanceDeviationRepository.findByType(DeviationType.LATE_COMING, pageable)).thenReturn(page);

        var result = attendanceDeviationService.getByType(DeviationType.LATE_COMING, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("LATE_COMING", result.getContent().get(0).type());
    }

    @Test
    void getByStatus_shouldReturnPage() {
        var employee = createEmployee();
        var deviations = List.of(createDeviation(1L, employee, DeviationType.LATE_COMING));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(deviations, pageable, 1);
        when(attendanceDeviationRepository.findByStatus(DeviationStatus.PENDING, pageable)).thenReturn(page);

        var result = attendanceDeviationService.getByStatus(DeviationStatus.PENDING, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("PENDING", result.getContent().get(0).status());
    }

    @Test
    void getByDateRange_shouldReturnPage() {
        var employee = createEmployee();
        var deviations = List.of(createDeviation(1L, employee, DeviationType.LATE_COMING));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(deviations, pageable, 1);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        when(attendanceDeviationRepository.findByDateRange(startDate, endDate, pageable)).thenReturn(page);

        var result = attendanceDeviationService.getByDateRange(startDate, endDate, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void create_shouldCreateAndReturnDeviation() {
        var employee = createEmployee();
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceDeviationRepository.save(any(AttendanceDeviation.class))).thenAnswer(invocation -> {
            AttendanceDeviation d = invocation.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(LocalDateTime.now());
            d.setUpdatedAt(LocalDateTime.now());
            return d;
        });

        var result = attendanceDeviationService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("John Doe", result.employeeName());
        assertEquals(15, result.deviationMinutes());
        assertEquals("LATE_COMING", result.type());
        verify(attendanceDeviationRepository).save(any(AttendanceDeviation.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceDeviationService.create(request));
        verify(attendanceDeviationRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateAndReturnDeviation() {
        var employee = createEmployee();
        var deviation = createDeviation(1L, employee, DeviationType.LATE_COMING);
        var request = createRequest();
        when(attendanceDeviationRepository.findById(1L)).thenReturn(Optional.of(deviation));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceDeviationRepository.save(any(AttendanceDeviation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = attendanceDeviationService.update(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(15, result.deviationMinutes());
        verify(attendanceDeviationRepository).save(any(AttendanceDeviation.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var request = createRequest();
        when(attendanceDeviationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceDeviationService.update(999L, request));
        verify(attendanceDeviationRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNotPending() {
        var employee = createEmployee();
        var deviation = createDeviation(1L, employee, DeviationType.LATE_COMING);
        deviation.setStatus(DeviationStatus.APPROVED);
        var request = createRequest();
        when(attendanceDeviationRepository.findById(1L)).thenReturn(Optional.of(deviation));

        var ex = assertThrows(BadRequestException.class,
                () -> attendanceDeviationService.update(1L, request));
        assertTrue(ex.getMessage().contains("APPROVED"));
        verify(attendanceDeviationRepository, never()).save(any());
    }

    @Test
    void approve_shouldApproveDeviation() {
        var employee = createEmployee();
        var deviation = createDeviation(1L, employee, DeviationType.LATE_COMING);
        var approvalRequest = new DeviationApprovalRequest("APPROVED", "admin", "Acknowledged");
        when(attendanceDeviationRepository.findById(1L)).thenReturn(Optional.of(deviation));
        when(attendanceDeviationRepository.save(any(AttendanceDeviation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = attendanceDeviationService.approve(1L, approvalRequest);

        assertEquals("APPROVED", result.status());
        assertEquals("admin", result.approvedBy());
        verify(attendanceDeviationRepository).save(any(AttendanceDeviation.class));
    }

    @Test
    void approve_shouldWaiveDeviation() {
        var employee = createEmployee();
        var deviation = createDeviation(1L, employee, DeviationType.LATE_COMING);
        var approvalRequest = new DeviationApprovalRequest("WAIVED", "admin", "Emergency");
        when(attendanceDeviationRepository.findById(1L)).thenReturn(Optional.of(deviation));
        when(attendanceDeviationRepository.save(any(AttendanceDeviation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = attendanceDeviationService.approve(1L, approvalRequest);

        assertEquals("WAIVED", result.status());
        assertEquals("admin", result.approvedBy());
    }

    @Test
    void approve_shouldThrowWhenNotFound() {
        var approvalRequest = new DeviationApprovalRequest("APPROVED", "admin", null);
        when(attendanceDeviationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceDeviationService.approve(999L, approvalRequest));
        verify(attendanceDeviationRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenAlreadyApproved() {
        var employee = createEmployee();
        var deviation = createDeviation(1L, employee, DeviationType.LATE_COMING);
        deviation.setStatus(DeviationStatus.APPROVED);
        var approvalRequest = new DeviationApprovalRequest("WAIVED", "admin", null);
        when(attendanceDeviationRepository.findById(1L)).thenReturn(Optional.of(deviation));

        var ex = assertThrows(BadRequestException.class,
                () -> attendanceDeviationService.approve(1L, approvalRequest));
        assertTrue(ex.getMessage().contains("APPROVED"));
        verify(attendanceDeviationRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenInvalidStatus() {
        var employee = createEmployee();
        var deviation = createDeviation(1L, employee, DeviationType.LATE_COMING);
        var approvalRequest = new DeviationApprovalRequest("PENDING", "admin", null);
        when(attendanceDeviationRepository.findById(1L)).thenReturn(Optional.of(deviation));

        assertThrows(BadRequestException.class,
                () -> attendanceDeviationService.approve(1L, approvalRequest));
        verify(attendanceDeviationRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteDeviation() {
        var employee = createEmployee();
        var deviation = createDeviation(1L, employee, DeviationType.LATE_COMING);
        when(attendanceDeviationRepository.findById(1L)).thenReturn(Optional.of(deviation));

        attendanceDeviationService.delete(1L);

        verify(attendanceDeviationRepository).delete(deviation);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(attendanceDeviationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceDeviationService.delete(999L));
        verify(attendanceDeviationRepository, never()).delete(any());
    }

    @Test
    void detectDeviations_shouldDetectLateComing() {
        var employee = createEmployee();
        var shift = createShift();
        var date = LocalDate.of(2024, 1, 15);

        var roster = new ShiftRoster();
        roster.setShift(shift);
        roster.setEmployee(employee);

        // Employee arrives at 9:20, shift starts at 9:00, grace period 5 min → 20 min late
        var punchIn = new AttendancePunch();
        punchIn.setDirection(PunchDirection.IN);
        punchIn.setPunchTime(LocalDateTime.of(2024, 1, 15, 9, 20));
        punchIn.setEmployee(employee);

        var punchOut = new AttendancePunch();
        punchOut.setDirection(PunchDirection.OUT);
        punchOut.setPunchTime(LocalDateTime.of(2024, 1, 15, 17, 0));
        punchOut.setEmployee(employee);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRosterRepository.findOverlapping(1L, date, date)).thenReturn(List.of(roster));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(punchIn, punchOut)));
        when(attendanceDeviationRepository.findByEmployeeIdAndDeviationDateAndType(1L, date, DeviationType.LATE_COMING))
                .thenReturn(List.of());
        when(attendanceDeviationRepository.save(any(AttendanceDeviation.class))).thenAnswer(invocation -> {
            AttendanceDeviation d = invocation.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(LocalDateTime.now());
            d.setUpdatedAt(LocalDateTime.now());
            return d;
        });

        var result = attendanceDeviationService.detectDeviations(1L, date);

        assertEquals(1, result.size());
        assertEquals("LATE_COMING", result.get(0).type());
        assertEquals(20, result.get(0).deviationMinutes());
        verify(attendanceDeviationRepository).save(any(AttendanceDeviation.class));
    }

    @Test
    void detectDeviations_shouldDetectEarlyGoing() {
        var employee = createEmployee();
        var shift = createShift();
        var date = LocalDate.of(2024, 1, 15);

        var roster = new ShiftRoster();
        roster.setShift(shift);
        roster.setEmployee(employee);

        // Employee leaves at 16:30, shift ends at 17:00, grace period 5 min → 30 min early
        var punchIn = new AttendancePunch();
        punchIn.setDirection(PunchDirection.IN);
        punchIn.setPunchTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        punchIn.setEmployee(employee);

        var punchOut = new AttendancePunch();
        punchOut.setDirection(PunchDirection.OUT);
        punchOut.setPunchTime(LocalDateTime.of(2024, 1, 15, 16, 30));
        punchOut.setEmployee(employee);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRosterRepository.findOverlapping(1L, date, date)).thenReturn(List.of(roster));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(punchIn, punchOut)));
        when(attendanceDeviationRepository.findByEmployeeIdAndDeviationDateAndType(1L, date, DeviationType.EARLY_GOING))
                .thenReturn(List.of());
        when(attendanceDeviationRepository.save(any(AttendanceDeviation.class))).thenAnswer(invocation -> {
            AttendanceDeviation d = invocation.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(LocalDateTime.now());
            d.setUpdatedAt(LocalDateTime.now());
            return d;
        });

        var result = attendanceDeviationService.detectDeviations(1L, date);

        assertEquals(1, result.size());
        assertEquals("EARLY_GOING", result.get(0).type());
        assertEquals(30, result.get(0).deviationMinutes());
        verify(attendanceDeviationRepository).save(any(AttendanceDeviation.class));
    }

    @Test
    void detectDeviations_shouldDetectBothLateAndEarly() {
        var employee = createEmployee();
        var shift = createShift();
        var date = LocalDate.of(2024, 1, 15);

        var roster = new ShiftRoster();
        roster.setShift(shift);
        roster.setEmployee(employee);

        // Late coming (9:20) and early going (16:30)
        var punchIn = new AttendancePunch();
        punchIn.setDirection(PunchDirection.IN);
        punchIn.setPunchTime(LocalDateTime.of(2024, 1, 15, 9, 20));
        punchIn.setEmployee(employee);

        var punchOut = new AttendancePunch();
        punchOut.setDirection(PunchDirection.OUT);
        punchOut.setPunchTime(LocalDateTime.of(2024, 1, 15, 16, 30));
        punchOut.setEmployee(employee);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRosterRepository.findOverlapping(1L, date, date)).thenReturn(List.of(roster));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(punchIn, punchOut)));
        when(attendanceDeviationRepository.findByEmployeeIdAndDeviationDateAndType(eq(1L), eq(date), any(DeviationType.class)))
                .thenReturn(List.of());
        when(attendanceDeviationRepository.save(any(AttendanceDeviation.class))).thenAnswer(invocation -> {
            AttendanceDeviation d = invocation.getArgument(0);
            d.setId(d.getType() == DeviationType.LATE_COMING ? 1L : 2L);
            d.setCreatedAt(LocalDateTime.now());
            d.setUpdatedAt(LocalDateTime.now());
            return d;
        });

        var result = attendanceDeviationService.detectDeviations(1L, date);

        assertEquals(2, result.size());
    }

    @Test
    void detectDeviations_shouldThrowWhenNoShiftRoster() {
        var employee = createEmployee();
        var date = LocalDate.of(2024, 1, 15);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRosterRepository.findOverlapping(1L, date, date)).thenReturn(List.of());

        assertThrows(BadRequestException.class,
                () -> attendanceDeviationService.detectDeviations(1L, date));
        verify(attendanceDeviationRepository, never()).save(any());
    }

    @Test
    void detectDeviations_shouldReturnEmptyWhenNoPunches() {
        var employee = createEmployee();
        var shift = createShift();
        var date = LocalDate.of(2024, 1, 15);

        var roster = new ShiftRoster();
        roster.setShift(shift);
        roster.setEmployee(employee);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRosterRepository.findOverlapping(1L, date, date)).thenReturn(List.of(roster));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var result = attendanceDeviationService.detectDeviations(1L, date);

        assertTrue(result.isEmpty());
        verify(attendanceDeviationRepository, never()).save(any());
    }

    @Test
    void detectDeviations_shouldSkipWhenWithinGracePeriod() {
        var employee = createEmployee();
        var shift = createShift();
        var date = LocalDate.of(2024, 1, 15);

        var roster = new ShiftRoster();
        roster.setShift(shift);
        roster.setEmployee(employee);

        // Employee arrives at 9:03 (within 5 min grace) and leaves at 16:57 (within 5 min grace)
        var punchIn = new AttendancePunch();
        punchIn.setDirection(PunchDirection.IN);
        punchIn.setPunchTime(LocalDateTime.of(2024, 1, 15, 9, 3));
        punchIn.setEmployee(employee);

        var punchOut = new AttendancePunch();
        punchOut.setDirection(PunchDirection.OUT);
        punchOut.setPunchTime(LocalDateTime.of(2024, 1, 15, 16, 57));
        punchOut.setEmployee(employee);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRosterRepository.findOverlapping(1L, date, date)).thenReturn(List.of(roster));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(punchIn, punchOut)));

        var result = attendanceDeviationService.detectDeviations(1L, date);

        assertTrue(result.isEmpty());
        verify(attendanceDeviationRepository, never()).save(any());
    }

    @Test
    void detectDeviations_shouldSkipWhenRecordAlreadyExists() {
        var employee = createEmployee();
        var shift = createShift();
        var date = LocalDate.of(2024, 1, 15);

        var roster = new ShiftRoster();
        roster.setShift(shift);
        roster.setEmployee(employee);

        var punchIn = new AttendancePunch();
        punchIn.setDirection(PunchDirection.IN);
        punchIn.setPunchTime(LocalDateTime.of(2024, 1, 15, 9, 20));
        punchIn.setEmployee(employee);

        var punchOut = new AttendancePunch();
        punchOut.setDirection(PunchDirection.OUT);
        punchOut.setPunchTime(LocalDateTime.of(2024, 1, 15, 17, 0));
        punchOut.setEmployee(employee);

        var existingDeviation = createDeviation(99L, employee, DeviationType.LATE_COMING);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRosterRepository.findOverlapping(1L, date, date)).thenReturn(List.of(roster));
        when(attendancePunchRepository.findByEmployeeIdAndPunchTimeBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(punchIn, punchOut)));
        when(attendanceDeviationRepository.findByEmployeeIdAndDeviationDateAndType(1L, date, DeviationType.LATE_COMING))
                .thenReturn(List.of(existingDeviation));

        var result = attendanceDeviationService.detectDeviations(1L, date);

        assertTrue(result.isEmpty());
        verify(attendanceDeviationRepository, never()).save(any());
    }

    @Test
    void detectDeviations_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceDeviationService.detectDeviations(999L, LocalDate.of(2024, 1, 15)));
        verify(attendanceDeviationRepository, never()).save(any());
    }

    @Test
    void getSummary_shouldReturnSummaries() {
        var employee = createEmployee();

        var deviation1 = createDeviation(1L, employee, DeviationType.LATE_COMING);
        deviation1.setDeviationMinutes(20);
        deviation1.setPenaltyAction(PenaltyAction.WARNING);

        var deviation2 = createDeviation(2L, employee, DeviationType.EARLY_GOING);
        deviation2.setDeviationMinutes(30);
        deviation2.setPenaltyAction(PenaltyAction.LEAVE_DEDUCTION);

        var deviation3 = createDeviation(3L, employee, DeviationType.LATE_COMING);
        deviation3.setDeviationMinutes(10);
        deviation3.setPenaltyAction(PenaltyAction.PAY_CUT);

        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var page = new PageImpl<>(List.of(deviation1, deviation2, deviation3));
        when(attendanceDeviationRepository.findByDateRange(eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(page);

        var result = attendanceDeviationService.getSummary(startDate, endDate);

        assertEquals(1, result.size());
        var summary = result.get(0);
        assertEquals(1L, summary.employeeId());
        assertEquals("EMP001", summary.employeeCode());
        assertEquals("John Doe", summary.employeeName());
        assertEquals(2, summary.lateComingCount());
        assertEquals(1, summary.earlyGoingCount());
        assertEquals(60, summary.totalDeviationMinutes());
        assertEquals(30, summary.lateComingMinutes());
        assertEquals(30, summary.earlyGoingMinutes());
        assertEquals(1, summary.warningCount());
        assertEquals(1, summary.leaveDeductionCount());
        assertEquals(1, summary.payCutCount());
    }

    @Test
    void create_shouldHandleNullGracePeriodAndPenalty() {
        var employee = createEmployee();
        var request = new AttendanceDeviationRequest(
                1L,
                LocalDate.of(2024, 1, 15),
                "EARLY_GOING",
                30,
                LocalTime.of(17, 0),
                LocalDateTime.of(2024, 1, 15, 16, 30),
                null,
                null,
                null
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceDeviationRepository.save(any(AttendanceDeviation.class))).thenAnswer(invocation -> {
            AttendanceDeviation d = invocation.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(LocalDateTime.now());
            d.setUpdatedAt(LocalDateTime.now());
            return d;
        });

        var result = attendanceDeviationService.create(request);

        assertNotNull(result);
        assertEquals("EARLY_GOING", result.type());
        assertEquals("NONE", result.penaltyAction());
        assertEquals(0, result.gracePeriodMinutes());
    }

    @Test
    void approve_shouldPreserveExistingRemarksWhenApprovalRemarksNull() {
        var employee = createEmployee();
        var deviation = createDeviation(1L, employee, DeviationType.LATE_COMING);
        deviation.setRemarks("Original remark");
        var approvalRequest = new DeviationApprovalRequest("APPROVED", "admin", null);
        when(attendanceDeviationRepository.findById(1L)).thenReturn(Optional.of(deviation));
        when(attendanceDeviationRepository.save(any(AttendanceDeviation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = attendanceDeviationService.approve(1L, approvalRequest);

        assertEquals("APPROVED", result.status());
        assertEquals("Original remark", result.remarks());
    }
}
