package com.raster.hrm.attendancereport.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class ReportEmailService {

    private static final Logger log = LoggerFactory.getLogger(ReportEmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public ReportEmailService(JavaMailSender mailSender,
                               @Value("${hrm.mail.from:noreply@hrm.raster.com}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendReportEmail(String[] recipients, String subject, String body,
                                 String fileName, byte[] attachmentData) throws MessagingException {
        if (recipients == null || recipients.length == 0) {
            log.warn("No recipients specified for report email: {}", subject);
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(recipients);
        helper.setSubject(subject);
        helper.setText(body);
        helper.addAttachment(fileName, new ByteArrayResource(attachmentData));

        mailSender.send(message);
        log.info("Sent report email '{}' to {} recipient(s)", subject, recipients.length);
    }
}
