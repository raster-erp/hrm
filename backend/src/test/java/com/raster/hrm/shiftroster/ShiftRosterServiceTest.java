package com.raster.hrm.shiftroster;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.rotationpattern.entity.RotationPattern;
import com.raster.hrm.rotationpattern.repository.RotationPatternRepository;
import com.raster.hrm.shift.entity.Shift;
import com.raster.hrm.shift.entity.ShiftType;
import com.raster.hrm.shift.repository.ShiftRepository;
import com.raster.hrm.shiftroster.dto.BulkShiftRosterRequest;
import com.raster.hrm.shiftroster.dto.ShiftRosterRequest;
import com.raster.hrm.shiftroster.entity.ShiftRoster;
import com.raster.hrm.shiftroster.repository.ShiftRosterRepository;
import com.raster.hrm.shiftroster.service.ShiftRosterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShiftRosterServiceTest {

    @Mock
    private ShiftRosterRepository shiftRosterRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private RotationPatternRepository rotationPatternRepository;

    @InjectMocks
    private ShiftRosterService shiftRosterService;

    private Employee createEmployee(Long id, String code) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail(code + "@test.com");
        return employee;
    }

    private Shift createShift(Long id, String name) {
        var shift = new Shift();
        shift.setId(id);
        shift.setName(name);
        shift.setType(ShiftType.MORNING);
        shift.setStartTime(LocalTime.of(6, 0));
        shift.setEndTime(LocalTime.of(14, 0));
        return shift;
    }

    private RotationPattern createRotationPattern(Long id, String name) {
        var pattern = new RotationPattern();
        pattern.setId(id);
        pattern.setName(name);
        pattern.setRotationDays(7);
        pattern.setShiftSequence("1,2,3");
        return pattern;
    }

    private ShiftRoster createRoster(Long id, Employee employee, Shift shift) {
        var roster = new ShiftRoster();
        roster.setId(id);
        roster.setEmployee(employee);
        roster.setShift(shift);
        roster.setEffectiveDate(LocalDate.of(2024, 1, 1));
        roster.setEndDate(LocalDate.of(2024, 6, 30));
        roster.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        roster.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return roster;
    }

    private ShiftRosterRequest createRequest() {
        return new ShiftRosterRequest(1L, 1L, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 31), null);
    }

    @Test
    void getAll_shouldReturnPageOfRosters() {
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");
        var rosters = List.of(createRoster(1L, employee, shift));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(rosters, pageable, 1);
        when(shiftRosterRepository.findAll(pageable)).thenReturn(page);

        var result = shiftRosterService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).employeeName());
        assertEquals("Morning", result.getContent().get(0).shiftName());
        verify(shiftRosterRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<ShiftRoster>(List.of(), pageable, 0);
        when(shiftRosterRepository.findAll(pageable)).thenReturn(page);

        var result = shiftRosterService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnRoster() {
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");
        var roster = createRoster(1L, employee, shift);
        when(shiftRosterRepository.findById(1L)).thenReturn(Optional.of(roster));

        var result = shiftRosterService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("John Doe", result.employeeName());
        assertEquals("EMP-001", result.employeeCode());
        assertEquals(1L, result.shiftId());
        assertEquals("Morning", result.shiftName());
        assertEquals(LocalDate.of(2024, 1, 1), result.effectiveDate());
        assertEquals(LocalDate.of(2024, 6, 30), result.endDate());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(shiftRosterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shiftRosterService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnRosters() {
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");
        var rosters = List.of(createRoster(1L, employee, shift));
        when(shiftRosterRepository.findByEmployeeId(1L)).thenReturn(rosters);

        var result = shiftRosterService.getByEmployeeId(1L);

        assertEquals(1, result.size());
        assertEquals("EMP-001", result.get(0).employeeCode());
    }

    @Test
    void create_shouldCreateAndReturnRoster() {
        var request = createRequest();
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRosterRepository.findOverlapping(eq(1L), any(), any())).thenReturn(List.of());
        when(shiftRosterRepository.save(any(ShiftRoster.class))).thenAnswer(invocation -> {
            ShiftRoster r = invocation.getArgument(0);
            r.setId(1L);
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(LocalDateTime.now());
            return r;
        });

        var result = shiftRosterService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals(1L, result.shiftId());
        assertEquals(LocalDate.of(2024, 7, 1), result.effectiveDate());
        assertEquals(LocalDate.of(2024, 12, 31), result.endDate());
        assertNull(result.rotationPatternId());
        verify(shiftRosterRepository).save(any(ShiftRoster.class));
    }

    @Test
    void create_shouldCreateWithRotationPattern() {
        var request = new ShiftRosterRequest(1L, 1L, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 31), 1L);
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");
        var pattern = createRotationPattern(1L, "Weekly");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(rotationPatternRepository.findById(1L)).thenReturn(Optional.of(pattern));
        when(shiftRosterRepository.findOverlapping(eq(1L), any(), any())).thenReturn(List.of());
        when(shiftRosterRepository.save(any(ShiftRoster.class))).thenAnswer(invocation -> {
            ShiftRoster r = invocation.getArgument(0);
            r.setId(1L);
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(LocalDateTime.now());
            return r;
        });

        var result = shiftRosterService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.rotationPatternId());
        assertEquals("Weekly", result.rotationPatternName());
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shiftRosterService.create(request));
        verify(shiftRosterRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenShiftNotFound() {
        var request = createRequest();
        var employee = createEmployee(1L, "EMP-001");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shiftRosterService.create(request));
        verify(shiftRosterRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenOverlappingRosterExists() {
        var request = createRequest();
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");
        var existingRoster = createRoster(2L, employee, shift);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRosterRepository.findOverlapping(eq(1L), any(), any())).thenReturn(List.of(existingRoster));

        var ex = assertThrows(BadRequestException.class,
                () -> shiftRosterService.create(request));
        assertTrue(ex.getMessage().contains("overlapping"));
        verify(shiftRosterRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenEndDateBeforeEffectiveDate() {
        var request = new ShiftRosterRequest(1L, 1L, LocalDate.of(2024, 12, 31), LocalDate.of(2024, 7, 1), null);
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));

        var ex = assertThrows(BadRequestException.class,
                () -> shiftRosterService.create(request));
        assertTrue(ex.getMessage().contains("End date"));
        verify(shiftRosterRepository, never()).save(any());
    }

    @Test
    void bulkCreate_shouldCreateRostersForMultipleEmployees() {
        var request = new BulkShiftRosterRequest(
                List.of(1L, 2L), 1L, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 31), null);
        var employee1 = createEmployee(1L, "EMP-001");
        var employee2 = createEmployee(2L, "EMP-002");
        var shift = createShift(1L, "Morning");

        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee2));
        when(shiftRosterRepository.findOverlapping(eq(1L), any(), any())).thenReturn(List.of());
        when(shiftRosterRepository.findOverlapping(eq(2L), any(), any())).thenReturn(List.of());
        when(shiftRosterRepository.save(any(ShiftRoster.class))).thenAnswer(invocation -> {
            ShiftRoster r = invocation.getArgument(0);
            r.setId(r.getEmployee().getId());
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(LocalDateTime.now());
            return r;
        });

        var result = shiftRosterService.bulkCreate(request);

        assertEquals(2, result.size());
    }

    @Test
    void bulkCreate_shouldSkipEmployeesWithConflicts() {
        var request = new BulkShiftRosterRequest(
                List.of(1L, 2L), 1L, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 31), null);
        var employee1 = createEmployee(1L, "EMP-001");
        var employee2 = createEmployee(2L, "EMP-002");
        var shift = createShift(1L, "Morning");
        var existingRoster = createRoster(3L, employee1, shift);

        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee2));
        when(shiftRosterRepository.findOverlapping(eq(1L), any(), any())).thenReturn(List.of(existingRoster));
        when(shiftRosterRepository.findOverlapping(eq(2L), any(), any())).thenReturn(List.of());
        when(shiftRosterRepository.save(any(ShiftRoster.class))).thenAnswer(invocation -> {
            ShiftRoster r = invocation.getArgument(0);
            r.setId(2L);
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(LocalDateTime.now());
            return r;
        });

        var result = shiftRosterService.bulkCreate(request);

        assertEquals(1, result.size());
        assertEquals("EMP-002", result.get(0).employeeCode());
    }

    @Test
    void update_shouldUpdateAndReturnRoster() {
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");
        var roster = createRoster(1L, employee, shift);
        var newShift = createShift(2L, "Evening");
        var request = new ShiftRosterRequest(1L, 2L, LocalDate.of(2024, 8, 1), LocalDate.of(2024, 12, 31), null);

        when(shiftRosterRepository.findById(1L)).thenReturn(Optional.of(roster));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(2L)).thenReturn(Optional.of(newShift));
        when(shiftRosterRepository.findOverlappingExcluding(eq(1L), any(), any(), eq(1L))).thenReturn(List.of());
        when(shiftRosterRepository.save(any(ShiftRoster.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = shiftRosterService.update(1L, request);

        assertNotNull(result);
        assertEquals(2L, result.shiftId());
        assertEquals("Evening", result.shiftName());
        assertEquals(LocalDate.of(2024, 8, 1), result.effectiveDate());
        verify(shiftRosterRepository).save(any(ShiftRoster.class));
    }

    @Test
    void update_shouldThrowWhenRosterNotFound() {
        var request = createRequest();
        when(shiftRosterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shiftRosterService.update(999L, request));
        verify(shiftRosterRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenOverlappingExists() {
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");
        var roster = createRoster(1L, employee, shift);
        var existingRoster = createRoster(2L, employee, shift);
        var request = createRequest();

        when(shiftRosterRepository.findById(1L)).thenReturn(Optional.of(roster));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRosterRepository.findOverlappingExcluding(eq(1L), any(), any(), eq(1L)))
                .thenReturn(List.of(existingRoster));

        var ex = assertThrows(BadRequestException.class,
                () -> shiftRosterService.update(1L, request));
        assertTrue(ex.getMessage().contains("overlapping"));
        verify(shiftRosterRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteRoster() {
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");
        var roster = createRoster(1L, employee, shift);
        when(shiftRosterRepository.findById(1L)).thenReturn(Optional.of(roster));

        shiftRosterService.delete(1L);

        verify(shiftRosterRepository).delete(roster);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(shiftRosterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shiftRosterService.delete(999L));
        verify(shiftRosterRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapResponseWithNullRotationPattern() {
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");
        var roster = createRoster(1L, employee, shift);
        when(shiftRosterRepository.findById(1L)).thenReturn(Optional.of(roster));

        var result = shiftRosterService.getById(1L);

        assertNull(result.rotationPatternId());
        assertNull(result.rotationPatternName());
    }

    @Test
    void create_shouldAllowNullEndDate() {
        var request = new ShiftRosterRequest(1L, 1L, LocalDate.of(2024, 7, 1), null, null);
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRosterRepository.findOverlapping(eq(1L), any(), any())).thenReturn(List.of());
        when(shiftRosterRepository.save(any(ShiftRoster.class))).thenAnswer(invocation -> {
            ShiftRoster r = invocation.getArgument(0);
            r.setId(1L);
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(LocalDateTime.now());
            return r;
        });

        var result = shiftRosterService.create(request);

        assertNotNull(result);
        assertNull(result.endDate());
    }

    @Test
    void getByDateRange_shouldReturnRosters() {
        var employee = createEmployee(1L, "EMP-001");
        var shift = createShift(1L, "Morning");
        var rosters = List.of(createRoster(1L, employee, shift));
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 6, 30);
        when(shiftRosterRepository.findByDateRange(startDate, endDate)).thenReturn(rosters);

        var result = shiftRosterService.getByDateRange(startDate, endDate);

        assertEquals(1, result.size());
        assertEquals("EMP-001", result.get(0).employeeCode());
        verify(shiftRosterRepository).findByDateRange(startDate, endDate);
    }

    @Test
    void getByDateRange_shouldReturnEmptyList() {
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 6, 30);
        when(shiftRosterRepository.findByDateRange(startDate, endDate)).thenReturn(List.of());

        var result = shiftRosterService.getByDateRange(startDate, endDate);

        assertTrue(result.isEmpty());
    }
}
