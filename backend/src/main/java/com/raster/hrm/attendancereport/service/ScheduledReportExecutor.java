package com.raster.hrm.attendancereport.service;

import com.raster.hrm.attendancereport.entity.ReportSchedule;
import com.raster.hrm.attendancereport.entity.ScheduleFrequency;
import com.raster.hrm.attendancereport.repository.ReportScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "hrm.report.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class ScheduledReportExecutor {

    private static final Logger log = LoggerFactory.getLogger(ScheduledReportExecutor.class);

    private final ReportScheduleRepository reportScheduleRepository;
    private final AttendanceReportService attendanceReportService;
    private final ReportEmailService reportEmailService;

    public ScheduledReportExecutor(ReportScheduleRepository reportScheduleRepository,
                                    AttendanceReportService attendanceReportService,
                                    ReportEmailService reportEmailService) {
        this.reportScheduleRepository = reportScheduleRepository;
        this.attendanceReportService = attendanceReportService;
        this.reportEmailService = reportEmailService;
    }

    @Scheduled(fixedDelayString = "${hrm.report.schedule.interval-ms:60000}")
    @Transactional
    public void executeDueSchedules() {
        var now = LocalDateTime.now();
        List<ReportSchedule> dueSchedules = reportScheduleRepository
                .findByActiveTrueAndNextRunAtBefore(now);

        if (dueSchedules.isEmpty()) {
            return;
        }

        log.info("Found {} due report schedule(s) to execute", dueSchedules.size());

        for (var schedule : dueSchedules) {
            executeSchedule(schedule, now);
        }
    }

    public void executeSchedule(ReportSchedule schedule, LocalDateTime now) {
        try {
            log.info("Executing scheduled report: {} (id={})", schedule.getReportName(), schedule.getId());

            var params = buildReportParams(schedule, now);
            var reportType = schedule.getReportType().name();
            byte[] reportData = attendanceReportService.exportReportAsCsv(reportType, params);

            String fileName = reportType.toLowerCase() + "_report_" + LocalDate.now() + ".csv";

            if (schedule.getRecipients() != null && !schedule.getRecipients().isBlank()) {
                String[] recipients = parseRecipients(schedule.getRecipients());
                String subject = "Scheduled Report: " + schedule.getReportName();
                String body = "Please find attached the scheduled report: " + schedule.getReportName()
                        + "\nGenerated at: " + now;

                reportEmailService.sendReportEmail(recipients, subject, body, fileName, reportData);
            }

            schedule.setLastRunAt(now);
            schedule.setNextRunAt(calculateNextRunAt(now, schedule.getFrequency()));
            reportScheduleRepository.save(schedule);

            log.info("Successfully executed scheduled report: {} (id={})",
                    schedule.getReportName(), schedule.getId());
        } catch (Exception e) {
            log.error("Failed to execute scheduled report: {} (id={})",
                    schedule.getReportName(), schedule.getId(), e);
        }
    }

    public Map<String, String> buildReportParams(ReportSchedule schedule, LocalDateTime now) {
        var params = new HashMap<String, String>();
        var today = now.toLocalDate();

        if (schedule.getDepartment() != null) {
            params.put("departmentId", String.valueOf(schedule.getDepartment().getId()));
        }

        switch (schedule.getReportType()) {
            case DAILY_MUSTER -> params.put("date", today.minusDays(1).toString());
            case MONTHLY_SUMMARY -> {
                var previousMonth = today.minusMonths(1);
                params.put("year", String.valueOf(previousMonth.getYear()));
                params.put("month", String.valueOf(previousMonth.getMonthValue()));
            }
            case ABSENTEE_LIST -> {
                params.put("startDate", today.minusDays(1).toString());
                params.put("endDate", today.minusDays(1).toString());
            }
        }

        return params;
    }

    public static LocalDateTime calculateNextRunAt(LocalDateTime from, ScheduleFrequency frequency) {
        return switch (frequency) {
            case DAILY -> from.plusDays(1);
            case WEEKLY -> from.plusWeeks(1);
            case MONTHLY -> from.plusMonths(1);
        };
    }

    public static String[] parseRecipients(String recipients) {
        if (recipients == null || recipients.isBlank()) {
            return new String[0];
        }
        return recipients.split("[,;\\s]+");
    }
}
