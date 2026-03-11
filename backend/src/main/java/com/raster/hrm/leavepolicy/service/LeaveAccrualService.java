package com.raster.hrm.leavepolicy.service;

import com.raster.hrm.leavepolicyassignment.entity.LeavePolicyAssignment;
import com.raster.hrm.leavepolicyassignment.repository.LeavePolicyAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class LeaveAccrualService {

    private static final Logger log = LoggerFactory.getLogger(LeaveAccrualService.class);

    private final LeavePolicyAssignmentRepository assignmentRepository;

    public LeaveAccrualService(LeavePolicyAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    // Runs daily at 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void processAccruals() {
        var today = LocalDate.now();
        log.info("Starting leave accrual processing for date: {}", today);

        var activeAssignments = assignmentRepository.findByActive(true);
        int processed = 0;

        for (var assignment : activeAssignments) {
            if (isAccrualDue(assignment, today)) {
                processAccrual(assignment, today);
                processed++;
            }
        }

        log.info("Leave accrual processing completed. Processed {} assignments.", processed);
    }

    // Package-private for unit testing
    boolean isAccrualDue(LeavePolicyAssignment assignment, LocalDate date) {
        if (assignment.getEffectiveFrom().isAfter(date)) {
            return false;
        }
        if (assignment.getEffectiveTo() != null && assignment.getEffectiveTo().isBefore(date)) {
            return false;
        }

        var policy = assignment.getLeavePolicy();
        var frequency = policy.getAccrualFrequency();

        return switch (frequency) {
            case MONTHLY -> date.getDayOfMonth() == 1;
            case QUARTERLY -> date.getDayOfMonth() == 1 && (date.getMonthValue() - 1) % 3 == 0;
            case ANNUAL -> date.getDayOfMonth() == 1 && date.getMonthValue() == 1;
        };
    }

    private void processAccrual(LeavePolicyAssignment assignment, LocalDate date) {
        var policy = assignment.getLeavePolicy();
        var leaveType = policy.getLeaveType();
        var accrualDays = policy.getAccrualDays();

        log.info("Accrual due - Policy: '{}', LeaveType: '{}', Days: {}, AssignmentType: {}, EffectiveFrom: {}, Date: {}",
                policy.getName(),
                leaveType.getName(),
                accrualDays,
                assignment.getAssignmentType(),
                assignment.getEffectiveFrom(),
                date);
    }
}
