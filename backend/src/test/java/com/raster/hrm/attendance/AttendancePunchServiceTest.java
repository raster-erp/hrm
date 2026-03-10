package com.raster.hrm.attendance;

import com.raster.hrm.attendance.dto.AttendancePunchRequest;
import com.raster.hrm.attendance.dto.PunchSyncRequest;
import com.raster.hrm.attendance.entity.AttendancePunch;
import com.raster.hrm.attendance.entity.PunchDirection;
import com.raster.hrm.attendance.repository.AttendancePunchRepository;
import com.raster.hrm.attendance.service.AttendancePunchService;
import com.raster.hrm.device.entity.Device;
import com.raster.hrm.device.entity.DeviceStatus;
import com.raster.hrm.device.entity.DeviceType;
import com.raster.hrm.device.repository.DeviceRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.ResourceNotFoundException;
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
class AttendancePunchServiceTest {

    @Mock
    private AttendancePunchRepository punchRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private AttendancePunchService punchService;

    private Employee createEmployee(Long id, String code, String firstName, String lastName) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "@test.com");
        return employee;
    }

    private Device createDevice(Long id, String serialNumber, String name) {
        var device = new Device();
        device.setId(id);
        device.setSerialNumber(serialNumber);
        device.setName(name);
        device.setType(DeviceType.BIOMETRIC);
        device.setStatus(DeviceStatus.ACTIVE);
        device.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        device.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return device;
    }

    private AttendancePunch createPunch(Long id, Employee employee, Device device, LocalDateTime punchTime, PunchDirection direction) {
        var punch = new AttendancePunch();
        punch.setId(id);
        punch.setEmployee(employee);
        punch.setDevice(device);
        punch.setPunchTime(punchTime);
        punch.setDirection(direction);
        punch.setNormalized(true);
        punch.setSource("DEVICE");
        punch.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        punch.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return punch;
    }

    @Test
    void getAll_shouldReturnPageOfPunches() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punches = List.of(
                createPunch(1L, employee, device, LocalDateTime.of(2024, 6, 1, 9, 0), PunchDirection.IN),
                createPunch(2L, employee, device, LocalDateTime.of(2024, 6, 1, 18, 0), PunchDirection.OUT)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(punches, pageable, 2);
        when(punchRepository.findAll(pageable)).thenReturn(page);

        var result = punchService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("IN", result.getContent().get(0).direction());
        assertEquals("OUT", result.getContent().get(1).direction());
        verify(punchRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<AttendancePunch>(List.of(), pageable, 0);
        when(punchRepository.findAll(pageable)).thenReturn(page);

        var result = punchService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnPunch() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punch = createPunch(1L, employee, device, LocalDateTime.of(2024, 6, 1, 9, 0), PunchDirection.IN);
        when(punchRepository.findById(1L)).thenReturn(Optional.of(punch));

        var result = punchService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("BIO-001", result.deviceSerialNumber());
        assertEquals("IN", result.direction());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(punchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> punchService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnPunches() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punches = List.of(
                createPunch(1L, employee, device, LocalDateTime.of(2024, 6, 1, 9, 0), PunchDirection.IN)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(punches, pageable, 1);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(punchRepository.findByEmployeeId(1L, pageable)).thenReturn(page);

        var result = punchService.getByEmployeeId(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("EMP001", result.getContent().get(0).employeeCode());
    }

    @Test
    void getByEmployeeId_shouldThrowWhenEmployeeNotFound() {
        var pageable = PageRequest.of(0, 20);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> punchService.getByEmployeeId(999L, pageable));
        verify(punchRepository, never()).findByEmployeeId(any(), any());
    }

    @Test
    void getByDateRange_shouldReturnPunches() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punches = List.of(
                createPunch(1L, employee, device, LocalDateTime.of(2024, 6, 1, 9, 0), PunchDirection.IN)
        );
        var pageable = PageRequest.of(0, 20);
        var from = LocalDate.of(2024, 6, 1);
        var to = LocalDate.of(2024, 6, 30);
        var fromDateTime = from.atStartOfDay();
        var toDateTime = to.atTime(LocalTime.MAX);
        var page = new PageImpl<>(punches, pageable, 1);
        when(punchRepository.findByPunchTimeBetween(fromDateTime, toDateTime, pageable)).thenReturn(page);

        var result = punchService.getByDateRange(from, to, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getByEmployeeAndDateRange_shouldReturnPunches() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punches = List.of(
                createPunch(1L, employee, device, LocalDateTime.of(2024, 6, 1, 9, 0), PunchDirection.IN)
        );
        var pageable = PageRequest.of(0, 20);
        var from = LocalDate.of(2024, 6, 1);
        var to = LocalDate.of(2024, 6, 30);
        var fromDateTime = from.atStartOfDay();
        var toDateTime = to.atTime(LocalTime.MAX);
        var page = new PageImpl<>(punches, pageable, 1);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(punchRepository.findByEmployeeIdAndPunchTimeBetween(1L, fromDateTime, toDateTime, pageable))
                .thenReturn(page);

        var result = punchService.getByEmployeeAndDateRange(1L, from, to, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getByEmployeeAndDateRange_shouldThrowWhenEmployeeNotFound() {
        var pageable = PageRequest.of(0, 20);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> punchService.getByEmployeeAndDateRange(999L, LocalDate.now(), LocalDate.now(), pageable));
    }

    @Test
    void create_shouldCreateAndReturnPunch() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punchTime = LocalDateTime.of(2024, 6, 1, 9, 0);
        var request = new AttendancePunchRequest(1L, 1L, punchTime, "IN", "raw-data-123");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(punchRepository.existsDuplicate(1L, 1L, punchTime, PunchDirection.IN)).thenReturn(false);
        when(punchRepository.save(any(AttendancePunch.class))).thenAnswer(invocation -> {
            AttendancePunch p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        var result = punchService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("BIO-001", result.deviceSerialNumber());
        assertEquals("IN", result.direction());
        assertEquals(punchTime, result.punchTime());
        assertEquals("raw-data-123", result.rawData());
        assertTrue(result.normalized());
        assertEquals("API", result.source());
        verify(punchRepository).save(any(AttendancePunch.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var request = new AttendancePunchRequest(999L, 1L, LocalDateTime.now(), "IN", null);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> punchService.create(request));
        verify(punchRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenDeviceNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new AttendancePunchRequest(1L, 999L, LocalDateTime.now(), "IN", null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> punchService.create(request));
        verify(punchRepository, never()).save(any());
    }

    @Test
    void create_shouldReturnExistingWhenDuplicate() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punchTime = LocalDateTime.of(2024, 6, 1, 9, 0);
        var request = new AttendancePunchRequest(1L, 1L, punchTime, "IN", null);
        var existingPunch = createPunch(5L, employee, device, punchTime, PunchDirection.IN);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(punchRepository.existsDuplicate(1L, 1L, punchTime, PunchDirection.IN)).thenReturn(true);
        when(punchRepository.findByEmployeeIdAndPunchTimeBetween(eq(1L), eq(punchTime), eq(punchTime), any()))
                .thenReturn(new PageImpl<>(List.of(existingPunch)));

        var result = punchService.create(request);

        assertEquals(5L, result.id());
        verify(punchRepository, never()).save(any());
    }

    @Test
    void syncPunches_shouldAcceptNonDuplicatePunches() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punchTime1 = "2024-06-01T09:00:00";
        var punchTime2 = "2024-06-01T18:00:00";
        var punches = List.of(
                new PunchSyncRequest.PunchData(1L, punchTime1, "IN", "raw-1"),
                new PunchSyncRequest.PunchData(1L, punchTime2, "OUT", "raw-2")
        );
        var request = new PunchSyncRequest(1L, punches);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(punchRepository.existsDuplicate(any(), any(), any(), any())).thenReturn(false);
        when(punchRepository.save(any(AttendancePunch.class))).thenAnswer(invocation -> {
            AttendancePunch p = invocation.getArgument(0);
            p.setId((long) (Math.random() * 1000));
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = punchService.syncPunches(request);

        assertEquals(2, result.totalReceived());
        assertEquals(2, result.accepted());
        assertEquals(0, result.duplicatesSkipped());
        assertEquals(2, result.acceptedPunchIds().size());
    }

    @Test
    void syncPunches_shouldSkipDuplicates() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punchTime1 = "2024-06-01T09:00:00";
        var punchTime2 = "2024-06-01T18:00:00";
        var punches = List.of(
                new PunchSyncRequest.PunchData(1L, punchTime1, "IN", "raw-1"),
                new PunchSyncRequest.PunchData(1L, punchTime2, "OUT", "raw-2")
        );
        var request = new PunchSyncRequest(1L, punches);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(punchRepository.existsDuplicate(eq(1L), eq(1L), eq(LocalDateTime.of(2024, 6, 1, 9, 0)), eq(PunchDirection.IN)))
                .thenReturn(true);
        when(punchRepository.existsDuplicate(eq(1L), eq(1L), eq(LocalDateTime.of(2024, 6, 1, 18, 0)), eq(PunchDirection.OUT)))
                .thenReturn(false);
        when(punchRepository.save(any(AttendancePunch.class))).thenAnswer(invocation -> {
            AttendancePunch p = invocation.getArgument(0);
            p.setId(10L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = punchService.syncPunches(request);

        assertEquals(2, result.totalReceived());
        assertEquals(1, result.accepted());
        assertEquals(1, result.duplicatesSkipped());
    }

    @Test
    void syncPunches_shouldThrowWhenDeviceNotFound() {
        var punches = List.of(
                new PunchSyncRequest.PunchData(1L, "2024-06-01T09:00:00", "IN", null)
        );
        var request = new PunchSyncRequest(999L, punches);
        when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> punchService.syncPunches(request));
    }

    @Test
    void syncPunches_shouldSkipUnknownEmployees() {
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punches = List.of(
                new PunchSyncRequest.PunchData(999L, "2024-06-01T09:00:00", "IN", null)
        );
        var request = new PunchSyncRequest(1L, punches);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = punchService.syncPunches(request);

        assertEquals(1, result.totalReceived());
        assertEquals(0, result.accepted());
        assertEquals(0, result.duplicatesSkipped());
    }

    @Test
    void syncPunches_shouldUpdateDeviceLastSyncAt() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punches = List.of(
                new PunchSyncRequest.PunchData(1L, "2024-06-01T09:00:00", "IN", null)
        );
        var request = new PunchSyncRequest(1L, punches);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(punchRepository.existsDuplicate(any(), any(), any(), any())).thenReturn(false);
        when(punchRepository.save(any(AttendancePunch.class))).thenAnswer(invocation -> {
            AttendancePunch p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        punchService.syncPunches(request);

        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void delete_shouldDeletePunch() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punch = createPunch(1L, employee, device, LocalDateTime.of(2024, 6, 1, 9, 0), PunchDirection.IN);
        when(punchRepository.findById(1L)).thenReturn(Optional.of(punch));

        punchService.delete(1L);

        verify(punchRepository).delete(punch);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(punchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> punchService.delete(999L));
        verify(punchRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var device = createDevice(1L, "BIO-001", "Main Entrance");
        var punchTime = LocalDateTime.of(2024, 6, 1, 9, 0);
        var punch = createPunch(1L, employee, device, punchTime, PunchDirection.IN);
        punch.setRawData("raw-data");
        when(punchRepository.findById(1L)).thenReturn(Optional.of(punch));

        var result = punchService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals(1L, result.deviceId());
        assertEquals("BIO-001", result.deviceSerialNumber());
        assertEquals("Main Entrance", result.deviceName());
        assertEquals(punchTime, result.punchTime());
        assertEquals("IN", result.direction());
        assertEquals("raw-data", result.rawData());
        assertTrue(result.normalized());
        assertEquals("DEVICE", result.source());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.updatedAt());
    }
}
