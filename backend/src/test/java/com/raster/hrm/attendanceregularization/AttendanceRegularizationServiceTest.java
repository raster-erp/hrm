package com.raster.hrm.attendanceregularization;

import com.raster.hrm.attendance.entity.AttendancePunch;
import com.raster.hrm.attendance.repository.AttendancePunchRepository;
import com.raster.hrm.attendanceregularization.dto.RegularizationApprovalRequest;
import com.raster.hrm.attendanceregularization.dto.RegularizationRequestRequest;
import com.raster.hrm.attendanceregularization.entity.RegularizationRequest;
import com.raster.hrm.attendanceregularization.entity.RegularizationStatus;
import com.raster.hrm.attendanceregularization.entity.RegularizationType;
import com.raster.hrm.attendanceregularization.repository.RegularizationRequestRepository;
import com.raster.hrm.attendanceregularization.service.RegularizationRequestService;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceRegularizationServiceTest {

    @Mock
    private RegularizationRequestRepository regularizationRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendancePunchRepository attendancePunchRepository;

    @InjectMocks
    private RegularizationRequestService regularizationRequestService;

    private Employee createEmployee() {
        var employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        return employee;
    }

    private RegularizationRequest createRegularizationRequest(Long id, Employee employee, RegularizationType type) {
        var request = new RegularizationRequest();
        request.setId(id);
        request.setEmployee(employee);
        request.setRequestDate(LocalDate.of(2024, 1, 15));
        request.setType(type);
        request.setReason("Missed punch due to system error");
        request.setOriginalPunchIn(LocalDateTime.of(2024, 1, 15, 9, 0));
        request.setOriginalPunchOut(null);
        request.setCorrectedPunchIn(LocalDateTime.of(2024, 1, 15, 9, 0));
        request.setCorrectedPunchOut(LocalDateTime.of(2024, 1, 15, 17, 0));
        request.setStatus(RegularizationStatus.PENDING);
        request.setApprovalLevel(0);
        request.setRemarks("Test regularization");
        request.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        request.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return request;
    }

    private RegularizationRequestRequest createRequestDto() {
        return new RegularizationRequestRequest(
                1L,
                LocalDate.of(2024, 1, 15),
                "MISSED_PUNCH",
                "Missed punch due to system error",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                null,
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 17, 0),
                "Test regularization"
        );
    }

    @Test
    void getAll_shouldReturnPage() {
        var employee = createEmployee();
        var requests = List.of(
                createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH),
                createRegularizationRequest(2L, employee, RegularizationType.ON_DUTY)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(requests, pageable, 2);
        when(regularizationRequestRepository.findAll(pageable)).thenReturn(page);

        var result = regularizationRequestService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).employeeName());
        verify(regularizationRequestRepository).findAll(pageable);
    }

    @Test
    void getById_shouldReturnResponse() {
        var employee = createEmployee();
        var request = createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH);
        when(regularizationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        var result = regularizationRequestService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("MISSED_PUNCH", result.type());
        assertEquals("PENDING", result.status());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(regularizationRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> regularizationRequestService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnPage() {
        var employee = createEmployee();
        var requests = List.of(createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(requests, pageable, 1);
        when(regularizationRequestRepository.findByEmployeeId(1L, pageable)).thenReturn(page);

        var result = regularizationRequestService.getByEmployeeId(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).employeeId());
    }

    @Test
    void getByStatus_shouldReturnPage() {
        var employee = createEmployee();
        var requests = List.of(createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(requests, pageable, 1);
        when(regularizationRequestRepository.findByStatus(RegularizationStatus.PENDING, pageable)).thenReturn(page);

        var result = regularizationRequestService.getByStatus(RegularizationStatus.PENDING, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("PENDING", result.getContent().get(0).status());
    }

    @Test
    void getByType_shouldReturnPage() {
        var employee = createEmployee();
        var requests = List.of(createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(requests, pageable, 1);
        when(regularizationRequestRepository.findByType(RegularizationType.MISSED_PUNCH, pageable)).thenReturn(page);

        var result = regularizationRequestService.getByType(RegularizationType.MISSED_PUNCH, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("MISSED_PUNCH", result.getContent().get(0).type());
    }

    @Test
    void getByDateRange_shouldReturnPage() {
        var employee = createEmployee();
        var requests = List.of(createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH));
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(requests, pageable, 1);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        when(regularizationRequestRepository.findByDateRange(startDate, endDate, pageable)).thenReturn(page);

        var result = regularizationRequestService.getByDateRange(startDate, endDate, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void create_shouldCreateRequest() {
        var employee = createEmployee();
        var requestDto = createRequestDto();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(regularizationRequestRepository.save(any(RegularizationRequest.class))).thenAnswer(invocation -> {
            RegularizationRequest r = invocation.getArgument(0);
            r.setId(1L);
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(LocalDateTime.now());
            return r;
        });

        var result = regularizationRequestService.create(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("John Doe", result.employeeName());
        assertEquals("MISSED_PUNCH", result.type());
        assertEquals("Missed punch due to system error", result.reason());
        verify(regularizationRequestRepository).save(any(RegularizationRequest.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var requestDto = createRequestDto();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> regularizationRequestService.create(requestDto));
        verify(regularizationRequestRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateRequest() {
        var employee = createEmployee();
        var request = createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH);
        var requestDto = createRequestDto();
        when(regularizationRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(regularizationRequestRepository.save(any(RegularizationRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = regularizationRequestService.update(1L, requestDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("MISSED_PUNCH", result.type());
        verify(regularizationRequestRepository).save(any(RegularizationRequest.class));
    }

    @Test
    void update_shouldThrowWhenNotPending() {
        var employee = createEmployee();
        var request = createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH);
        request.setStatus(RegularizationStatus.APPROVED);
        var requestDto = createRequestDto();
        when(regularizationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        var ex = assertThrows(BadRequestException.class,
                () -> regularizationRequestService.update(1L, requestDto));
        assertTrue(ex.getMessage().contains("APPROVED"));
        verify(regularizationRequestRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var requestDto = createRequestDto();
        when(regularizationRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> regularizationRequestService.update(999L, requestDto));
        verify(regularizationRequestRepository, never()).save(any());
    }

    @Test
    void approve_shouldApproveRequest() {
        var employee = createEmployee();
        var request = createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH);
        var approvalRequest = new RegularizationApprovalRequest("APPROVED", "admin", "Acknowledged");
        when(regularizationRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(regularizationRequestRepository.save(any(RegularizationRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attendancePunchRepository.save(any(AttendancePunch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = regularizationRequestService.approve(1L, approvalRequest);

        assertEquals("APPROVED", result.status());
        assertEquals("admin", result.approvedBy());
        verify(regularizationRequestRepository).save(any(RegularizationRequest.class));
    }

    @Test
    void approve_shouldRejectRequest() {
        var employee = createEmployee();
        var request = createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH);
        var approvalRequest = new RegularizationApprovalRequest("REJECTED", "admin", "Insufficient info");
        when(regularizationRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(regularizationRequestRepository.save(any(RegularizationRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = regularizationRequestService.approve(1L, approvalRequest);

        assertEquals("REJECTED", result.status());
        assertEquals("admin", result.approvedBy());
        verify(attendancePunchRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenNotPending() {
        var employee = createEmployee();
        var request = createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH);
        request.setStatus(RegularizationStatus.APPROVED);
        var approvalRequest = new RegularizationApprovalRequest("REJECTED", "admin", null);
        when(regularizationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        var ex = assertThrows(BadRequestException.class,
                () -> regularizationRequestService.approve(1L, approvalRequest));
        assertTrue(ex.getMessage().contains("APPROVED"));
        verify(regularizationRequestRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenNotFound() {
        var approvalRequest = new RegularizationApprovalRequest("APPROVED", "admin", null);
        when(regularizationRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> regularizationRequestService.approve(999L, approvalRequest));
        verify(regularizationRequestRepository, never()).save(any());
    }

    @Test
    void approve_shouldCreateAttendancePunches() {
        var employee = createEmployee();
        var request = createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH);
        var approvalRequest = new RegularizationApprovalRequest("APPROVED", "admin", "Approved");
        when(regularizationRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(regularizationRequestRepository.save(any(RegularizationRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attendancePunchRepository.save(any(AttendancePunch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        regularizationRequestService.approve(1L, approvalRequest);

        verify(attendancePunchRepository, times(2)).save(any(AttendancePunch.class));
    }

    @Test
    void delete_shouldDeleteRequest() {
        var employee = createEmployee();
        var request = createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH);
        when(regularizationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        regularizationRequestService.delete(1L);

        verify(regularizationRequestRepository).delete(request);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(regularizationRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> regularizationRequestService.delete(999L));
        verify(regularizationRequestRepository, never()).delete(any());
    }

    @Test
    void getByEmployeeAndDateRange_shouldReturnList() {
        var employee = createEmployee();
        var requests = List.of(
                createRegularizationRequest(1L, employee, RegularizationType.MISSED_PUNCH),
                createRegularizationRequest(2L, employee, RegularizationType.ON_DUTY)
        );
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        when(regularizationRequestRepository.findByEmployeeIdAndDateRange(1L, startDate, endDate)).thenReturn(requests);

        var result = regularizationRequestService.getByEmployeeAndDateRange(1L, startDate, endDate);

        assertEquals(2, result.size());
        assertEquals("MISSED_PUNCH", result.get(0).type());
        assertEquals("ON_DUTY", result.get(1).type());
    }
}
