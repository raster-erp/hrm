package com.raster.hrm.idcard;

import com.raster.hrm.idcard.dto.IdCardRequest;
import com.raster.hrm.idcard.entity.IdCard;
import com.raster.hrm.idcard.entity.IdCardStatus;
import com.raster.hrm.idcard.repository.IdCardRepository;
import com.raster.hrm.idcard.service.IdCardService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdCardServiceTest {

    @Mock
    private IdCardRepository idCardRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private IdCardService idCardService;

    private Employee createEmployee(Long id, String code, String firstName, String lastName) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "@test.com");
        return employee;
    }

    private IdCard createIdCard(Long id, Employee employee, String cardNumber) {
        var idCard = new IdCard();
        idCard.setId(id);
        idCard.setEmployee(employee);
        idCard.setCardNumber(cardNumber);
        idCard.setIssueDate(LocalDate.of(2024, 1, 1));
        idCard.setExpiryDate(LocalDate.of(2026, 12, 31));
        idCard.setStatus(IdCardStatus.ACTIVE);
        idCard.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        idCard.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return idCard;
    }

    private IdCardRequest createRequest() {
        return new IdCardRequest(
                1L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    @Test
    void getAll_shouldReturnPageOfIdCards() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var idCards = List.of(
                createIdCard(1L, employee, "IDC-ABC1234567"),
                createIdCard(2L, employee, "IDC-DEF7890123")
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(idCards, pageable, 2);
        when(idCardRepository.findAll(pageable)).thenReturn(page);

        var result = idCardService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("IDC-ABC1234567", result.getContent().get(0).cardNumber());
        assertEquals("IDC-DEF7890123", result.getContent().get(1).cardNumber());
        verify(idCardRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<IdCard>(List.of(), pageable, 0);
        when(idCardRepository.findAll(pageable)).thenReturn(page);

        var result = idCardService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnIdCard() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var idCard = createIdCard(1L, employee, "IDC-ABC1234567");
        when(idCardRepository.findById(1L)).thenReturn(Optional.of(idCard));

        var result = idCardService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("IDC-ABC1234567", result.cardNumber());
        assertEquals("ACTIVE", result.status());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(idCardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> idCardService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnIdCards() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var idCards = List.of(
                createIdCard(1L, employee, "IDC-ABC1234567"),
                createIdCard(2L, employee, "IDC-DEF7890123")
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(idCardRepository.findByEmployeeId(1L)).thenReturn(idCards);

        var result = idCardService.getByEmployeeId(1L);

        assertEquals(2, result.size());
        assertEquals("IDC-ABC1234567", result.get(0).cardNumber());
        assertEquals("IDC-DEF7890123", result.get(1).cardNumber());
    }

    @Test
    void getByEmployeeId_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> idCardService.getByEmployeeId(999L));
        verify(idCardRepository, never()).findByEmployeeId(any());
    }

    @Test
    void getByEmployeeId_shouldReturnEmptyListWhenNoIdCards() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(idCardRepository.findByEmployeeId(1L)).thenReturn(List.of());

        var result = idCardService.getByEmployeeId(1L);

        assertEquals(0, result.size());
    }

    @Test
    void create_shouldCreateAndReturnIdCard() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(idCardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(idCardRepository.save(any(IdCard.class))).thenAnswer(invocation -> {
            IdCard c = invocation.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        var result = idCardService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertNotNull(result.cardNumber());
        assertTrue(result.cardNumber().startsWith("IDC-"));
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals(LocalDate.of(2024, 1, 1), result.issueDate());
        assertEquals(LocalDate.of(2026, 12, 31), result.expiryDate());
        verify(idCardRepository).save(any(IdCard.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> idCardService.create(request));
        verify(idCardRepository, never()).save(any());
    }

    @Test
    void create_shouldGenerateUniqueCardNumber() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(idCardRepository.existsByCardNumber(anyString()))
                .thenReturn(true)
                .thenReturn(false);
        when(idCardRepository.save(any(IdCard.class))).thenAnswer(invocation -> {
            IdCard c = invocation.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        var result = idCardService.create(request);

        assertNotNull(result);
        assertTrue(result.cardNumber().startsWith("IDC-"));
        assertEquals(14, result.cardNumber().length());
    }

    @Test
    void update_shouldUpdateAndReturnIdCard() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var idCard = createIdCard(1L, employee, "IDC-ABC1234567");
        var request = createRequest();
        when(idCardRepository.findById(1L)).thenReturn(Optional.of(idCard));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(idCardRepository.save(any(IdCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = idCardService.update(1L, request);

        assertNotNull(result);
        assertEquals("IDC-ABC1234567", result.cardNumber());
        assertEquals(LocalDate.of(2024, 1, 1), result.issueDate());
        assertEquals(LocalDate.of(2026, 12, 31), result.expiryDate());
        verify(idCardRepository).save(any(IdCard.class));
    }

    @Test
    void update_shouldThrowWhenIdCardNotFound() {
        var request = createRequest();
        when(idCardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> idCardService.update(999L, request));
        verify(idCardRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenEmployeeNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var idCard = createIdCard(1L, employee, "IDC-ABC1234567");
        var request = createRequest();
        when(idCardRepository.findById(1L)).thenReturn(Optional.of(idCard));
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> idCardService.update(1L, request));
        verify(idCardRepository, never()).save(any());
    }

    @Test
    void updateStatus_shouldUpdateStatus() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var idCard = createIdCard(1L, employee, "IDC-ABC1234567");
        when(idCardRepository.findById(1L)).thenReturn(Optional.of(idCard));
        when(idCardRepository.save(any(IdCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = idCardService.updateStatus(1L, IdCardStatus.EXPIRED);

        assertEquals("EXPIRED", result.status());
        verify(idCardRepository).save(any(IdCard.class));
    }

    @Test
    void updateStatus_shouldThrowWhenNotFound() {
        when(idCardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> idCardService.updateStatus(999L, IdCardStatus.EXPIRED));
        verify(idCardRepository, never()).save(any());
    }

    @Test
    void updateStatus_shouldSetCancelledStatus() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var idCard = createIdCard(1L, employee, "IDC-ABC1234567");
        when(idCardRepository.findById(1L)).thenReturn(Optional.of(idCard));
        when(idCardRepository.save(any(IdCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = idCardService.updateStatus(1L, IdCardStatus.CANCELLED);

        assertEquals("CANCELLED", result.status());
    }

    @Test
    void delete_shouldDeleteIdCard() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var idCard = createIdCard(1L, employee, "IDC-ABC1234567");
        when(idCardRepository.findById(1L)).thenReturn(Optional.of(idCard));

        idCardService.delete(1L);

        verify(idCardRepository).delete(idCard);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(idCardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> idCardService.delete(999L));
        verify(idCardRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var idCard = createIdCard(1L, employee, "IDC-ABC1234567");
        when(idCardRepository.findById(1L)).thenReturn(Optional.of(idCard));

        var result = idCardService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("IDC-ABC1234567", result.cardNumber());
        assertEquals(LocalDate.of(2024, 1, 1), result.issueDate());
        assertEquals(LocalDate.of(2026, 12, 31), result.expiryDate());
        assertEquals("ACTIVE", result.status());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.updatedAt());
    }

    @Test
    void create_shouldMapAllFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new IdCardRequest(
                1L,
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2027, 6, 1)
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(idCardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(idCardRepository.save(any(IdCard.class))).thenAnswer(invocation -> {
            IdCard c = invocation.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        var result = idCardService.create(request);

        assertEquals(LocalDate.of(2024, 6, 1), result.issueDate());
        assertEquals(LocalDate.of(2027, 6, 1), result.expiryDate());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
    }
}
