package com.raster.hrm.leaveapplication.service;

import com.raster.hrm.leaveapplication.entity.LeaveApplication;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LeaveApplicationNotificationService {

    private static final Logger log = LoggerFactory.getLogger(LeaveApplicationNotificationService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public LeaveApplicationNotificationService(JavaMailSender mailSender,
                                                @Value("${hrm.mail.from:noreply@hrm.raster.com}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Async
    public void notifyApplicationSubmitted(LeaveApplication application) {
        String employeeEmail = application.getEmployee().getEmail();
        String employeeName = application.getEmployee().getFirstName() + " " + application.getEmployee().getLastName();
        String leaveTypeName = application.getLeaveType().getName();
        String subject = "Leave Application Submitted – " + leaveTypeName;
        String body = String.format(
                "Dear %s,%n%nYour %s application from %s to %s (%s day(s)) has been submitted and is pending approval.%n%nRegards,%nHRM System",
                employeeName, leaveTypeName, application.getFromDate(), application.getToDate(), application.getNumberOfDays());

        sendEmail(employeeEmail, subject, body);
    }

    @Async
    public void notifyApplicationApproved(LeaveApplication application) {
        String employeeEmail = application.getEmployee().getEmail();
        String employeeName = application.getEmployee().getFirstName() + " " + application.getEmployee().getLastName();
        String leaveTypeName = application.getLeaveType().getName();
        String subject = "Leave Application Approved – " + leaveTypeName;
        String body = String.format(
                "Dear %s,%n%nYour %s application from %s to %s (%s day(s)) has been approved by %s.%n%nRegards,%nHRM System",
                employeeName, leaveTypeName, application.getFromDate(), application.getToDate(),
                application.getNumberOfDays(), application.getApprovedBy());

        sendEmail(employeeEmail, subject, body);
    }

    @Async
    public void notifyApplicationRejected(LeaveApplication application) {
        String employeeEmail = application.getEmployee().getEmail();
        String employeeName = application.getEmployee().getFirstName() + " " + application.getEmployee().getLastName();
        String leaveTypeName = application.getLeaveType().getName();
        String subject = "Leave Application Rejected – " + leaveTypeName;
        String remarks = application.getRemarks() != null ? application.getRemarks() : "No remarks provided";
        String body = String.format(
                "Dear %s,%n%nYour %s application from %s to %s (%s day(s)) has been rejected.%nRemarks: %s%n%nRegards,%nHRM System",
                employeeName, leaveTypeName, application.getFromDate(), application.getToDate(),
                application.getNumberOfDays(), remarks);

        sendEmail(employeeEmail, subject, body);
    }

    @Async
    public void notifyApplicationCancelled(LeaveApplication application) {
        String employeeEmail = application.getEmployee().getEmail();
        String employeeName = application.getEmployee().getFirstName() + " " + application.getEmployee().getLastName();
        String leaveTypeName = application.getLeaveType().getName();
        String subject = "Leave Application Cancelled – " + leaveTypeName;
        String body = String.format(
                "Dear %s,%n%nYour %s application from %s to %s (%s day(s)) has been cancelled.%n%nRegards,%nHRM System",
                employeeName, leaveTypeName, application.getFromDate(), application.getToDate(),
                application.getNumberOfDays());

        sendEmail(employeeEmail, subject, body);
    }

    void sendEmail(String recipient, String subject, String body) {
        if (recipient == null || recipient.isBlank()) {
            log.warn("No recipient email for leave notification: {}", subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body);

            mailSender.send(message);
            log.info("Sent leave notification '{}' to {}", subject, recipient);
        } catch (Exception e) {
            log.error("Failed to send leave notification '{}' to {}: {}", subject, recipient, e.getMessage());
        }
    }
}
