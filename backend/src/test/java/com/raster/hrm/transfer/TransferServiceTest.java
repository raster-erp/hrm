package com.raster.hrm.transfer;

import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.transfer.dto.TransferRequest;
import com.raster.hrm.transfer.dto.TransferResponse;
import com.raster.hrm.transfer.entity.Transfer;
import com.raster.hrm.transfer.entity.TransferStatus;
import com.raster.hrm.transfer.entity.TransferType;
import com.raster.hrm.transfer.repository.TransferRepository;
import com.raster.hrm.transfer.service.TransferService;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private TransferService transferService;

    private Employee createEmployee(Long id, String code, String firstName, String lastName) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "@test.com");
        return employee;
    }

    private Department createDepartment(Long id, String name, String code) {
        var department = new Department();
        department.setId(id);
        department.setName(name);
        department.setCode(code);
        return department;
    }

    private Transfer createTransfer(Long id, Employee employee, Department fromDept, Department toDept) {
        var transfer = new Transfer();
        transfer.setId(id);
        transfer.setEmployee(employee);
        transfer.setFromDepartment(fromDept);
        transfer.setToDepartment(toDept);
        transfer.setFromBranch("Branch A");
        transfer.setToBranch("Branch B");
        transfer.setTransferType(TransferType.INTER_DEPARTMENT);
        transfer.setEffectiveDate(LocalDate.of(2024, 6, 1));
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setReason("Operational need");
        transfer.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        transfer.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return transfer;
    }

    private TransferRequest createRequest() {
        return new TransferRequest(
                1L, 1L, 2L,
                "Branch A", "Branch B",
                "INTER_DEPARTMENT",
                LocalDate.of(2024, 6, 1),
                "Operational need"
        );
    }

    @Test
    void getAll_shouldReturnPageOfTransfers() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfers = List.of(
                createTransfer(1L, employee, fromDept, toDept),
                createTransfer(2L, employee, fromDept, toDept)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(transfers, pageable, 2);
        when(transferRepository.findAll(pageable)).thenReturn(page);

        var result = transferService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).employeeName());
        verify(transferRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<Transfer>(List.of(), pageable, 0);
        when(transferRepository.findAll(pageable)).thenReturn(page);

        var result = transferService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnTransfer() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        var result = transferService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("Engineering", result.fromDepartmentName());
        assertEquals("Marketing", result.toDepartmentName());
        assertEquals("INTER_DEPARTMENT", result.transferType());
        assertEquals("PENDING", result.status());
        assertEquals("Operational need", result.reason());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(transferRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnTransfers() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfers = List.of(
                createTransfer(1L, employee, fromDept, toDept),
                createTransfer(2L, employee, fromDept, toDept)
        );
        when(transferRepository.findByEmployeeId(1L)).thenReturn(transfers);

        var result = transferService.getByEmployeeId(1L);

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).employeeName());
    }

    @Test
    void getByEmployeeId_shouldReturnEmptyListWhenNoTransfers() {
        when(transferRepository.findByEmployeeId(1L)).thenReturn(List.of());

        var result = transferService.getByEmployeeId(1L);

        assertEquals(0, result.size());
    }

    @Test
    void getPendingTransfers_shouldReturnPendingTransfers() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        when(transferRepository.findByStatus(TransferStatus.PENDING)).thenReturn(List.of(transfer));

        var result = transferService.getPendingTransfers();

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).status());
    }

    @Test
    void getPendingTransfers_shouldReturnEmptyListWhenNonePending() {
        when(transferRepository.findByStatus(TransferStatus.PENDING)).thenReturn(List.of());

        var result = transferService.getPendingTransfers();

        assertEquals(0, result.size());
    }

    @Test
    void create_shouldCreateAndReturnTransfer() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var request = createRequest();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(fromDept));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(toDept));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer t = invocation.getArgument(0);
            t.setId(1L);
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            return t;
        });

        var result = transferService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("INTER_DEPARTMENT", result.transferType());
        assertEquals("PENDING", result.status());
        assertEquals("Branch A", result.fromBranch());
        assertEquals("Branch B", result.toBranch());
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.create(request));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenFromDepartmentNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.create(request));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenToDepartmentNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(fromDept));
        when(departmentRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.create(request));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenInvalidTransferType() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new TransferRequest(
                1L, null, null,
                "Branch A", "Branch B",
                "INVALID_TYPE",
                LocalDate.of(2024, 6, 1),
                "Reason"
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThrows(BadRequestException.class,
                () -> transferService.create(request));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void create_shouldCreateWithoutDepartments() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new TransferRequest(
                1L, null, null,
                "Branch A", "Branch B",
                "INTER_BRANCH",
                LocalDate.of(2024, 6, 1),
                "Branch transfer"
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer t = invocation.getArgument(0);
            t.setId(1L);
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            return t;
        });

        var result = transferService.create(request);

        assertNotNull(result);
        assertNull(result.fromDepartmentId());
        assertNull(result.toDepartmentId());
        assertEquals("INTER_BRANCH", result.transferType());
    }

    @Test
    void approve_shouldApproveTransfer() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        transfer.setStatus(TransferStatus.PENDING);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = transferService.approve(1L, 2L);

        assertEquals("APPROVED", result.status());
        assertEquals(2L, result.approvedById());
        assertEquals("Jane Smith", result.approvedByName());
        assertNotNull(result.approvedAt());
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void approve_shouldThrowWhenTransferNotFound() {
        when(transferRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.approve(999L, 2L));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenNotPending() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        transfer.setStatus(TransferStatus.APPROVED);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        assertThrows(BadRequestException.class,
                () -> transferService.approve(1L, 2L));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenApproverNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.approve(1L, 2L));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void reject_shouldRejectTransfer() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        transfer.setStatus(TransferStatus.PENDING);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = transferService.reject(1L, 2L);

        assertEquals("REJECTED", result.status());
        assertEquals(2L, result.approvedById());
        assertEquals("Jane Smith", result.approvedByName());
        assertNotNull(result.approvedAt());
    }

    @Test
    void reject_shouldThrowWhenTransferNotFound() {
        when(transferRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.reject(999L, 2L));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void reject_shouldThrowWhenNotPending() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        transfer.setStatus(TransferStatus.EXECUTED);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        assertThrows(BadRequestException.class,
                () -> transferService.reject(1L, 2L));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void reject_shouldThrowWhenApproverNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.reject(1L, 2L));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void execute_shouldExecuteTransferAndUpdateEmployeeDepartment() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        transfer.setStatus(TransferStatus.APPROVED);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = transferService.execute(1L);

        assertEquals("EXECUTED", result.status());
        assertEquals(toDept, employee.getDepartment());
        verify(employeeRepository).save(employee);
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void execute_shouldExecuteWithoutUpdatingDepartmentWhenToDepartmentNull() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var transfer = createTransfer(1L, employee, null, null);
        transfer.setFromBranch("Branch A");
        transfer.setToBranch("Branch B");
        transfer.setStatus(TransferStatus.APPROVED);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = transferService.execute(1L);

        assertEquals("EXECUTED", result.status());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenTransferNotFound() {
        when(transferRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.execute(999L));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenNotApproved() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        transfer.setStatus(TransferStatus.PENDING);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        assertThrows(BadRequestException.class,
                () -> transferService.execute(1L));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenRejected() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        transfer.setStatus(TransferStatus.REJECTED);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        assertThrows(BadRequestException.class,
                () -> transferService.execute(1L));
    }

    @Test
    void delete_shouldDeletePendingTransfer() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        transfer.setStatus(TransferStatus.PENDING);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        transferService.delete(1L);

        verify(transferRepository).delete(transfer);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(transferRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.delete(999L));
        verify(transferRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowWhenNotPending() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        transfer.setStatus(TransferStatus.APPROVED);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        assertThrows(BadRequestException.class,
                () -> transferService.delete(1L));
        verify(transferRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowWhenExecuted() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        transfer.setStatus(TransferStatus.EXECUTED);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        assertThrows(BadRequestException.class,
                () -> transferService.delete(1L));
        verify(transferRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        var result = transferService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals(1L, result.fromDepartmentId());
        assertEquals("Engineering", result.fromDepartmentName());
        assertEquals(2L, result.toDepartmentId());
        assertEquals("Marketing", result.toDepartmentName());
        assertEquals("Branch A", result.fromBranch());
        assertEquals("Branch B", result.toBranch());
        assertEquals("INTER_DEPARTMENT", result.transferType());
        assertEquals(LocalDate.of(2024, 6, 1), result.effectiveDate());
        assertEquals("PENDING", result.status());
        assertEquals("Operational need", result.reason());
        assertNull(result.approvedById());
        assertNull(result.approvedByName());
        assertNull(result.approvedAt());
    }

    @Test
    void getById_shouldMapNullDepartmentsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var transfer = createTransfer(1L, employee, null, null);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        var result = transferService.getById(1L);

        assertNull(result.fromDepartmentId());
        assertNull(result.fromDepartmentName());
        assertNull(result.toDepartmentId());
        assertNull(result.toDepartmentName());
    }

    @Test
    void create_shouldMapAllFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var request = new TransferRequest(
                1L, 1L, 2L,
                "HQ", "Regional",
                "INTER_BRANCH",
                LocalDate.of(2024, 12, 1),
                "Strategic move"
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(fromDept));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(toDept));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer t = invocation.getArgument(0);
            t.setId(1L);
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            return t;
        });

        var result = transferService.create(request);

        assertEquals("INTER_BRANCH", result.transferType());
        assertEquals("HQ", result.fromBranch());
        assertEquals("Regional", result.toBranch());
        assertEquals(LocalDate.of(2024, 12, 1), result.effectiveDate());
        assertEquals("Strategic move", result.reason());
        assertEquals("PENDING", result.status());
    }

    @Test
    void fullWorkflow_createApproveExecute() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        transfer.setStatus(TransferStatus.PENDING);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var approvedResult = transferService.approve(1L, 2L);
        assertEquals("APPROVED", approvedResult.status());

        var executedResult = transferService.execute(1L);
        assertEquals("EXECUTED", executedResult.status());
        verify(employeeRepository).save(employee);
    }

    @Test
    void fullWorkflow_createReject() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var fromDept = createDepartment(1L, "Engineering", "ENG");
        var toDept = createDepartment(2L, "Marketing", "MKT");
        var transfer = createTransfer(1L, employee, fromDept, toDept);
        transfer.setStatus(TransferStatus.PENDING);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var rejectedResult = transferService.reject(1L, 2L);
        assertEquals("REJECTED", rejectedResult.status());
    }
}
