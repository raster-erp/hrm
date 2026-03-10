package com.raster.hrm.attendance.service;

import com.raster.hrm.attendance.dto.AttendancePunchRequest;
import com.raster.hrm.attendance.dto.AttendancePunchResponse;
import com.raster.hrm.attendance.dto.PunchSyncRequest;
import com.raster.hrm.attendance.dto.PunchSyncResponse;
import com.raster.hrm.attendance.entity.AttendancePunch;
import com.raster.hrm.attendance.entity.PunchDirection;
import com.raster.hrm.attendance.repository.AttendancePunchRepository;
import com.raster.hrm.device.entity.Device;
import com.raster.hrm.device.repository.DeviceRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AttendancePunchService {

    private static final Logger log = LoggerFactory.getLogger(AttendancePunchService.class);

    private final AttendancePunchRepository punchRepository;
    private final EmployeeRepository employeeRepository;
    private final DeviceRepository deviceRepository;

    public AttendancePunchService(AttendancePunchRepository punchRepository,
                                  EmployeeRepository employeeRepository,
                                  DeviceRepository deviceRepository) {
        this.punchRepository = punchRepository;
        this.employeeRepository = employeeRepository;
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public Page<AttendancePunchResponse> getAll(Pageable pageable) {
        return punchRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public AttendancePunchResponse getById(Long id) {
        var punch = punchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendancePunch", "id", id));
        return mapToResponse(punch);
    }

    @Transactional(readOnly = true)
    public Page<AttendancePunchResponse> getByEmployeeId(Long employeeId, Pageable pageable) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        return punchRepository.findByEmployeeId(employeeId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AttendancePunchResponse> getByDateRange(LocalDate from, LocalDate to, Pageable pageable) {
        var fromDateTime = from.atStartOfDay();
        var toDateTime = to.atTime(LocalTime.MAX);
        return punchRepository.findByPunchTimeBetween(fromDateTime, toDateTime, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AttendancePunchResponse> getByEmployeeAndDateRange(Long employeeId,
                                                                    LocalDate from,
                                                                    LocalDate to,
                                                                    Pageable pageable) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        var fromDateTime = from.atStartOfDay();
        var toDateTime = to.atTime(LocalTime.MAX);
        return punchRepository.findByEmployeeIdAndPunchTimeBetween(employeeId, fromDateTime, toDateTime, pageable)
                .map(this::mapToResponse);
    }

    public AttendancePunchResponse create(AttendancePunchRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));
        var device = deviceRepository.findById(request.deviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", request.deviceId()));

        var direction = PunchDirection.valueOf(request.direction());

        if (isDuplicate(employee.getId(), device.getId(), request.punchTime(), direction)) {
            log.warn("Duplicate punch detected for employee: {} at {}", employee.getEmployeeCode(), request.punchTime());
            var existing = punchRepository.findByEmployeeIdAndPunchTimeBetween(
                    employee.getId(), request.punchTime(), request.punchTime(),
                    Pageable.unpaged());
            return mapToResponse(existing.getContent().get(0));
        }

        var punch = new AttendancePunch();
        punch.setEmployee(employee);
        punch.setDevice(device);
        punch.setPunchTime(request.punchTime());
        punch.setDirection(direction);
        punch.setRawData(request.rawData());
        punch.setNormalized(true);
        punch.setSource("API");

        var saved = punchRepository.save(punch);
        log.info("Created attendance punch id: {} for employee: {} at {}", saved.getId(),
                employee.getEmployeeCode(), request.punchTime());
        return mapToResponse(saved);
    }

    public PunchSyncResponse syncPunches(PunchSyncRequest request) {
        var device = deviceRepository.findById(request.deviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", request.deviceId()));

        List<Long> acceptedIds = new ArrayList<>();
        int duplicatesSkipped = 0;

        for (var punchData : request.punches()) {
            var employeeOpt = employeeRepository.findById(punchData.employeeId());
            if (employeeOpt.isEmpty()) {
                log.warn("Skipping punch for unknown employee id: {}", punchData.employeeId());
                continue;
            }

            var employee = employeeOpt.get();
            var punchTime = LocalDateTime.parse(punchData.punchTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            var direction = PunchDirection.valueOf(punchData.direction());

            if (isDuplicate(employee.getId(), device.getId(), punchTime, direction)) {
                duplicatesSkipped++;
                continue;
            }

            var punch = new AttendancePunch();
            punch.setEmployee(employee);
            punch.setDevice(device);
            punch.setPunchTime(punchTime);
            punch.setDirection(direction);
            punch.setRawData(punchData.rawData());
            punch.setNormalized(true);
            punch.setSource("DEVICE");

            var saved = punchRepository.save(punch);
            acceptedIds.add(saved.getId());
        }

        device.setLastSyncAt(LocalDateTime.now());
        deviceRepository.save(device);

        log.info("Synced {} punches from device {}, {} duplicates skipped",
                acceptedIds.size(), device.getSerialNumber(), duplicatesSkipped);

        return new PunchSyncResponse(
                request.punches().size(),
                acceptedIds.size(),
                duplicatesSkipped,
                acceptedIds
        );
    }

    public void delete(Long id) {
        var punch = punchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendancePunch", "id", id));
        punchRepository.delete(punch);
        log.info("Deleted attendance punch with id: {}", id);
    }

    private boolean isDuplicate(Long employeeId, Long deviceId, LocalDateTime punchTime, PunchDirection direction) {
        return punchRepository.existsDuplicate(employeeId, deviceId, punchTime, direction);
    }

    private AttendancePunchResponse mapToResponse(AttendancePunch punch) {
        var employee = punch.getEmployee();
        var device = punch.getDevice();
        return new AttendancePunchResponse(
                punch.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                device.getId(),
                device.getSerialNumber(),
                device.getName(),
                punch.getPunchTime(),
                punch.getDirection().name(),
                punch.getRawData(),
                punch.isNormalized(),
                punch.getSource(),
                punch.getCreatedAt(),
                punch.getUpdatedAt()
        );
    }
}
