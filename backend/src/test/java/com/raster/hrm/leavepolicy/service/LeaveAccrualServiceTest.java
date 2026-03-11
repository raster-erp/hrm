package com.raster.hrm.leavepolicy.service;

import com.raster.hrm.leavepolicy.entity.AccrualFrequency;
import com.raster.hrm.leavepolicy.entity.LeavePolicy;
import com.raster.hrm.leavepolicy.service.LeaveAccrualService;
import com.raster.hrm.leavepolicyassignment.entity.AssignmentType;
import com.raster.hrm.leavepolicyassignment.entity.LeavePolicyAssignment;
import com.raster.hrm.leavepolicyassignment.repository.LeavePolicyAssignmentRepository;
import com.raster.hrm.leavetype.entity.LeaveType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveAccrualServiceTest {

    @Mock
    private LeavePolicyAssignmentRepository assignmentRepository;

    @InjectMocks
    private LeaveAccrualService leaveAccrualService;

    private LeaveType createLeaveType() {
        var leaveType = new LeaveType();
        leaveType.setId(1L);
        leaveType.setName("Annual Leave");
        leaveType.setCode("AL");
        return leaveType;
    }

    private LeavePolicy createPolicy(AccrualFrequency frequency) {
        var policy = new LeavePolicy();
        policy.setId(1L);
        policy.setName("Standard Policy");
        policy.setLeaveType(createLeaveType());
        policy.setAccrualFrequency(frequency);
        policy.setAccrualDays(new BigDecimal("1.25"));
        policy.setActive(true);
        return policy;
    }

    private LeavePolicyAssignment createAssignment(AccrualFrequency frequency,
                                                    LocalDate effectiveFrom,
                                                    LocalDate effectiveTo) {
        var assignment = new LeavePolicyAssignment();
        assignment.setId(1L);
        assignment.setLeavePolicy(createPolicy(frequency));
        assignment.setAssignmentType(AssignmentType.DEPARTMENT);
        assignment.setDepartmentId(1L);
        assignment.setEffectiveFrom(effectiveFrom);
        assignment.setEffectiveTo(effectiveTo);
        assignment.setActive(true);
        return assignment;
    }

    @Test
    void isAccrualDue_monthly_firstOfMonth_returnsTrue() {
        var assignment = createAssignment(AccrualFrequency.MONTHLY,
                LocalDate.of(2024, 1, 1), null);
        var date = LocalDate.of(2024, 6, 1);

        assertTrue(leaveAccrualService.isAccrualDue(assignment, date));
    }

    @Test
    void isAccrualDue_monthly_otherDay_returnsFalse() {
        var assignment = createAssignment(AccrualFrequency.MONTHLY,
                LocalDate.of(2024, 1, 1), null);
        var date = LocalDate.of(2024, 6, 15);

        assertFalse(leaveAccrualService.isAccrualDue(assignment, date));
    }

    @Test
    void isAccrualDue_quarterly_jan1_returnsTrue() {
        var assignment = createAssignment(AccrualFrequency.QUARTERLY,
                LocalDate.of(2023, 1, 1), null);
        var date = LocalDate.of(2024, 1, 1);

        assertTrue(leaveAccrualService.isAccrualDue(assignment, date));
    }

    @Test
    void isAccrualDue_quarterly_apr1_returnsTrue() {
        var assignment = createAssignment(AccrualFrequency.QUARTERLY,
                LocalDate.of(2023, 1, 1), null);
        var date = LocalDate.of(2024, 4, 1);

        assertTrue(leaveAccrualService.isAccrualDue(assignment, date));
    }

    @Test
    void isAccrualDue_quarterly_jul1_returnsTrue() {
        var assignment = createAssignment(AccrualFrequency.QUARTERLY,
                LocalDate.of(2023, 1, 1), null);
        var date = LocalDate.of(2024, 7, 1);

        assertTrue(leaveAccrualService.isAccrualDue(assignment, date));
    }

    @Test
    void isAccrualDue_quarterly_oct1_returnsTrue() {
        var assignment = createAssignment(AccrualFrequency.QUARTERLY,
                LocalDate.of(2023, 1, 1), null);
        var date = LocalDate.of(2024, 10, 1);

        assertTrue(leaveAccrualService.isAccrualDue(assignment, date));
    }

    @Test
    void isAccrualDue_quarterly_feb1_returnsFalse() {
        var assignment = createAssignment(AccrualFrequency.QUARTERLY,
                LocalDate.of(2023, 1, 1), null);
        var date = LocalDate.of(2024, 2, 1);

        assertFalse(leaveAccrualService.isAccrualDue(assignment, date));
    }

    @Test
    void isAccrualDue_annual_jan1_returnsTrue() {
        var assignment = createAssignment(AccrualFrequency.ANNUAL,
                LocalDate.of(2023, 1, 1), null);
        var date = LocalDate.of(2024, 1, 1);

        assertTrue(leaveAccrualService.isAccrualDue(assignment, date));
    }

    @Test
    void isAccrualDue_annual_otherDate_returnsFalse() {
        var assignment = createAssignment(AccrualFrequency.ANNUAL,
                LocalDate.of(2023, 1, 1), null);
        var date = LocalDate.of(2024, 6, 1);

        assertFalse(leaveAccrualService.isAccrualDue(assignment, date));
    }

    @Test
    void isAccrualDue_beforeEffectiveFrom_returnsFalse() {
        var assignment = createAssignment(AccrualFrequency.MONTHLY,
                LocalDate.of(2024, 6, 1), null);
        var date = LocalDate.of(2024, 5, 1);

        assertFalse(leaveAccrualService.isAccrualDue(assignment, date));
    }

    @Test
    void isAccrualDue_afterEffectiveTo_returnsFalse() {
        var assignment = createAssignment(AccrualFrequency.MONTHLY,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 5, 31));
        var date = LocalDate.of(2024, 7, 1);

        assertFalse(leaveAccrualService.isAccrualDue(assignment, date));
    }

    @Test
    void processAccruals_processesActiveAssignments() {
        var assignment = createAssignment(AccrualFrequency.MONTHLY,
                LocalDate.of(2024, 1, 1), null);

        when(assignmentRepository.findByActive(true)).thenReturn(List.of(assignment));

        leaveAccrualService.processAccruals();

        verify(assignmentRepository).findByActive(true);
    }

    @Test
    void processAccruals_skipsNonDueAssignments() {
        var futureAssignment = createAssignment(AccrualFrequency.ANNUAL,
                LocalDate.of(2099, 1, 1), null);

        when(assignmentRepository.findByActive(true)).thenReturn(List.of(futureAssignment));

        leaveAccrualService.processAccruals();

        verify(assignmentRepository).findByActive(true);
    }

    @Test
    void processAccruals_handlesEmptyAssignments() {
        when(assignmentRepository.findByActive(true)).thenReturn(List.of());

        leaveAccrualService.processAccruals();

        verify(assignmentRepository).findByActive(true);
    }
}
