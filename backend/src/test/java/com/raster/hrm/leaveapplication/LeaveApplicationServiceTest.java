package com.raster.hrm.leaveapplication;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leaveapplication.dto.LeaveApplicationRequest;
import com.raster.hrm.leaveapplication.dto.LeaveApprovalRequest;
import com.raster.hrm.leaveapplication.entity.LeaveApplication;
import com.raster.hrm.leaveapplication.entity.LeaveApplicationStatus;
import com.raster.hrm.leaveapplication.entity.LeaveApprovalLog;
import com.raster.hrm.leaveapplication.repository.LeaveApplicationRepository;
import com.raster.hrm.leaveapplication.repository.LeaveApprovalLogRepository;
import com.raster.hrm.leaveapplication.service.LeaveApplicationNotificationService;
import com.raster.hrm.leaveapplication.service.LeaveApplicationService;
import com.raster.hrm.leavebalance.service.LeaveBalanceService;
import com.raster.hrm.leavetype.entity.LeaveType;
import com.raster.hrm.leavetype.repository.LeaveTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class LeaveApplicationServiceTest {

    @Mock
    private LeaveApplicationRepository leaveApplicationRepository;

    @Mock
    private LeaveApprovalLogRepository leaveApprovalLogRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private LeaveApplicationNotificationService notificationService;

    @Mock
    private LeaveBalanceService leaveBalanceService;

    @InjectMocks
    private LeaveApplicationService leaveApplicationService;

    private Employee createEmployee() {
        var employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        return employee;
    }

    private LeaveType createLeaveType() {
        var leaveType = new LeaveType();
        leaveType.setId(1L);
        leaveType.setName("Casual Leave");
        leaveType.setCode("CL");
        return leaveType;
    }

    private LeaveApplication createLeaveApplication(Long id, Employee employee, LeaveType leaveType) {
        var application = new LeaveApplication();
        application.setId(id);
        application.setEmployee(employee);
        application.setLeaveType(leaveType);
        application.setFromDate(LocalDate.of(2024, 3, 1));
        application.setToDate(LocalDate.of(2024, 3, 3));
        application.setNumberOfDays(new BigDecimal("3.00"));
        application.setReason("Personal work");
        application.setStatus(LeaveApplicationStatus.PENDING);
        application.setApprovalLevel(0);
        application.setRemarks("Test application");
        application.setCreatedAt(LocalDateTime.of(2024, 2, 28, 10, 0));
        application.setUpdatedAt(LocalDateTime.of(2024, 2, 28, 10, 0));
        return application;
    }

    private LeaveApplicationRequest createRequestDto() {
        return new LeaveApplicationRequest(
                1L,
                1L,
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 3),
                new BigDecimal("3.00"),
                "Personal work",
                "Test application"
        );
    }

    @Test
    void getAll_shouldReturnPage() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var applications = List.of(
                createLeaveApplication(1L, employee, leaveType),
                createLeaveApplication(2L, employee, leaveType)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(applications, pageable, 2);
        when(leaveApplicationRepository.findAll(pageable)).thenReturn(page);

        var result = leaveApplicationService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).employeeName());
        verify(leaveApplicationRepository).findAll(pageable);
    }

    @Test
    void getById_shouldReturnResponse() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var application = createLeaveApplication(1L, employee, leaveType);
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(application));

        var result = leaveApplicationService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("Casual Leave", result.leaveTypeName());
        assertEquals("PENDING", result.status());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(leaveApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveApplicationService.getById(999L));
    }

    @Test
    void getByEmployee_shouldReturnPage() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var applications = List.of(createLeaveApplication(1L, employee, leaveType));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(applications, pageable, 1);
        when(leaveApplicationRepository.findByEmployeeId(1L, pageable)).thenReturn(page);

        var result = leaveApplicationService.getByEmployee(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).employeeId());
    }

    @Test
    void getByStatus_shouldReturnPage() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var applications = List.of(createLeaveApplication(1L, employee, leaveType));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(applications, pageable, 1);
        when(leaveApplicationRepository.findByStatus(LeaveApplicationStatus.PENDING, pageable)).thenReturn(page);

        var result = leaveApplicationService.getByStatus(LeaveApplicationStatus.PENDING, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("PENDING", result.getContent().get(0).status());
    }

    @Test
    void getByLeaveType_shouldReturnPage() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var applications = List.of(createLeaveApplication(1L, employee, leaveType));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(applications, pageable, 1);
        when(leaveApplicationRepository.findByLeaveTypeId(1L, pageable)).thenReturn(page);

        var result = leaveApplicationService.getByLeaveType(1L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getByDateRange_shouldReturnPage() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var applications = List.of(createLeaveApplication(1L, employee, leaveType));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(applications, pageable, 1);
        var fromDate = LocalDate.of(2024, 3, 1);
        var toDate = LocalDate.of(2024, 3, 31);
        when(leaveApplicationRepository.findByFromDateGreaterThanEqualAndToDateLessThanEqual(fromDate, toDate, pageable))
                .thenReturn(page);

        var result = leaveApplicationService.getByDateRange(fromDate, toDate, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getByEmployeeAndDateRange_shouldReturnPage() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var applications = List.of(createLeaveApplication(1L, employee, leaveType));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(applications, pageable, 1);
        var fromDate = LocalDate.of(2024, 3, 1);
        var toDate = LocalDate.of(2024, 3, 31);
        when(leaveApplicationRepository.findByEmployeeIdAndFromDateGreaterThanEqualAndToDateLessThanEqual(
                1L, fromDate, toDate, pageable)).thenReturn(page);

        var result = leaveApplicationService.getByEmployeeAndDateRange(1L, fromDate, toDate, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void create_shouldCreateApplication() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var requestDto = createRequestDto();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenAnswer(invocation -> {
            LeaveApplication app = invocation.getArgument(0);
            app.setId(1L);
            app.setCreatedAt(LocalDateTime.now());
            app.setUpdatedAt(LocalDateTime.now());
            return app;
        });

        var result = leaveApplicationService.create(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("John Doe", result.employeeName());
        assertEquals("Casual Leave", result.leaveTypeName());
        assertEquals("PENDING", result.status());
        verify(leaveApplicationRepository).save(any(LeaveApplication.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var requestDto = createRequestDto();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveApplicationService.create(requestDto));
        verify(leaveApplicationRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenLeaveTypeNotFound() {
        var employee = createEmployee();
        var requestDto = createRequestDto();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveApplicationService.create(requestDto));
        verify(leaveApplicationRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenFromDateAfterToDate() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var requestDto = new LeaveApplicationRequest(
                1L, 1L,
                LocalDate.of(2024, 3, 5),
                LocalDate.of(2024, 3, 1),
                new BigDecimal("3.00"),
                "Personal work",
                null
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        assertThrows(BadRequestException.class,
                () -> leaveApplicationService.create(requestDto));
        verify(leaveApplicationRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateApplication() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var application = createLeaveApplication(1L, employee, leaveType);
        var requestDto = createRequestDto();
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leaveApplicationService.update(1L, requestDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(leaveApplicationRepository).save(any(LeaveApplication.class));
    }

    @Test
    void update_shouldThrowWhenNotPending() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var application = createLeaveApplication(1L, employee, leaveType);
        application.setStatus(LeaveApplicationStatus.APPROVED);
        var requestDto = createRequestDto();
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(application));

        var ex = assertThrows(BadRequestException.class,
                () -> leaveApplicationService.update(1L, requestDto));
        assertTrue(ex.getMessage().contains("PENDING"));
        verify(leaveApplicationRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var requestDto = createRequestDto();
        when(leaveApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveApplicationService.update(999L, requestDto));
        verify(leaveApplicationRepository, never()).save(any());
    }

    @Test
    void approve_shouldApproveApplication() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var application = createLeaveApplication(1L, employee, leaveType);
        var approvalRequest = new LeaveApprovalRequest(LeaveApplicationStatus.APPROVED, "admin", "Approved");
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(leaveApprovalLogRepository.save(any(LeaveApprovalLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leaveApplicationService.approve(1L, approvalRequest);

        assertEquals("APPROVED", result.status());
        assertEquals("admin", result.approvedBy());
        verify(leaveApplicationRepository).save(any(LeaveApplication.class));
        verify(leaveApprovalLogRepository).save(any(LeaveApprovalLog.class));
    }

    @Test
    void approve_shouldRejectApplication() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var application = createLeaveApplication(1L, employee, leaveType);
        var approvalRequest = new LeaveApprovalRequest(LeaveApplicationStatus.REJECTED, "admin", "Insufficient leave balance");
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(leaveApprovalLogRepository.save(any(LeaveApprovalLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leaveApplicationService.approve(1L, approvalRequest);

        assertEquals("REJECTED", result.status());
        assertEquals("admin", result.approvedBy());
        verify(leaveApprovalLogRepository).save(any(LeaveApprovalLog.class));
    }

    @Test
    void approve_shouldThrowWhenNotPending() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var application = createLeaveApplication(1L, employee, leaveType);
        application.setStatus(LeaveApplicationStatus.APPROVED);
        var approvalRequest = new LeaveApprovalRequest(LeaveApplicationStatus.REJECTED, "admin", null);
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThrows(BadRequestException.class,
                () -> leaveApplicationService.approve(1L, approvalRequest));
        verify(leaveApplicationRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenNotFound() {
        var approvalRequest = new LeaveApprovalRequest(LeaveApplicationStatus.APPROVED, "admin", null);
        when(leaveApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveApplicationService.approve(999L, approvalRequest));
        verify(leaveApplicationRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenInvalidStatus() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var application = createLeaveApplication(1L, employee, leaveType);
        var approvalRequest = new LeaveApprovalRequest(LeaveApplicationStatus.CANCELLED, "admin", null);
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThrows(BadRequestException.class,
                () -> leaveApplicationService.approve(1L, approvalRequest));
        verify(leaveApplicationRepository, never()).save(any());
    }

    @Test
    void cancel_shouldCancelPendingApplication() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var application = createLeaveApplication(1L, employee, leaveType);
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leaveApplicationService.cancel(1L);

        assertEquals("CANCELLED", result.status());
        verify(leaveApplicationRepository).save(any(LeaveApplication.class));
    }

    @Test
    void cancel_shouldCancelApprovedApplication() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var application = createLeaveApplication(1L, employee, leaveType);
        application.setStatus(LeaveApplicationStatus.APPROVED);
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(leaveApplicationRepository.save(any(LeaveApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = leaveApplicationService.cancel(1L);

        assertEquals("CANCELLED", result.status());
    }

    @Test
    void cancel_shouldThrowWhenRejected() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var application = createLeaveApplication(1L, employee, leaveType);
        application.setStatus(LeaveApplicationStatus.REJECTED);
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThrows(BadRequestException.class,
                () -> leaveApplicationService.cancel(1L));
        verify(leaveApplicationRepository, never()).save(any());
    }

    @Test
    void cancel_shouldThrowWhenNotFound() {
        when(leaveApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveApplicationService.cancel(999L));
    }

    @Test
    void delete_shouldDeleteApplication() {
        var employee = createEmployee();
        var leaveType = createLeaveType();
        var application = createLeaveApplication(1L, employee, leaveType);
        when(leaveApplicationRepository.findById(1L)).thenReturn(Optional.of(application));

        leaveApplicationService.delete(1L);

        verify(leaveApplicationRepository).delete(application);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(leaveApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> leaveApplicationService.delete(999L));
        verify(leaveApplicationRepository, never()).delete(any());
    }
}
