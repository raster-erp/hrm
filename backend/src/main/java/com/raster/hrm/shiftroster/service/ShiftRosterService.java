package com.raster.hrm.shiftroster.service;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.rotationpattern.entity.RotationPattern;
import com.raster.hrm.rotationpattern.repository.RotationPatternRepository;
import com.raster.hrm.shift.entity.Shift;
import com.raster.hrm.shift.repository.ShiftRepository;
import com.raster.hrm.shiftroster.dto.BulkShiftRosterRequest;
import com.raster.hrm.shiftroster.dto.ShiftRosterRequest;
import com.raster.hrm.shiftroster.dto.ShiftRosterResponse;
import com.raster.hrm.shiftroster.entity.ShiftRoster;
import com.raster.hrm.shiftroster.repository.ShiftRosterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ShiftRosterService {

    private static final Logger log = LoggerFactory.getLogger(ShiftRosterService.class);

    private final ShiftRosterRepository shiftRosterRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final RotationPatternRepository rotationPatternRepository;

    public ShiftRosterService(ShiftRosterRepository shiftRosterRepository,
                              EmployeeRepository employeeRepository,
                              ShiftRepository shiftRepository,
                              RotationPatternRepository rotationPatternRepository) {
        this.shiftRosterRepository = shiftRosterRepository;
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
        this.rotationPatternRepository = rotationPatternRepository;
    }

    @Transactional(readOnly = true)
    public Page<ShiftRosterResponse> getAll(Pageable pageable) {
        return shiftRosterRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ShiftRosterResponse getById(Long id) {
        var roster = shiftRosterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShiftRoster", "id", id));
        return mapToResponse(roster);
    }

    @Transactional(readOnly = true)
    public List<ShiftRosterResponse> getByEmployeeId(Long employeeId) {
        return shiftRosterRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ShiftRosterResponse> getByEmployeeId(Long employeeId, Pageable pageable) {
        return shiftRosterRepository.findByEmployeeId(employeeId, pageable)
                .map(this::mapToResponse);
    }

    public ShiftRosterResponse create(ShiftRosterRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var shift = shiftRepository.findById(request.shiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", request.shiftId()));

        validateDateRange(request.effectiveDate(), request.endDate());
        checkForConflicts(request.employeeId(), request.effectiveDate(), request.endDate(), null);

        RotationPattern rotationPattern = null;
        if (request.rotationPatternId() != null) {
            rotationPattern = rotationPatternRepository.findById(request.rotationPatternId())
                    .orElseThrow(() -> new ResourceNotFoundException("RotationPattern", "id", request.rotationPatternId()));
        }

        var roster = new ShiftRoster();
        roster.setEmployee(employee);
        roster.setShift(shift);
        roster.setEffectiveDate(request.effectiveDate());
        roster.setEndDate(request.endDate());
        roster.setRotationPattern(rotationPattern);

        var saved = shiftRosterRepository.save(roster);
        log.info("Created shift roster with id: {} for employee: {}", saved.getId(), employee.getEmployeeCode());
        return mapToResponse(saved);
    }

    public List<ShiftRosterResponse> bulkCreate(BulkShiftRosterRequest request) {
        var shift = shiftRepository.findById(request.shiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", request.shiftId()));

        validateDateRange(request.effectiveDate(), request.endDate());

        RotationPattern rotationPattern = null;
        if (request.rotationPatternId() != null) {
            rotationPattern = rotationPatternRepository.findById(request.rotationPatternId())
                    .orElseThrow(() -> new ResourceNotFoundException("RotationPattern", "id", request.rotationPatternId()));
        }

        List<ShiftRosterResponse> results = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();

        for (Long employeeId : request.employeeIds()) {
            var employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

            var overlapping = shiftRosterRepository.findOverlapping(
                    employeeId, request.effectiveDate(),
                    request.endDate() != null ? request.endDate() : LocalDate.of(9999, 12, 31));

            if (!overlapping.isEmpty()) {
                conflicts.add("Employee " + employee.getEmployeeCode() + " has overlapping roster assignment");
                continue;
            }

            var roster = new ShiftRoster();
            roster.setEmployee(employee);
            roster.setShift(shift);
            roster.setEffectiveDate(request.effectiveDate());
            roster.setEndDate(request.endDate());
            roster.setRotationPattern(rotationPattern);

            var saved = shiftRosterRepository.save(roster);
            results.add(mapToResponse(saved));
        }

        if (!conflicts.isEmpty()) {
            log.warn("Bulk roster assignment completed with {} conflicts: {}", conflicts.size(), conflicts);
        }

        log.info("Bulk created {} shift roster assignments", results.size());
        return results;
    }

    public ShiftRosterResponse update(Long id, ShiftRosterRequest request) {
        var roster = shiftRosterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShiftRoster", "id", id));

        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var shift = shiftRepository.findById(request.shiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", request.shiftId()));

        validateDateRange(request.effectiveDate(), request.endDate());
        checkForConflicts(request.employeeId(), request.effectiveDate(), request.endDate(), id);

        RotationPattern rotationPattern = null;
        if (request.rotationPatternId() != null) {
            rotationPattern = rotationPatternRepository.findById(request.rotationPatternId())
                    .orElseThrow(() -> new ResourceNotFoundException("RotationPattern", "id", request.rotationPatternId()));
        }

        roster.setEmployee(employee);
        roster.setShift(shift);
        roster.setEffectiveDate(request.effectiveDate());
        roster.setEndDate(request.endDate());
        roster.setRotationPattern(rotationPattern);

        var saved = shiftRosterRepository.save(roster);
        log.info("Updated shift roster with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var roster = shiftRosterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShiftRoster", "id", id));
        shiftRosterRepository.delete(roster);
        log.info("Deleted shift roster with id: {}", id);
    }

    private void validateDateRange(LocalDate effectiveDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(effectiveDate)) {
            throw new BadRequestException("End date must not be before effective date");
        }
    }

    private void checkForConflicts(Long employeeId, LocalDate effectiveDate, LocalDate endDate, Long excludeId) {
        LocalDate checkEndDate = endDate != null ? endDate : LocalDate.of(9999, 12, 31);

        List<ShiftRoster> overlapping;
        if (excludeId != null) {
            overlapping = shiftRosterRepository.findOverlappingExcluding(
                    employeeId, effectiveDate, checkEndDate, excludeId);
        } else {
            overlapping = shiftRosterRepository.findOverlapping(
                    employeeId, effectiveDate, checkEndDate);
        }

        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Employee has an overlapping shift roster assignment for the specified date range");
        }
    }

    private ShiftRosterResponse mapToResponse(ShiftRoster roster) {
        return new ShiftRosterResponse(
                roster.getId(),
                roster.getEmployee().getId(),
                roster.getEmployee().getFirstName() + " " + roster.getEmployee().getLastName(),
                roster.getEmployee().getEmployeeCode(),
                roster.getShift().getId(),
                roster.getShift().getName(),
                roster.getEffectiveDate(),
                roster.getEndDate(),
                roster.getRotationPattern() != null ? roster.getRotationPattern().getId() : null,
                roster.getRotationPattern() != null ? roster.getRotationPattern().getName() : null,
                roster.getCreatedAt(),
                roster.getUpdatedAt()
        );
    }
}
