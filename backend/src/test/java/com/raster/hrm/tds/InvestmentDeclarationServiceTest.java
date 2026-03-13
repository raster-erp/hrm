package com.raster.hrm.tds;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.tds.dto.InvestmentDeclarationItemRequest;
import com.raster.hrm.tds.dto.InvestmentDeclarationRequest;
import com.raster.hrm.tds.dto.ProofSubmissionRequest;
import com.raster.hrm.tds.dto.ProofVerificationRequest;
import com.raster.hrm.tds.entity.DeclarationStatus;
import com.raster.hrm.tds.entity.InvestmentDeclaration;
import com.raster.hrm.tds.entity.InvestmentDeclarationItem;
import com.raster.hrm.tds.entity.ProofStatus;
import com.raster.hrm.tds.entity.TaxRegime;
import com.raster.hrm.tds.repository.InvestmentDeclarationItemRepository;
import com.raster.hrm.tds.repository.InvestmentDeclarationRepository;
import com.raster.hrm.tds.service.InvestmentDeclarationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestmentDeclarationServiceTest {

    @Mock
    private InvestmentDeclarationRepository investmentDeclarationRepository;

    @Mock
    private InvestmentDeclarationItemRepository investmentDeclarationItemRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private InvestmentDeclarationService investmentDeclarationService;

    private Employee createEmployee(Long id) {
        var employee = new Employee();
        employee.setId(id);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setBasicSalary(new BigDecimal("100000"));
        return employee;
    }

    private InvestmentDeclaration createDeclaration(Long id, Employee employee, String fy, DeclarationStatus status) {
        var declaration = new InvestmentDeclaration();
        declaration.setId(id);
        declaration.setEmployee(employee);
        declaration.setFinancialYear(fy);
        declaration.setRegime(TaxRegime.NEW);
        declaration.setStatus(status);
        declaration.setTotalDeclaredAmount(new BigDecimal("150000.00"));
        declaration.setTotalVerifiedAmount(BigDecimal.ZERO);
        declaration.setRemarks("Test declaration");
        declaration.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        declaration.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));

        var item = new InvestmentDeclarationItem();
        item.setId(1L);
        item.setDeclaration(declaration);
        item.setSection("80C");
        item.setDescription("PPF");
        item.setDeclaredAmount(new BigDecimal("150000.00"));
        item.setVerifiedAmount(BigDecimal.ZERO);
        item.setProofStatus(ProofStatus.PENDING);
        item.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        item.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));

        var items = new ArrayList<InvestmentDeclarationItem>();
        items.add(item);
        declaration.setItems(items);

        return declaration;
    }

    private InvestmentDeclarationRequest createRequest() {
        var itemRequests = List.of(
                new InvestmentDeclarationItemRequest("80C", "PPF", new BigDecimal("150000"))
        );
        return new InvestmentDeclarationRequest(1L, "2025-26", "NEW", "Test remarks", itemRequests);
    }

    @Test
    void getById_shouldReturnDeclaration() {
        var employee = createEmployee(1L);
        var declaration = createDeclaration(1L, employee, "2025-26", DeclarationStatus.DRAFT);
        when(investmentDeclarationRepository.findById(1L)).thenReturn(Optional.of(declaration));
        when(investmentDeclarationItemRepository.findByDeclarationId(1L)).thenReturn(declaration.getItems());

        var result = investmentDeclarationService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("John Doe", result.employeeName());
        assertEquals("2025-26", result.financialYear());
        assertEquals("NEW", result.regime());
        assertEquals("DRAFT", result.status());
        assertEquals(1, result.items().size());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(investmentDeclarationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> investmentDeclarationService.getById(999L));
    }

    @Test
    void create_shouldCreateDeclaration() {
        var employee = createEmployee(1L);
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(investmentDeclarationRepository.existsByEmployeeIdAndFinancialYear(1L, "2025-26")).thenReturn(false);
        when(investmentDeclarationRepository.save(any(InvestmentDeclaration.class))).thenAnswer(invocation -> {
            InvestmentDeclaration d = invocation.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(LocalDateTime.now());
            d.setUpdatedAt(LocalDateTime.now());
            return d;
        });

        var result = investmentDeclarationService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("DRAFT", result.status());
        assertEquals("NEW", result.regime());
        assertEquals(new BigDecimal("150000.00"), result.totalDeclaredAmount());
        verify(investmentDeclarationRepository).save(any(InvestmentDeclaration.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> investmentDeclarationService.create(request));
        verify(investmentDeclarationRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenDuplicate() {
        var employee = createEmployee(1L);
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(investmentDeclarationRepository.existsByEmployeeIdAndFinancialYear(1L, "2025-26")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> investmentDeclarationService.create(request));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(investmentDeclarationRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateDeclaration() {
        var employee = createEmployee(1L);
        var declaration = createDeclaration(1L, employee, "2025-26", DeclarationStatus.DRAFT);
        var request = createRequest();
        when(investmentDeclarationRepository.findById(1L)).thenReturn(Optional.of(declaration));
        when(investmentDeclarationRepository.save(any(InvestmentDeclaration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = investmentDeclarationService.update(1L, request);

        assertNotNull(result);
        assertEquals("NEW", result.regime());
        verify(investmentDeclarationRepository).save(any(InvestmentDeclaration.class));
    }

    @Test
    void update_shouldThrowWhenNotDraft() {
        var employee = createEmployee(1L);
        var declaration = createDeclaration(1L, employee, "2025-26", DeclarationStatus.SUBMITTED);
        var request = createRequest();
        when(investmentDeclarationRepository.findById(1L)).thenReturn(Optional.of(declaration));

        var ex = assertThrows(BadRequestException.class,
                () -> investmentDeclarationService.update(1L, request));
        assertTrue(ex.getMessage().contains("DRAFT"));
        verify(investmentDeclarationRepository, never()).save(any());
    }

    @Test
    void submit_shouldSetStatusToSubmitted() {
        var employee = createEmployee(1L);
        var declaration = createDeclaration(1L, employee, "2025-26", DeclarationStatus.DRAFT);
        when(investmentDeclarationRepository.findById(1L)).thenReturn(Optional.of(declaration));
        when(investmentDeclarationRepository.save(any(InvestmentDeclaration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = investmentDeclarationService.submit(1L);

        assertEquals("SUBMITTED", result.status());
        verify(investmentDeclarationRepository).save(any(InvestmentDeclaration.class));
    }

    @Test
    void submit_shouldThrowWhenNotDraft() {
        var employee = createEmployee(1L);
        var declaration = createDeclaration(1L, employee, "2025-26", DeclarationStatus.SUBMITTED);
        when(investmentDeclarationRepository.findById(1L)).thenReturn(Optional.of(declaration));

        var ex = assertThrows(BadRequestException.class,
                () -> investmentDeclarationService.submit(1L));
        assertTrue(ex.getMessage().contains("DRAFT"));
        verify(investmentDeclarationRepository, never()).save(any());
    }

    @Test
    void verify_shouldSetStatusToVerified() {
        var employee = createEmployee(1L);
        var declaration = createDeclaration(1L, employee, "2025-26", DeclarationStatus.SUBMITTED);
        when(investmentDeclarationRepository.findById(1L)).thenReturn(Optional.of(declaration));
        when(investmentDeclarationItemRepository.findByDeclarationId(1L)).thenReturn(declaration.getItems());
        when(investmentDeclarationRepository.save(any(InvestmentDeclaration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = investmentDeclarationService.verify(1L, 100L);

        assertEquals("VERIFIED", result.status());
        verify(investmentDeclarationRepository).save(any(InvestmentDeclaration.class));
    }

    @Test
    void verify_shouldThrowWhenNotSubmitted() {
        var employee = createEmployee(1L);
        var declaration = createDeclaration(1L, employee, "2025-26", DeclarationStatus.DRAFT);
        when(investmentDeclarationRepository.findById(1L)).thenReturn(Optional.of(declaration));

        var ex = assertThrows(BadRequestException.class,
                () -> investmentDeclarationService.verify(1L, 100L));
        assertTrue(ex.getMessage().contains("SUBMITTED"));
        verify(investmentDeclarationRepository, never()).save(any());
    }

    @Test
    void reject_shouldSetStatusToRejected() {
        var employee = createEmployee(1L);
        var declaration = createDeclaration(1L, employee, "2025-26", DeclarationStatus.SUBMITTED);
        when(investmentDeclarationRepository.findById(1L)).thenReturn(Optional.of(declaration));
        when(investmentDeclarationRepository.save(any(InvestmentDeclaration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = investmentDeclarationService.reject(1L, "Insufficient proof");

        assertEquals("REJECTED", result.status());
        verify(investmentDeclarationRepository).save(any(InvestmentDeclaration.class));
    }

    @Test
    void reject_shouldThrowWhenNotSubmitted() {
        var employee = createEmployee(1L);
        var declaration = createDeclaration(1L, employee, "2025-26", DeclarationStatus.DRAFT);
        when(investmentDeclarationRepository.findById(1L)).thenReturn(Optional.of(declaration));

        var ex = assertThrows(BadRequestException.class,
                () -> investmentDeclarationService.reject(1L, "Reason"));
        assertTrue(ex.getMessage().contains("SUBMITTED"));
        verify(investmentDeclarationRepository, never()).save(any());
    }

    @Test
    void submitProof_shouldUpdateItem() {
        var item = new InvestmentDeclarationItem();
        item.setId(1L);
        item.setSection("80C");
        item.setDescription("PPF");
        item.setDeclaredAmount(new BigDecimal("150000"));
        item.setProofStatus(ProofStatus.PENDING);
        when(investmentDeclarationItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(investmentDeclarationItemRepository.save(any(InvestmentDeclarationItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var request = new ProofSubmissionRequest(1L, "ppf_receipt.pdf", new BigDecimal("150000"));
        investmentDeclarationService.submitProof(request);

        verify(investmentDeclarationItemRepository).save(any(InvestmentDeclarationItem.class));
    }

    @Test
    void submitProof_shouldThrowWhenItemNotFound() {
        var request = new ProofSubmissionRequest(999L, "receipt.pdf", null);
        when(investmentDeclarationItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> investmentDeclarationService.submitProof(request));
        verify(investmentDeclarationItemRepository, never()).save(any());
    }

    @Test
    void verifyProof_shouldUpdateItem() {
        var item = new InvestmentDeclarationItem();
        item.setId(1L);
        item.setSection("80C");
        item.setDescription("PPF");
        item.setDeclaredAmount(new BigDecimal("150000"));
        item.setProofStatus(ProofStatus.SUBMITTED);
        when(investmentDeclarationItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(investmentDeclarationItemRepository.save(any(InvestmentDeclarationItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var request = new ProofVerificationRequest(1L, new BigDecimal("140000"), "VERIFIED", "Looks good");
        investmentDeclarationService.verifyProof(request);

        verify(investmentDeclarationItemRepository).save(any(InvestmentDeclarationItem.class));
    }

    @Test
    void delete_shouldDeleteDeclaration() {
        var employee = createEmployee(1L);
        var declaration = createDeclaration(1L, employee, "2025-26", DeclarationStatus.DRAFT);
        when(investmentDeclarationRepository.findById(1L)).thenReturn(Optional.of(declaration));

        investmentDeclarationService.delete(1L);

        verify(investmentDeclarationRepository).delete(declaration);
    }
}
