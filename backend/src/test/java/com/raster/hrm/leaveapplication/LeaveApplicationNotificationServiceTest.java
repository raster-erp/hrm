package com.raster.hrm.leaveapplication;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.leaveapplication.entity.LeaveApplication;
import com.raster.hrm.leaveapplication.entity.LeaveApplicationStatus;
import com.raster.hrm.leaveapplication.service.LeaveApplicationNotificationService;
import com.raster.hrm.leavetype.entity.LeaveType;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveApplicationNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private LeaveApplicationNotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new LeaveApplicationNotificationService(mailSender, "noreply@hrm.raster.com");
    }

    private Employee createEmployee() {
        var employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@test.com");
        return employee;
    }

    private LeaveType createLeaveType() {
        var leaveType = new LeaveType();
        leaveType.setId(1L);
        leaveType.setName("Casual Leave");
        leaveType.setCode("CL");
        return leaveType;
    }

    private LeaveApplication createApplication(LeaveApplicationStatus status) {
        var app = new LeaveApplication();
        app.setId(1L);
        app.setEmployee(createEmployee());
        app.setLeaveType(createLeaveType());
        app.setFromDate(LocalDate.of(2025, 6, 1));
        app.setToDate(LocalDate.of(2025, 6, 3));
        app.setNumberOfDays(new BigDecimal("3.00"));
        app.setReason("Personal work");
        app.setStatus(status);
        app.setApprovedBy("Manager");
        app.setRemarks("Some remarks");
        return app;
    }

    @Test
    void notifyApplicationSubmitted_shouldSendEmail() {
        var mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        var application = createApplication(LeaveApplicationStatus.PENDING);

        assertDoesNotThrow(() -> notificationService.notifyApplicationSubmitted(application));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void notifyApplicationApproved_shouldSendEmail() {
        var mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        var application = createApplication(LeaveApplicationStatus.APPROVED);

        assertDoesNotThrow(() -> notificationService.notifyApplicationApproved(application));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void notifyApplicationRejected_shouldSendEmail() {
        var mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        var application = createApplication(LeaveApplicationStatus.REJECTED);

        assertDoesNotThrow(() -> notificationService.notifyApplicationRejected(application));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void notifyApplicationRejected_shouldIncludeDefaultRemarksWhenNull() {
        var mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        var application = createApplication(LeaveApplicationStatus.REJECTED);
        application.setRemarks(null);

        assertDoesNotThrow(() -> notificationService.notifyApplicationRejected(application));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void notifyApplicationCancelled_shouldSendEmail() {
        var mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        var application = createApplication(LeaveApplicationStatus.CANCELLED);

        assertDoesNotThrow(() -> notificationService.notifyApplicationCancelled(application));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void notifyApplicationSubmitted_withNullEmail_shouldNotSend() {
        var application = createApplication(LeaveApplicationStatus.PENDING);
        application.getEmployee().setEmail(null);

        assertDoesNotThrow(() -> notificationService.notifyApplicationSubmitted(application));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void notifyApplicationSubmitted_withBlankEmail_shouldNotSend() {
        var application = createApplication(LeaveApplicationStatus.PENDING);
        application.getEmployee().setEmail("  ");

        assertDoesNotThrow(() -> notificationService.notifyApplicationSubmitted(application));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void notifyApplicationApproved_shouldHandleMailException() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail server unavailable"));

        var application = createApplication(LeaveApplicationStatus.APPROVED);

        assertDoesNotThrow(() -> notificationService.notifyApplicationApproved(application));
    }
}
