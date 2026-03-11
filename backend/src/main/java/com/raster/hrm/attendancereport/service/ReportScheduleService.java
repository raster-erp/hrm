package com.raster.hrm.attendancereport.service;

import com.raster.hrm.attendancereport.dto.ReportScheduleRequest;
import com.raster.hrm.attendancereport.dto.ReportScheduleResponse;
import com.raster.hrm.attendancereport.entity.ReportFormat;
import com.raster.hrm.attendancereport.entity.ReportSchedule;
import com.raster.hrm.attendancereport.entity.ReportType;
import com.raster.hrm.attendancereport.entity.ScheduleFrequency;
import com.raster.hrm.attendancereport.repository.ReportScheduleRepository;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ReportScheduleService.class);

    private final ReportScheduleRepository reportScheduleRepository;
    private final DepartmentRepository departmentRepository;

    public ReportScheduleService(ReportScheduleRepository reportScheduleRepository,
                                  DepartmentRepository departmentRepository) {
        this.reportScheduleRepository = reportScheduleRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<ReportScheduleResponse> getAll(Pageable pageable) {
        return reportScheduleRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ReportScheduleResponse getById(Long id) {
        var schedule = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportSchedule", "id", id));
        return mapToResponse(schedule);
    }

    @Transactional(readOnly = true)
    public Page<ReportScheduleResponse> getByReportType(String reportType, Pageable pageable) {
        var type = parseReportType(reportType);
        return reportScheduleRepository.findByReportType(type, pageable)
                .map(this::mapToResponse);
    }

    public ReportScheduleResponse create(ReportScheduleRequest request) {
        var schedule = new ReportSchedule();
        mapFromRequest(schedule, request);

        var saved = reportScheduleRepository.save(schedule);
        log.info("Created report schedule with id: {} name: {}", saved.getId(), saved.getReportName());
        return mapToResponse(saved);
    }

    public ReportScheduleResponse update(Long id, ReportScheduleRequest request) {
        var schedule = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportSchedule", "id", id));

        mapFromRequest(schedule, request);

        var saved = reportScheduleRepository.save(schedule);
        log.info("Updated report schedule with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public ReportScheduleResponse toggleActive(Long id) {
        var schedule = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportSchedule", "id", id));

        schedule.setActive(!schedule.isActive());
        var saved = reportScheduleRepository.save(schedule);
        log.info("Toggled report schedule id: {} active to: {}", saved.getId(), saved.isActive());
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var schedule = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReportSchedule", "id", id));
        reportScheduleRepository.delete(schedule);
        log.info("Deleted report schedule with id: {}", id);
    }

    private void mapFromRequest(ReportSchedule schedule, ReportScheduleRequest request) {
        schedule.setReportName(request.reportName());
        schedule.setReportType(parseReportType(request.reportType()));
        schedule.setFrequency(parseFrequency(request.frequency()));
        schedule.setRecipients(request.recipients());

        if (request.exportFormat() != null && !request.exportFormat().isBlank()) {
            schedule.setExportFormat(parseExportFormat(request.exportFormat()));
        } else {
            schedule.setExportFormat(ReportFormat.CSV);
        }

        if (request.departmentId() != null) {
            var department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.departmentId()));
            schedule.setDepartment(department);
        } else {
            schedule.setDepartment(null);
        }
    }

    private ReportScheduleResponse mapToResponse(ReportSchedule schedule) {
        var departmentId = schedule.getDepartment() != null ? schedule.getDepartment().getId() : null;
        var departmentName = schedule.getDepartment() != null ? schedule.getDepartment().getName() : null;

        return new ReportScheduleResponse(
                schedule.getId(),
                schedule.getReportName(),
                schedule.getReportType().name(),
                schedule.getFrequency().name(),
                departmentId,
                departmentName,
                schedule.getRecipients(),
                schedule.getExportFormat().name(),
                schedule.isActive(),
                schedule.getLastRunAt(),
                schedule.getNextRunAt(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }

    private ReportType parseReportType(String value) {
        try {
            return ReportType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid report type: " + value
                    + ". Allowed values: DAILY_MUSTER, MONTHLY_SUMMARY, ABSENTEE_LIST");
        }
    }

    private ScheduleFrequency parseFrequency(String value) {
        try {
            return ScheduleFrequency.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid frequency: " + value
                    + ". Allowed values: DAILY, WEEKLY, MONTHLY");
        }
    }

    private ReportFormat parseExportFormat(String value) {
        try {
            return ReportFormat.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid export format: " + value
                    + ". Allowed values: CSV, PDF, EXCEL");
        }
    }
}
