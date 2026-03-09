package com.raster.hrm.contract;

import com.raster.hrm.contract.dto.ContractAmendmentRequest;
import com.raster.hrm.contract.dto.ContractRequest;
import com.raster.hrm.contract.entity.ContractAmendment;
import com.raster.hrm.contract.entity.ContractStatus;
import com.raster.hrm.contract.entity.ContractType;
import com.raster.hrm.contract.entity.EmploymentContract;
import com.raster.hrm.contract.repository.ContractAmendmentRepository;
import com.raster.hrm.contract.repository.ContractRepository;
import com.raster.hrm.contract.service.ContractService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractAmendmentRepository amendmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ContractService contractService;

    private Employee createEmployee(Long id, String code, String firstName, String lastName) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "@test.com");
        return employee;
    }

    private EmploymentContract createContract(Long id, Employee employee, ContractType type, ContractStatus status) {
        var contract = new EmploymentContract();
        contract.setId(id);
        contract.setEmployee(employee);
        contract.setContractType(type);
        contract.setStartDate(LocalDate.of(2024, 1, 1));
        contract.setEndDate(LocalDate.of(2024, 12, 31));
        contract.setTerms("Standard terms");
        contract.setStatus(status);
        contract.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        contract.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        return contract;
    }

    private ContractRequest createContractRequest() {
        return new ContractRequest(
                1L, "PERMANENT", LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31), "Standard terms", "ACTIVE"
        );
    }

    private ContractAmendment createAmendment(Long id, EmploymentContract contract) {
        var amendment = new ContractAmendment();
        amendment.setId(id);
        amendment.setContract(contract);
        amendment.setAmendmentDate(LocalDate.of(2024, 6, 1));
        amendment.setDescription("Updated salary clause");
        amendment.setOldTerms("Old terms");
        amendment.setNewTerms("New terms");
        amendment.setCreatedAt(LocalDateTime.of(2024, 6, 1, 10, 0));
        return amendment;
    }

    @Test
    void getAll_shouldReturnPageOfContracts() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var contracts = List.of(
                createContract(1L, employee, ContractType.PERMANENT, ContractStatus.ACTIVE),
                createContract(2L, employee, ContractType.FIXED_TERM, ContractStatus.ACTIVE)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(contracts, pageable, 2);
        when(contractRepository.findAll(pageable)).thenReturn(page);

        var result = contractService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("PERMANENT", result.getContent().get(0).contractType());
        assertEquals("FIXED_TERM", result.getContent().get(1).contractType());
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<EmploymentContract>(List.of(), pageable, 0);
        when(contractRepository.findAll(pageable)).thenReturn(page);

        var result = contractService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getById_shouldReturnContract() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var contract = createContract(1L, employee, ContractType.PERMANENT, ContractStatus.ACTIVE);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        var result = contractService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("PERMANENT", result.contractType());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("ACTIVE", result.status());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class,
                () -> contractService.getById(999L));

        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void getByEmployee_shouldReturnContracts() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var contracts = List.of(
                createContract(1L, employee, ContractType.PERMANENT, ContractStatus.ACTIVE),
                createContract(2L, employee, ContractType.PROBATION, ContractStatus.EXPIRED)
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.findByEmployeeId(1L)).thenReturn(contracts);

        var result = contractService.getByEmployee(1L);

        assertEquals(2, result.size());
        assertEquals("PERMANENT", result.get(0).contractType());
        assertEquals("PROBATION", result.get(1).contractType());
    }

    @Test
    void getByEmployee_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contractService.getByEmployee(999L));
    }

    @Test
    void getByEmployee_shouldReturnEmptyListWhenNoContracts() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.findByEmployeeId(1L)).thenReturn(List.of());

        var result = contractService.getByEmployee(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getExpiringContracts_shouldReturnContracts() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var contracts = List.of(
                createContract(1L, employee, ContractType.FIXED_TERM, ContractStatus.ACTIVE)
        );
        var startDate = LocalDate.of(2024, 11, 1);
        var endDate = LocalDate.of(2024, 12, 31);
        when(contractRepository.findByEndDateBetween(startDate, endDate)).thenReturn(contracts);

        var result = contractService.getExpiringContracts(startDate, endDate);

        assertEquals(1, result.size());
        assertEquals("FIXED_TERM", result.get(0).contractType());
    }

    @Test
    void getExpiringContracts_shouldThrowWhenStartDateAfterEndDate() {
        var startDate = LocalDate.of(2024, 12, 31);
        var endDate = LocalDate.of(2024, 1, 1);

        assertThrows(BadRequestException.class,
                () -> contractService.getExpiringContracts(startDate, endDate));
    }

    @Test
    void getExpiringContracts_shouldReturnEmptyListWhenNoneExpiring() {
        var startDate = LocalDate.of(2024, 11, 1);
        var endDate = LocalDate.of(2024, 12, 31);
        when(contractRepository.findByEndDateBetween(startDate, endDate)).thenReturn(List.of());

        var result = contractService.getExpiringContracts(startDate, endDate);

        assertTrue(result.isEmpty());
    }

    @Test
    void create_shouldReturnCreatedContract() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = createContractRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.save(any(EmploymentContract.class))).thenAnswer(invocation -> {
            EmploymentContract saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        var result = contractService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("PERMANENT", result.contractType());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        verify(contractRepository).save(any(EmploymentContract.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var request = createContractRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contractService.create(request));
    }

    @Test
    void create_shouldSetStatusFromRequest() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new ContractRequest(
                1L, "PERMANENT", LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31), "Terms", "ACTIVE"
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.save(any(EmploymentContract.class))).thenAnswer(invocation -> {
            EmploymentContract saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        var result = contractService.create(request);

        assertEquals("ACTIVE", result.status());
    }

    @Test
    void create_shouldUseDefaultStatusWhenNullInRequest() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new ContractRequest(
                1L, "PERMANENT", LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31), "Terms", null
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.save(any(EmploymentContract.class))).thenAnswer(invocation -> {
            EmploymentContract saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        var result = contractService.create(request);

        assertEquals("ACTIVE", result.status());
    }

    @Test
    void create_shouldUseDefaultStatusWhenBlankInRequest() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new ContractRequest(
                1L, "PERMANENT", LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31), "Terms", ""
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.save(any(EmploymentContract.class))).thenAnswer(invocation -> {
            EmploymentContract saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        var result = contractService.create(request);

        assertEquals("ACTIVE", result.status());
    }

    @Test
    void update_shouldReturnUpdatedContract() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var contract = createContract(1L, employee, ContractType.PERMANENT, ContractStatus.ACTIVE);
        var request = createContractRequest();
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.save(any(EmploymentContract.class))).thenAnswer(invocation -> {
            EmploymentContract saved = invocation.getArgument(0);
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        var result = contractService.update(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("PERMANENT", result.contractType());
        verify(contractRepository).save(any(EmploymentContract.class));
    }

    @Test
    void update_shouldThrowWhenContractNotFound() {
        var request = createContractRequest();
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contractService.update(999L, request));
    }

    @Test
    void update_shouldThrowWhenEmployeeNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var contract = createContract(1L, employee, ContractType.PERMANENT, ContractStatus.ACTIVE);
        var request = new ContractRequest(
                999L, "PERMANENT", LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31), "Terms", "ACTIVE"
        );
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contractService.update(1L, request));
    }

    @Test
    void renewContract_shouldSetOldToRenewedAndCreateNew() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldContract = createContract(1L, employee, ContractType.PERMANENT, ContractStatus.ACTIVE);
        var request = new ContractRequest(
                1L, "PERMANENT", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31), "Renewed terms", "ACTIVE"
        );
        when(contractRepository.findById(1L)).thenReturn(Optional.of(oldContract));
        when(contractRepository.save(any(EmploymentContract.class))).thenAnswer(invocation -> {
            EmploymentContract saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(2L);
                saved.setCreatedAt(LocalDateTime.now());
            }
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        var result = contractService.renewContract(1L, request);

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("ACTIVE", result.status());
        assertEquals(ContractStatus.RENEWED, oldContract.getStatus());
    }

    @Test
    void renewContract_shouldThrowWhenContractNotFound() {
        var request = createContractRequest();
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contractService.renewContract(999L, request));
    }

    @Test
    void renewContract_shouldThrowWhenContractNotActive() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var contract = createContract(1L, employee, ContractType.PERMANENT, ContractStatus.EXPIRED);
        var request = createContractRequest();
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        var exception = assertThrows(BadRequestException.class,
                () -> contractService.renewContract(1L, request));

        assertTrue(exception.getMessage().contains("Only active contracts can be renewed"));
    }

    @Test
    void renewContract_shouldThrowWhenContractTerminated() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var contract = createContract(1L, employee, ContractType.PERMANENT, ContractStatus.TERMINATED);
        var request = createContractRequest();
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        assertThrows(BadRequestException.class,
                () -> contractService.renewContract(1L, request));
    }

    @Test
    void addAmendment_shouldReturnCreatedAmendment() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var contract = createContract(1L, employee, ContractType.PERMANENT, ContractStatus.ACTIVE);
        var request = new ContractAmendmentRequest(
                LocalDate.of(2024, 6, 1), "Updated salary clause",
                "Old terms", "New terms"
        );
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(amendmentRepository.save(any(ContractAmendment.class))).thenAnswer(invocation -> {
            ContractAmendment saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        var result = contractService.addAmendment(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.contractId());
        assertEquals("Updated salary clause", result.description());
        verify(amendmentRepository).save(any(ContractAmendment.class));
    }

    @Test
    void addAmendment_shouldThrowWhenContractNotFound() {
        var request = new ContractAmendmentRequest(
                LocalDate.of(2024, 6, 1), "Description",
                "Old terms", "New terms"
        );
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contractService.addAmendment(999L, request));
    }

    @Test
    void getAmendments_shouldReturnAmendments() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var contract = createContract(1L, employee, ContractType.PERMANENT, ContractStatus.ACTIVE);
        var amendments = List.of(
                createAmendment(1L, contract),
                createAmendment(2L, contract)
        );
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(amendmentRepository.findByContractId(1L)).thenReturn(amendments);

        var result = contractService.getAmendments(1L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
    }

    @Test
    void getAmendments_shouldThrowWhenContractNotFound() {
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contractService.getAmendments(999L));
    }

    @Test
    void getAmendments_shouldReturnEmptyListWhenNoAmendments() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var contract = createContract(1L, employee, ContractType.PERMANENT, ContractStatus.ACTIVE);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(amendmentRepository.findByContractId(1L)).thenReturn(List.of());

        var result = contractService.getAmendments(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getById_shouldReturnContractWithNullEndDate() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var contract = createContract(1L, employee, ContractType.PERMANENT, ContractStatus.ACTIVE);
        contract.setEndDate(null);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        var result = contractService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(null, result.endDate());
    }

    @Test
    void create_shouldHandleNullEndDate() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new ContractRequest(
                1L, "PERMANENT", LocalDate.of(2024, 1, 1),
                null, "Terms", "ACTIVE"
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(contractRepository.save(any(EmploymentContract.class))).thenAnswer(invocation -> {
            EmploymentContract saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        var result = contractService.create(request);

        assertNotNull(result);
        assertEquals(null, result.endDate());
    }
}
