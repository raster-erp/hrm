package com.raster.hrm.attendancereport;

import com.raster.hrm.attendancereport.service.ReportEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private ReportEmailService reportEmailService;

    private void setFromAddress(String fromAddress) throws Exception {
        Field field = ReportEmailService.class.getDeclaredField("fromAddress");
        field.setAccessible(true);
        field.set(reportEmailService, fromAddress);
    }

    @Test
    void sendReportEmail_shouldSendEmailWithAttachment() throws Exception {
        setFromAddress("noreply@hrm.raster.com");
        var mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String[] recipients = {"admin@test.com", "hr@test.com"};
        byte[] data = "test,csv,data".getBytes();

        assertDoesNotThrow(() ->
                reportEmailService.sendReportEmail(recipients, "Test Report", "Body text",
                        "report.csv", data));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendReportEmail_withNullRecipients_shouldNotSend() throws Exception {
        assertDoesNotThrow(() ->
                reportEmailService.sendReportEmail(null, "Test", "Body", "file.csv", new byte[0]));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendReportEmail_withEmptyRecipients_shouldNotSend() throws Exception {
        assertDoesNotThrow(() ->
                reportEmailService.sendReportEmail(new String[0], "Test", "Body", "file.csv", new byte[0]));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendReportEmail_withSingleRecipient_shouldSend() throws Exception {
        setFromAddress("noreply@hrm.raster.com");
        var mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String[] recipients = {"admin@test.com"};
        byte[] data = "data".getBytes();

        reportEmailService.sendReportEmail(recipients, "Subject", "Body", "file.csv", data);

        verify(mailSender).send(any(MimeMessage.class));
    }
}
