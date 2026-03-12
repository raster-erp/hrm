package com.raster.hrm.leaveencashment;

import com.raster.hrm.leavebalance.entity.LeaveBalance;
import com.raster.hrm.leavebalance.repository.LeaveBalanceRepository;
import com.raster.hrm.leaveencashment.dto.EncashmentEligibilityResponse;
import com.raster.hrm.leaveencashment.dto.LeaveEncashmentApprovalRequest;
import com.raster.hrm.leaveencashment.dto.LeaveEncashmentRequest;
import com.raster.hrm.leaveencashment.dto.LeaveEncashmentResponse;
import com.raster.hrm.leaveencashment.entity.EncashmentStatus;
import com.raster.hrm.leaveencashment.service.LeaveEncashmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/leaveencashment/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/leaveencashment/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class LeaveEncashmentIntegrationTest {

    @Autowired
    private LeaveEncashmentService leaveEncashmentService;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    private static final Long EMPLOYEE_ID = 9801L;
    private static final Long ENCASHABLE_TYPE_ID = 9801L;
    private static final Long NON_ENCASHABLE_TYPE_ID = 9802L;

    @Test
    void fullEncashmentWorkflow() {
        int year = java.time.LocalDate.now().getYear();

        // Step 1: Check eligibility
        EncashmentEligibilityResponse eligibility = leaveEncashmentService.checkEligibility(
                EMPLOYEE_ID, ENCASHABLE_TYPE_ID, year);
        assertTrue(eligibility.eligible());
        assertEquals(new BigDecimal("10.00"), eligibility.maxEncashableDays());

        // Step 2: Create request
        var request = new LeaveEncashmentRequest(
                EMPLOYEE_ID, ENCASHABLE_TYPE_ID, new BigDecimal("5.00"), "Integration test");
        LeaveEncashmentResponse created = leaveEncashmentService.createRequest(request);
        assertNotNull(created.id());
        assertEquals("PENDING", created.status());
        assertEquals(0, new BigDecimal("5000.00").compareTo(created.totalAmount()));

        // Step 3: Approve
        var approvalRequest = new LeaveEncashmentApprovalRequest(
                EncashmentStatus.APPROVED, "Admin", "Approved");
        LeaveEncashmentResponse approved = leaveEncashmentService.approve(created.id(), approvalRequest);
        assertEquals("APPROVED", approved.status());

        // Step 4: Verify balance updated
        LeaveBalance updatedBalance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(EMPLOYEE_ID, ENCASHABLE_TYPE_ID, year)
                .orElseThrow();
        assertEquals(0, new BigDecimal("5.00").compareTo(updatedBalance.getEncashed()));
        assertEquals(0, new BigDecimal("10.00").compareTo(updatedBalance.getAvailable()));

        // Step 5: Mark as paid
        LeaveEncashmentResponse paid = leaveEncashmentService.markAsPaid(created.id(), "Finance");
        assertEquals("PAID", paid.status());
    }

    @Test
    void eligibilityCheck_notEncashableType() {
        int year = java.time.LocalDate.now().getYear();
        EncashmentEligibilityResponse eligibility = leaveEncashmentService.checkEligibility(
                EMPLOYEE_ID, NON_ENCASHABLE_TYPE_ID, year);

        assertFalse(eligibility.eligible());
    }

    @Test
    void queryEncashments() {
        int year = java.time.LocalDate.now().getYear();

        // Create an encashment
        var request = new LeaveEncashmentRequest(
                EMPLOYEE_ID, ENCASHABLE_TYPE_ID, new BigDecimal("3.00"), null);
        LeaveEncashmentResponse created = leaveEncashmentService.createRequest(request);

        // Query by employee
        var byEmployee = leaveEncashmentService.getByEmployee(EMPLOYEE_ID, PageRequest.of(0, 10));
        assertEquals(1, byEmployee.getTotalElements());

        // Query by status
        var byStatus = leaveEncashmentService.getByStatus(EncashmentStatus.PENDING, PageRequest.of(0, 10));
        assertTrue(byStatus.getTotalElements() >= 1);

        // Query all
        var all = leaveEncashmentService.getAll(PageRequest.of(0, 10));
        assertTrue(all.getTotalElements() >= 1);

        // Get by id
        var byId = leaveEncashmentService.getById(created.id());
        assertEquals(created.id(), byId.id());
    }
}
