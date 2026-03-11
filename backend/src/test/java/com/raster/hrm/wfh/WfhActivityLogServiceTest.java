package com.raster.hrm.wfh;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.wfh.dto.WfhCheckInRequest;
import com.raster.hrm.wfh.entity.WfhActivityLog;
import com.raster.hrm.wfh.entity.WfhRequest;
import com.raster.hrm.wfh.entity.WfhStatus;
import com.raster.hrm.wfh.repository.WfhActivityLogRepository;
import com.raster.hrm.wfh.repository.WfhRequestRepository;
import com.raster.hrm.wfh.service.WfhActivityLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WfhActivityLogServiceTest {

    @Mock
    private WfhActivityLogRepository wfhActivityLogRepository;

    @Mock
    private WfhRequestRepository wfhRequestRepository;

    @InjectMocks
    private WfhActivityLogService wfhActivityLogService;

    private Employee createEmployee() {
        var employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        return employee;
    }

    private WfhRequest createWfhRequest(Long id, Employee employee, WfhStatus status) {
        var request = new WfhRequest();
        request.setId(id);
        request.setEmployee(employee);
        request.setRequestDate(LocalDate.now());
        request.setReason("Working from home");
        request.setStatus(status);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        return request;
    }

    private WfhActivityLog createActivityLog(Long id, WfhRequest wfhRequest) {
        var log = new WfhActivityLog();
        log.setId(id);
        log.setWfhRequest(wfhRequest);
        log.setCheckInTime(LocalDateTime.now().minusHours(2));
        log.setIpAddress("192.168.1.1");
        log.setLocation("Home Office");
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        return log;
    }

    @Test
    void checkIn_shouldCreateActivityLog() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.APPROVED);
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));
        when(wfhActivityLogRepository.findActiveByRequestId(1L)).thenReturn(Optional.empty());
        when(wfhActivityLogRepository.save(any(WfhActivityLog.class))).thenAnswer(invocation -> {
            var saved = invocation.getArgument(0, WfhActivityLog.class);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        var checkInRequest = new WfhCheckInRequest(1L, "192.168.1.1", "Home Office");
        var result = wfhActivityLogService.checkIn(checkInRequest);

        assertNotNull(result);
        assertEquals(1L, result.wfhRequestId());
        assertEquals("192.168.1.1", result.ipAddress());
        assertEquals("Home Office", result.location());
        assertNotNull(result.checkInTime());
        assertNull(result.checkOutTime());
        verify(wfhActivityLogRepository).save(any(WfhActivityLog.class));
    }

    @Test
    void checkIn_shouldThrowWhenRequestNotFound() {
        when(wfhRequestRepository.findById(999L)).thenReturn(Optional.empty());

        var checkInRequest = new WfhCheckInRequest(999L, null, null);

        assertThrows(ResourceNotFoundException.class,
                () -> wfhActivityLogService.checkIn(checkInRequest));
    }

    @Test
    void checkIn_shouldThrowWhenRequestNotApproved() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.PENDING);
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));

        var checkInRequest = new WfhCheckInRequest(1L, null, null);

        assertThrows(BadRequestException.class,
                () -> wfhActivityLogService.checkIn(checkInRequest));
    }

    @Test
    void checkIn_shouldThrowWhenNotRequestDate() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.APPROVED);
        wfhRequest.setRequestDate(LocalDate.now().plusDays(1));
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));

        var checkInRequest = new WfhCheckInRequest(1L, null, null);

        assertThrows(BadRequestException.class,
                () -> wfhActivityLogService.checkIn(checkInRequest));
    }

    @Test
    void checkIn_shouldThrowWhenActiveSessionExists() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.APPROVED);
        var activeLog = createActivityLog(1L, wfhRequest);
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));
        when(wfhActivityLogRepository.findActiveByRequestId(1L)).thenReturn(Optional.of(activeLog));

        var checkInRequest = new WfhCheckInRequest(1L, null, null);

        assertThrows(BadRequestException.class,
                () -> wfhActivityLogService.checkIn(checkInRequest));
    }

    @Test
    void checkOut_shouldSetCheckOutTime() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.APPROVED);
        var activityLog = createActivityLog(1L, wfhRequest);
        when(wfhActivityLogRepository.findById(1L)).thenReturn(Optional.of(activityLog));
        when(wfhActivityLogRepository.save(any(WfhActivityLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = wfhActivityLogService.checkOut(1L);

        assertNotNull(result);
        assertNotNull(result.checkOutTime());
        verify(wfhActivityLogRepository).save(any(WfhActivityLog.class));
    }

    @Test
    void checkOut_shouldThrowWhenNotFound() {
        when(wfhActivityLogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> wfhActivityLogService.checkOut(999L));
    }

    @Test
    void checkOut_shouldThrowWhenAlreadyCheckedOut() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.APPROVED);
        var activityLog = createActivityLog(1L, wfhRequest);
        activityLog.setCheckOutTime(LocalDateTime.now());
        when(wfhActivityLogRepository.findById(1L)).thenReturn(Optional.of(activityLog));

        assertThrows(BadRequestException.class,
                () -> wfhActivityLogService.checkOut(1L));
    }

    @Test
    void getByRequestId_shouldReturnLogs() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.APPROVED);
        var logs = List.of(createActivityLog(1L, wfhRequest), createActivityLog(2L, wfhRequest));
        when(wfhActivityLogRepository.findByWfhRequestId(1L)).thenReturn(logs);

        var result = wfhActivityLogService.getByRequestId(1L);

        assertEquals(2, result.size());
        assertEquals("EMP001", result.get(0).employeeCode());
    }

    @Test
    void getActiveSession_shouldReturnActiveLog() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.APPROVED);
        var activeLog = createActivityLog(1L, wfhRequest);
        when(wfhActivityLogRepository.findActiveByRequestId(1L)).thenReturn(Optional.of(activeLog));

        var result = wfhActivityLogService.getActiveSession(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().id());
    }

    @Test
    void getActiveSession_shouldReturnEmptyWhenNoActiveSession() {
        when(wfhActivityLogRepository.findActiveByRequestId(1L)).thenReturn(Optional.empty());

        var result = wfhActivityLogService.getActiveSession(1L);

        assertTrue(result.isEmpty());
    }
}
