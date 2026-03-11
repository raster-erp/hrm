package com.raster.hrm.wfh.service;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.wfh.dto.WfhCheckInRequest;
import com.raster.hrm.wfh.dto.WfhCheckInResponse;
import com.raster.hrm.wfh.entity.WfhActivityLog;
import com.raster.hrm.wfh.entity.WfhStatus;
import com.raster.hrm.wfh.repository.WfhActivityLogRepository;
import com.raster.hrm.wfh.repository.WfhRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WfhActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(WfhActivityLogService.class);

    private final WfhActivityLogRepository wfhActivityLogRepository;
    private final WfhRequestRepository wfhRequestRepository;

    public WfhActivityLogService(WfhActivityLogRepository wfhActivityLogRepository,
                                  WfhRequestRepository wfhRequestRepository) {
        this.wfhActivityLogRepository = wfhActivityLogRepository;
        this.wfhRequestRepository = wfhRequestRepository;
    }

    public WfhCheckInResponse checkIn(WfhCheckInRequest request) {
        var wfhRequest = wfhRequestRepository.findById(request.wfhRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("WfhRequest", "id", request.wfhRequestId()));

        if (wfhRequest.getStatus() != WfhStatus.APPROVED) {
            throw new BadRequestException("WFH request must be APPROVED to check in. Current status: " + wfhRequest.getStatus());
        }

        if (!wfhRequest.getRequestDate().equals(LocalDate.now())) {
            throw new BadRequestException("Can only check in on the WFH request date: " + wfhRequest.getRequestDate());
        }

        var activeSession = wfhActivityLogRepository.findActiveByRequestId(request.wfhRequestId());
        if (activeSession.isPresent()) {
            throw new BadRequestException("An active check-in session already exists for this WFH request");
        }

        var activityLog = new WfhActivityLog();
        activityLog.setWfhRequest(wfhRequest);
        activityLog.setCheckInTime(LocalDateTime.now());
        activityLog.setIpAddress(request.ipAddress());
        activityLog.setLocation(request.location());

        var saved = wfhActivityLogRepository.save(activityLog);
        log.info("WFH check-in created with id: {} for request: {}", saved.getId(), wfhRequest.getId());
        return mapToResponse(saved);
    }

    public WfhCheckInResponse checkOut(Long activityLogId) {
        var activityLog = wfhActivityLogRepository.findById(activityLogId)
                .orElseThrow(() -> new ResourceNotFoundException("WfhActivityLog", "id", activityLogId));

        if (activityLog.getCheckOutTime() != null) {
            throw new BadRequestException("This session has already been checked out");
        }

        activityLog.setCheckOutTime(LocalDateTime.now());
        var saved = wfhActivityLogRepository.save(activityLog);
        log.info("WFH check-out completed for activity log id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WfhCheckInResponse> getByRequestId(Long requestId) {
        return wfhActivityLogRepository.findByWfhRequestId(requestId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<WfhCheckInResponse> getActiveSession(Long requestId) {
        return wfhActivityLogRepository.findActiveByRequestId(requestId)
                .map(this::mapToResponse);
    }

    private WfhCheckInResponse mapToResponse(WfhActivityLog activityLog) {
        var wfhRequest = activityLog.getWfhRequest();
        var employee = wfhRequest.getEmployee();
        return new WfhCheckInResponse(
                activityLog.getId(),
                wfhRequest.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                activityLog.getCheckInTime(),
                activityLog.getCheckOutTime(),
                activityLog.getIpAddress(),
                activityLog.getLocation(),
                activityLog.getCreatedAt(),
                activityLog.getUpdatedAt()
        );
    }
}
