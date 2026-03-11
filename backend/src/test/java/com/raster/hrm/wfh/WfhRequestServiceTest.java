package com.raster.hrm.wfh;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.wfh.dto.WfhApprovalRequest;
import com.raster.hrm.wfh.dto.WfhRequestCreateRequest;
import com.raster.hrm.wfh.entity.WfhRequest;
import com.raster.hrm.wfh.entity.WfhStatus;
import com.raster.hrm.wfh.repository.WfhActivityLogRepository;
import com.raster.hrm.wfh.repository.WfhRequestRepository;
import com.raster.hrm.wfh.service.WfhRequestService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WfhRequestServiceTest {

    @Mock
    private WfhRequestRepository wfhRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private WfhActivityLogRepository wfhActivityLogRepository;

    @InjectMocks
    private WfhRequestService wfhRequestService;

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
        request.setRequestDate(LocalDate.of(2024, 6, 15));
        request.setReason("Working from home");
        request.setStatus(status);
        request.setRemarks("Test remarks");
        request.setCreatedAt(LocalDateTime.of(2024, 6, 14, 10, 0));
        request.setUpdatedAt(LocalDateTime.of(2024, 6, 14, 10, 0));
        return request;
    }

    @Test
    void getAll_shouldReturnPageOfRequests() {
        var employee = createEmployee();
        var requests = List.of(
                createWfhRequest(1L, employee, WfhStatus.PENDING),
                createWfhRequest(2L, employee, WfhStatus.APPROVED)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(requests, pageable, 2);
        when(wfhRequestRepository.findAll(pageable)).thenReturn(page);

        var result = wfhRequestService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).employeeName());
        verify(wfhRequestRepository).findAll(pageable);
    }

    @Test
    void getById_shouldReturnRequest() {
        var employee = createEmployee();
        var request = createWfhRequest(1L, employee, WfhStatus.PENDING);
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        var result = wfhRequestService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("PENDING", result.status());
        assertEquals("Working from home", result.reason());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(wfhRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> wfhRequestService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnPage() {
        var employee = createEmployee();
        var requests = List.of(createWfhRequest(1L, employee, WfhStatus.PENDING));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(requests, pageable, 1);
        when(wfhRequestRepository.findByEmployeeId(1L, pageable)).thenReturn(page);

        var result = wfhRequestService.getByEmployeeId(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).employeeId());
    }

    @Test
    void getByStatus_shouldReturnPage() {
        var employee = createEmployee();
        var requests = List.of(createWfhRequest(1L, employee, WfhStatus.APPROVED));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(requests, pageable, 1);
        when(wfhRequestRepository.findByStatus(WfhStatus.APPROVED, pageable)).thenReturn(page);

        var result = wfhRequestService.getByStatus(WfhStatus.APPROVED, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("APPROVED", result.getContent().get(0).status());
    }

    @Test
    void getByDateRange_shouldReturnPage() {
        var employee = createEmployee();
        var requests = List.of(createWfhRequest(1L, employee, WfhStatus.PENDING));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(requests, pageable, 1);
        var startDate = LocalDate.of(2024, 6, 1);
        var endDate = LocalDate.of(2024, 6, 30);
        when(wfhRequestRepository.findByDateRange(startDate, endDate, pageable)).thenReturn(page);

        var result = wfhRequestService.getByDateRange(startDate, endDate, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void create_shouldCreateWfhRequest() {
        var employee = createEmployee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(wfhRequestRepository.save(any(WfhRequest.class))).thenAnswer(invocation -> {
            var saved = invocation.getArgument(0, WfhRequest.class);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        var createRequest = new WfhRequestCreateRequest(1L, LocalDate.of(2024, 6, 15), "WFH needed", "Test");
        var result = wfhRequestService.create(createRequest);

        assertNotNull(result);
        assertEquals(1L, result.employeeId());
        assertEquals("WFH needed", result.reason());
        assertEquals("PENDING", result.status());
        verify(wfhRequestRepository).save(any(WfhRequest.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        var createRequest = new WfhRequestCreateRequest(999L, LocalDate.of(2024, 6, 15), "WFH needed", null);

        assertThrows(ResourceNotFoundException.class,
                () -> wfhRequestService.create(createRequest));
    }

    @Test
    void update_shouldUpdatePendingRequest() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.PENDING);
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(wfhRequestRepository.save(any(WfhRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updateRequest = new WfhRequestCreateRequest(1L, LocalDate.of(2024, 6, 20), "Updated reason", "Updated remarks");
        var result = wfhRequestService.update(1L, updateRequest);

        assertEquals("Updated reason", result.reason());
        assertEquals(LocalDate.of(2024, 6, 20), result.requestDate());
    }

    @Test
    void update_shouldThrowWhenNotPending() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.APPROVED);
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));

        var updateRequest = new WfhRequestCreateRequest(1L, LocalDate.of(2024, 6, 20), "Updated reason", null);

        assertThrows(BadRequestException.class,
                () -> wfhRequestService.update(1L, updateRequest));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        when(wfhRequestRepository.findById(999L)).thenReturn(Optional.empty());

        var updateRequest = new WfhRequestCreateRequest(1L, LocalDate.of(2024, 6, 20), "Updated reason", null);

        assertThrows(ResourceNotFoundException.class,
                () -> wfhRequestService.update(999L, updateRequest));
    }

    @Test
    void update_shouldThrowWhenEmployeeNotFound() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.PENDING);
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        var updateRequest = new WfhRequestCreateRequest(999L, LocalDate.of(2024, 6, 20), "Updated reason", null);

        assertThrows(ResourceNotFoundException.class,
                () -> wfhRequestService.update(1L, updateRequest));
    }

    @Test
    void approve_shouldApproveRequest() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.PENDING);
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));
        when(wfhRequestRepository.save(any(WfhRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var approvalRequest = new WfhApprovalRequest("APPROVED", "admin", "Approved");
        var result = wfhRequestService.approve(1L, approvalRequest);

        assertEquals("APPROVED", result.status());
        assertEquals("admin", result.approvedBy());
        assertNotNull(result.approvedAt());
    }

    @Test
    void approve_shouldRejectRequest() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.PENDING);
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));
        when(wfhRequestRepository.save(any(WfhRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var approvalRequest = new WfhApprovalRequest("REJECTED", "admin", "Not justified");
        var result = wfhRequestService.approve(1L, approvalRequest);

        assertEquals("REJECTED", result.status());
        assertEquals("admin", result.approvedBy());
    }

    @Test
    void approve_shouldThrowWhenNotPending() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.APPROVED);
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));

        var approvalRequest = new WfhApprovalRequest("REJECTED", "admin", null);

        assertThrows(BadRequestException.class,
                () -> wfhRequestService.approve(1L, approvalRequest));
    }

    @Test
    void approve_shouldThrowWhenInvalidStatus() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.PENDING);
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));

        var approvalRequest = new WfhApprovalRequest("PENDING", "admin", null);

        assertThrows(BadRequestException.class,
                () -> wfhRequestService.approve(1L, approvalRequest));
    }

    @Test
    void approve_shouldThrowWhenNotFound() {
        when(wfhRequestRepository.findById(999L)).thenReturn(Optional.empty());

        var approvalRequest = new WfhApprovalRequest("APPROVED", "admin", null);

        assertThrows(ResourceNotFoundException.class,
                () -> wfhRequestService.approve(999L, approvalRequest));
    }

    @Test
    void approve_shouldPreserveExistingRemarksWhenNull() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.PENDING);
        wfhRequest.setRemarks("Original remarks");
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));
        when(wfhRequestRepository.save(any(WfhRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var approvalRequest = new WfhApprovalRequest("APPROVED", "admin", null);
        var result = wfhRequestService.approve(1L, approvalRequest);

        assertEquals("APPROVED", result.status());
        assertEquals("Original remarks", result.remarks());
    }

    @Test
    void delete_shouldDeleteRequest() {
        var employee = createEmployee();
        var wfhRequest = createWfhRequest(1L, employee, WfhStatus.PENDING);
        when(wfhRequestRepository.findById(1L)).thenReturn(Optional.of(wfhRequest));

        wfhRequestService.delete(1L);

        verify(wfhRequestRepository).delete(wfhRequest);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(wfhRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> wfhRequestService.delete(999L));
    }

    @Test
    void getDashboard_shouldReturnAggregatedData() {
        var employee = createEmployee();
        var requests = List.of(
                createWfhRequest(1L, employee, WfhStatus.APPROVED),
                createWfhRequest(2L, employee, WfhStatus.PENDING),
                createWfhRequest(3L, employee, WfhStatus.REJECTED)
        );
        var startDate = LocalDate.of(2024, 6, 1);
        var endDate = LocalDate.of(2024, 6, 30);
        var page = new PageImpl<>(requests, Pageable.unpaged(), 3);
        when(wfhRequestRepository.findByDateRange(startDate, endDate, Pageable.unpaged())).thenReturn(page);
        when(wfhActivityLogRepository.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        var result = wfhRequestService.getDashboard(startDate, endDate);

        assertEquals(1, result.size());
        var dashboard = result.get(0);
        assertEquals(1L, dashboard.employeeId());
        assertEquals("EMP001", dashboard.employeeCode());
        assertEquals("John Doe", dashboard.employeeName());
        assertEquals(3, dashboard.totalRequests());
        assertEquals(1, dashboard.approvedRequests());
        assertEquals(1, dashboard.pendingRequests());
        assertEquals(1, dashboard.rejectedRequests());
        assertFalse(dashboard.checkedInToday());
    }
}
