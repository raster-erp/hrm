package com.raster.hrm.compoff;

import com.raster.hrm.compoff.dto.CompOffApprovalRequest;
import com.raster.hrm.compoff.dto.CompOffBalanceResponse;
import com.raster.hrm.compoff.dto.CompOffCreditRequest;
import com.raster.hrm.compoff.dto.CompOffCreditResponse;
import com.raster.hrm.compoff.entity.CompOffStatus;
import com.raster.hrm.compoff.service.CompOffCreditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/compoff/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/compoff/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CompOffCreditIntegrationTest {

    @Autowired
    private CompOffCreditService compOffCreditService;

    private static final Long EMPLOYEE_ID = 9901L;

    @Test
    void fullCompOffWorkflow() {
        // Step 1: Create comp-off request
        var request = new CompOffCreditRequest(
                EMPLOYEE_ID, LocalDate.of(2025, 6, 15),
                "Worked on weekend for release", new BigDecimal("8.00"), "Integration test");
        CompOffCreditResponse created = compOffCreditService.createRequest(request);
        assertNotNull(created.id());
        assertEquals("PENDING", created.status());
        assertEquals(LocalDate.of(2025, 6, 15), created.workedDate());
        assertEquals(LocalDate.of(2025, 6, 15).plusDays(90), created.expiryDate());

        // Step 2: Check balance
        CompOffBalanceResponse balance = compOffCreditService.getBalance(EMPLOYEE_ID);
        assertEquals(1, balance.totalCredits());
        assertEquals(1, balance.pending());
        assertEquals(0, balance.approved());

        // Step 3: Approve
        var approvalRequest = new CompOffApprovalRequest(CompOffStatus.APPROVED, "Admin", "Approved");
        CompOffCreditResponse approved = compOffCreditService.approve(created.id(), approvalRequest);
        assertEquals("APPROVED", approved.status());
        assertEquals("Admin", approved.approvedBy());
        assertNotNull(approved.approvedAt());

        // Step 4: Verify balance updated
        CompOffBalanceResponse updatedBalance = compOffCreditService.getBalance(EMPLOYEE_ID);
        assertEquals(1, updatedBalance.approved());
        assertEquals(0, updatedBalance.pending());
        assertEquals(1, updatedBalance.availableForUse());
    }

    @Test
    void queryCompOffs() {
        // Create a comp-off
        var request = new CompOffCreditRequest(
                EMPLOYEE_ID, LocalDate.of(2025, 7, 20),
                "Worked on holiday", new BigDecimal("6.00"), null);
        CompOffCreditResponse created = compOffCreditService.createRequest(request);

        // Query by employee
        var byEmployee = compOffCreditService.getByEmployee(EMPLOYEE_ID, PageRequest.of(0, 10));
        assertEquals(1, byEmployee.getTotalElements());

        // Query by status
        var byStatus = compOffCreditService.getByStatus(CompOffStatus.PENDING, PageRequest.of(0, 10));
        assertTrue(byStatus.getTotalElements() >= 1);

        // Query all
        var all = compOffCreditService.getAll(PageRequest.of(0, 10));
        assertTrue(all.getTotalElements() >= 1);

        // Get by id
        var byId = compOffCreditService.getById(created.id());
        assertEquals(created.id(), byId.id());
    }

    @Test
    void rejectCompOff() {
        var request = new CompOffCreditRequest(
                EMPLOYEE_ID, LocalDate.of(2025, 8, 10),
                "Worked on weekend", null, null);
        CompOffCreditResponse created = compOffCreditService.createRequest(request);

        var rejectRequest = new CompOffApprovalRequest(
                CompOffStatus.REJECTED, "Manager", "Not a valid comp-off day");
        CompOffCreditResponse rejected = compOffCreditService.approve(created.id(), rejectRequest);

        assertEquals("REJECTED", rejected.status());
        assertEquals("Not a valid comp-off day", rejected.remarks());
    }
}
