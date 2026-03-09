package com.raster.hrm.promotion;

import com.raster.hrm.designation.entity.Designation;
import com.raster.hrm.designation.repository.DesignationRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.promotion.dto.PromotionRequest;
import com.raster.hrm.promotion.entity.Promotion;
import com.raster.hrm.promotion.entity.PromotionStatus;
import com.raster.hrm.promotion.repository.PromotionRepository;
import com.raster.hrm.promotion.service.PromotionService;
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
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DesignationRepository designationRepository;

    @InjectMocks
    private PromotionService promotionService;

    private Employee createEmployee(Long id, String code, String firstName, String lastName) {
        var employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(code);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "@test.com");
        return employee;
    }

    private Designation createDesignation(Long id, String title, String code) {
        var designation = new Designation();
        designation.setId(id);
        designation.setTitle(title);
        designation.setCode(code);
        return designation;
    }

    private Promotion createPromotion(Long id, Employee employee, Designation oldDesignation, Designation newDesignation) {
        var promotion = new Promotion();
        promotion.setId(id);
        promotion.setEmployee(employee);
        promotion.setOldDesignation(oldDesignation);
        promotion.setNewDesignation(newDesignation);
        promotion.setOldGrade("G5");
        promotion.setNewGrade("G6");
        promotion.setEffectiveDate(LocalDate.of(2024, 6, 1));
        promotion.setStatus(PromotionStatus.PENDING);
        promotion.setReason("Outstanding performance");
        promotion.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        promotion.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return promotion;
    }

    private PromotionRequest createRequest() {
        return new PromotionRequest(
                1L, 1L, 2L,
                "G5", "G6",
                LocalDate.of(2024, 6, 1),
                "Outstanding performance"
        );
    }

    @Test
    void getAll_shouldReturnPageOfPromotions() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotions = List.of(
                createPromotion(1L, employee, oldDesig, newDesig),
                createPromotion(2L, employee, oldDesig, newDesig)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(promotions, pageable, 2);
        when(promotionRepository.findAll(pageable)).thenReturn(page);

        var result = promotionService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).employeeName());
        verify(promotionRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<Promotion>(List.of(), pageable, 0);
        when(promotionRepository.findAll(pageable)).thenReturn(page);

        var result = promotionService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnPromotion() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

        var result = promotionService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("Software Engineer", result.oldDesignationTitle());
        assertEquals("Senior Software Engineer", result.newDesignationTitle());
        assertEquals("G5", result.oldGrade());
        assertEquals("G6", result.newGrade());
        assertEquals("PENDING", result.status());
        assertEquals("Outstanding performance", result.reason());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(promotionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> promotionService.getById(999L));
    }

    @Test
    void getByEmployeeId_shouldReturnPromotions() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotions = List.of(
                createPromotion(1L, employee, oldDesig, newDesig),
                createPromotion(2L, employee, oldDesig, newDesig)
        );
        when(promotionRepository.findByEmployeeId(1L)).thenReturn(promotions);

        var result = promotionService.getByEmployeeId(1L);

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).employeeName());
    }

    @Test
    void getByEmployeeId_shouldReturnEmptyListWhenNoPromotions() {
        when(promotionRepository.findByEmployeeId(1L)).thenReturn(List.of());

        var result = promotionService.getByEmployeeId(1L);

        assertEquals(0, result.size());
    }

    @Test
    void getPendingPromotions_shouldReturnPendingPromotions() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        when(promotionRepository.findByStatus(PromotionStatus.PENDING)).thenReturn(List.of(promotion));

        var result = promotionService.getPendingPromotions();

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).status());
    }

    @Test
    void getPendingPromotions_shouldReturnEmptyListWhenNonePending() {
        when(promotionRepository.findByStatus(PromotionStatus.PENDING)).thenReturn(List.of());

        var result = promotionService.getPendingPromotions();

        assertEquals(0, result.size());
    }

    @Test
    void create_shouldCreateAndReturnPromotion() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var request = createRequest();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(designationRepository.findById(1L)).thenReturn(Optional.of(oldDesig));
        when(designationRepository.findById(2L)).thenReturn(Optional.of(newDesig));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> {
            Promotion p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        var result = promotionService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals("Software Engineer", result.oldDesignationTitle());
        assertEquals("Senior Software Engineer", result.newDesignationTitle());
        assertEquals("G5", result.oldGrade());
        assertEquals("G6", result.newGrade());
        assertEquals("PENDING", result.status());
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> promotionService.create(request));
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenOldDesignationNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(designationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> promotionService.create(request));
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenNewDesignationNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var request = createRequest();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(designationRepository.findById(1L)).thenReturn(Optional.of(oldDesig));
        when(designationRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> promotionService.create(request));
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void create_shouldCreateWithoutDesignations() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var request = new PromotionRequest(
                1L, null, null,
                "G5", "G6",
                LocalDate.of(2024, 6, 1),
                "Grade promotion"
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> {
            Promotion p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        var result = promotionService.create(request);

        assertNotNull(result);
        assertNull(result.oldDesignationId());
        assertNull(result.newDesignationId());
        assertEquals("G5", result.oldGrade());
        assertEquals("G6", result.newGrade());
    }

    @Test
    void approve_shouldApprovePromotion() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        promotion.setStatus(PromotionStatus.PENDING);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = promotionService.approve(1L, 2L);

        assertEquals("APPROVED", result.status());
        assertEquals(2L, result.approvedById());
        assertEquals("Jane Smith", result.approvedByName());
        assertNotNull(result.approvedAt());
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void approve_shouldThrowWhenPromotionNotFound() {
        when(promotionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> promotionService.approve(999L, 2L));
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenNotPending() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        promotion.setStatus(PromotionStatus.APPROVED);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

        assertThrows(BadRequestException.class,
                () -> promotionService.approve(1L, 2L));
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void approve_shouldThrowWhenApproverNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> promotionService.approve(1L, 2L));
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void reject_shouldRejectPromotion() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        promotion.setStatus(PromotionStatus.PENDING);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = promotionService.reject(1L, 2L);

        assertEquals("REJECTED", result.status());
        assertEquals(2L, result.approvedById());
        assertEquals("Jane Smith", result.approvedByName());
        assertNotNull(result.approvedAt());
    }

    @Test
    void reject_shouldThrowWhenPromotionNotFound() {
        when(promotionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> promotionService.reject(999L, 2L));
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void reject_shouldThrowWhenNotPending() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        promotion.setStatus(PromotionStatus.EXECUTED);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

        assertThrows(BadRequestException.class,
                () -> promotionService.reject(1L, 2L));
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void reject_shouldThrowWhenApproverNotFound() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> promotionService.reject(1L, 2L));
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void execute_shouldExecutePromotionAndUpdateEmployeeDesignation() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        promotion.setStatus(PromotionStatus.APPROVED);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = promotionService.execute(1L);

        assertEquals("EXECUTED", result.status());
        assertEquals(newDesig, employee.getDesignation());
        verify(employeeRepository).save(employee);
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void execute_shouldExecuteWithoutUpdatingDesignationWhenNewDesignationNull() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var promotion = createPromotion(1L, employee, null, null);
        promotion.setStatus(PromotionStatus.APPROVED);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = promotionService.execute(1L);

        assertEquals("EXECUTED", result.status());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenPromotionNotFound() {
        when(promotionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> promotionService.execute(999L));
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenNotApproved() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        promotion.setStatus(PromotionStatus.PENDING);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

        assertThrows(BadRequestException.class,
                () -> promotionService.execute(1L));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenRejected() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        promotion.setStatus(PromotionStatus.REJECTED);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

        assertThrows(BadRequestException.class,
                () -> promotionService.execute(1L));
    }

    @Test
    void delete_shouldDeletePendingPromotion() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        promotion.setStatus(PromotionStatus.PENDING);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

        promotionService.delete(1L);

        verify(promotionRepository).delete(promotion);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(promotionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> promotionService.delete(999L));
        verify(promotionRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowWhenNotPending() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        promotion.setStatus(PromotionStatus.APPROVED);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

        assertThrows(BadRequestException.class,
                () -> promotionService.delete(1L));
        verify(promotionRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowWhenExecuted() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        promotion.setStatus(PromotionStatus.EXECUTED);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

        assertThrows(BadRequestException.class,
                () -> promotionService.delete(1L));
        verify(promotionRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

        var result = promotionService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals(1L, result.employeeId());
        assertEquals("EMP001", result.employeeCode());
        assertEquals("John Doe", result.employeeName());
        assertEquals(1L, result.oldDesignationId());
        assertEquals("Software Engineer", result.oldDesignationTitle());
        assertEquals(2L, result.newDesignationId());
        assertEquals("Senior Software Engineer", result.newDesignationTitle());
        assertEquals("G5", result.oldGrade());
        assertEquals("G6", result.newGrade());
        assertEquals(LocalDate.of(2024, 6, 1), result.effectiveDate());
        assertEquals("PENDING", result.status());
        assertEquals("Outstanding performance", result.reason());
        assertNull(result.approvedById());
        assertNull(result.approvedByName());
        assertNull(result.approvedAt());
    }

    @Test
    void getById_shouldMapNullDesignationsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var promotion = createPromotion(1L, employee, null, null);
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

        var result = promotionService.getById(1L);

        assertNull(result.oldDesignationId());
        assertNull(result.oldDesignationTitle());
        assertNull(result.newDesignationId());
        assertNull(result.newDesignationTitle());
    }

    @Test
    void create_shouldMapAllFieldsCorrectly() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var request = new PromotionRequest(
                1L, 1L, 2L,
                "G3", "G4",
                LocalDate.of(2024, 12, 1),
                "Exceeds expectations"
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(designationRepository.findById(1L)).thenReturn(Optional.of(oldDesig));
        when(designationRepository.findById(2L)).thenReturn(Optional.of(newDesig));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> {
            Promotion p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        var result = promotionService.create(request);

        assertEquals("G3", result.oldGrade());
        assertEquals("G4", result.newGrade());
        assertEquals(LocalDate.of(2024, 12, 1), result.effectiveDate());
        assertEquals("Exceeds expectations", result.reason());
        assertEquals("PENDING", result.status());
    }

    @Test
    void fullWorkflow_createApproveExecute() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        promotion.setStatus(PromotionStatus.PENDING);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var approvedResult = promotionService.approve(1L, 2L);
        assertEquals("APPROVED", approvedResult.status());

        var executedResult = promotionService.execute(1L);
        assertEquals("EXECUTED", executedResult.status());
        verify(employeeRepository).save(employee);
    }

    @Test
    void fullWorkflow_createReject() {
        var employee = createEmployee(1L, "EMP001", "John", "Doe");
        var approver = createEmployee(2L, "EMP002", "Jane", "Smith");
        var oldDesig = createDesignation(1L, "Software Engineer", "SE");
        var newDesig = createDesignation(2L, "Senior Software Engineer", "SSE");
        var promotion = createPromotion(1L, employee, oldDesig, newDesig);
        promotion.setStatus(PromotionStatus.PENDING);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var rejectedResult = promotionService.reject(1L, 2L);
        assertEquals("REJECTED", rejectedResult.status());
    }
}
