package com.raster.hrm.leaveplan.service;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.leaveplan.dto.LeavePlanRequest;
import com.raster.hrm.leaveplan.dto.LeavePlanResponse;
import com.raster.hrm.leaveplan.entity.LeavePlan;
import com.raster.hrm.leaveplan.entity.LeavePlanStatus;
import com.raster.hrm.leaveplan.repository.LeavePlanRepository;
import com.raster.hrm.leavetype.entity.LeaveType;
import com.raster.hrm.leavetype.repository.LeaveTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class LeavePlanService {

    private static final Logger log = LoggerFactory.getLogger(LeavePlanService.class);

    private final LeavePlanRepository leavePlanRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public LeavePlanService(LeavePlanRepository leavePlanRepository,
                            EmployeeRepository employeeRepository,
                            LeaveTypeRepository leaveTypeRepository) {
        this.leavePlanRepository = leavePlanRepository;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    // ── Create ──────────────────────────────────────────────────────────

    public LeavePlanResponse create(LeavePlanRequest request) {
        log.info("Creating leave plan for employee {} from {} to {}",
                request.employeeId(), request.plannedFromDate(), request.plannedToDate());

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        LeaveType leaveType = leaveTypeRepository.findById(request.leaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", request.leaveTypeId()));

        if (request.plannedFromDate().isAfter(request.plannedToDate())) {
            throw new BadRequestException("Planned from date must be on or before planned to date");
        }

        var leavePlan = new LeavePlan();
        leavePlan.setEmployee(employee);
        leavePlan.setLeaveType(leaveType);
        leavePlan.setPlannedFromDate(request.plannedFromDate());
        leavePlan.setPlannedToDate(request.plannedToDate());
        leavePlan.setNumberOfDays(request.numberOfDays());
        leavePlan.setNotes(request.notes());
        leavePlan.setStatus(LeavePlanStatus.PLANNED);

        LeavePlan saved = leavePlanRepository.save(leavePlan);
        log.info("Leave plan created with id {} for employee {}", saved.getId(), request.employeeId());
        return toResponse(saved);
    }

    // ── Update ──────────────────────────────────────────────────────────

    public LeavePlanResponse update(Long id, LeavePlanRequest request) {
        log.info("Updating leave plan {}", id);

        LeavePlan leavePlan = leavePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePlan", "id", id));

        if (leavePlan.getStatus() != LeavePlanStatus.PLANNED) {
            throw new BadRequestException(
                    "Only PLANNED leave plans can be updated. Current status: " + leavePlan.getStatus());
        }

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        LeaveType leaveType = leaveTypeRepository.findById(request.leaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", request.leaveTypeId()));

        if (request.plannedFromDate().isAfter(request.plannedToDate())) {
            throw new BadRequestException("Planned from date must be on or before planned to date");
        }

        leavePlan.setEmployee(employee);
        leavePlan.setLeaveType(leaveType);
        leavePlan.setPlannedFromDate(request.plannedFromDate());
        leavePlan.setPlannedToDate(request.plannedToDate());
        leavePlan.setNumberOfDays(request.numberOfDays());
        leavePlan.setNotes(request.notes());

        LeavePlan saved = leavePlanRepository.save(leavePlan);
        log.info("Leave plan {} updated", id);
        return toResponse(saved);
    }

    // ── Cancel ──────────────────────────────────────────────────────────

    public LeavePlanResponse cancel(Long id) {
        log.info("Cancelling leave plan {}", id);

        LeavePlan leavePlan = leavePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePlan", "id", id));

        if (leavePlan.getStatus() != LeavePlanStatus.PLANNED) {
            throw new BadRequestException(
                    "Only PLANNED leave plans can be cancelled. Current status: " + leavePlan.getStatus());
        }

        leavePlan.setStatus(LeavePlanStatus.CANCELLED);

        LeavePlan saved = leavePlanRepository.save(leavePlan);
        log.info("Leave plan {} cancelled", id);
        return toResponse(saved);
    }

    // ── Convert to Application ──────────────────────────────────────────

    public LeavePlanResponse convertToApplication(Long id) {
        log.info("Converting leave plan {} to application", id);

        LeavePlan leavePlan = leavePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePlan", "id", id));

        if (leavePlan.getStatus() != LeavePlanStatus.PLANNED) {
            throw new BadRequestException(
                    "Only PLANNED leave plans can be converted. Current status: " + leavePlan.getStatus());
        }

        leavePlan.setStatus(LeavePlanStatus.CONVERTED);

        LeavePlan saved = leavePlanRepository.save(leavePlan);
        log.info("Leave plan {} converted to application", id);
        return toResponse(saved);
    }

    // ── Queries ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LeavePlanResponse getById(Long id) {
        log.debug("Fetching leave plan by id {}", id);
        return leavePlanRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePlan", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<LeavePlanResponse> getAll(Pageable pageable) {
        log.debug("Fetching all leave plans");
        return leavePlanRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LeavePlanResponse> getByEmployee(Long employeeId, Pageable pageable) {
        log.debug("Fetching leave plans for employee {}", employeeId);
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        return leavePlanRepository.findByEmployeeId(employeeId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LeavePlanResponse> getByStatus(LeavePlanStatus status, Pageable pageable) {
        log.debug("Fetching leave plans by status {}", status);
        return leavePlanRepository.findByStatus(status, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<LeavePlanResponse> getByDateRange(LocalDate start, LocalDate end) {
        log.debug("Fetching leave plans between {} and {}", start, end);
        return leavePlanRepository
                .findByPlannedFromDateGreaterThanEqualAndPlannedToDateLessThanEqual(start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeavePlanResponse> getByEmployeeAndDateRange(Long employeeId, LocalDate start, LocalDate end) {
        log.debug("Fetching leave plans for employee {} between {} and {}", employeeId, start, end);
        return leavePlanRepository
                .findByEmployeeIdAndPlannedFromDateGreaterThanEqualAndPlannedToDateLessThanEqual(
                        employeeId, start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeavePlanResponse> getByDepartmentAndDateRange(Long departmentId, LocalDate start, LocalDate end) {
        log.debug("Fetching leave plans for department {} between {} and {}", departmentId, start, end);
        return leavePlanRepository
                .findByEmployee_Department_IdAndPlannedFromDateLessThanEqualAndPlannedToDateGreaterThanEqual(
                        departmentId, end, start).stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Private helpers ─────────────────────────────────────────────────

    private LeavePlanResponse toResponse(LeavePlan leavePlan) {
        Employee employee = leavePlan.getEmployee();
        LeaveType leaveType = leavePlan.getLeaveType();
        return new LeavePlanResponse(
                leavePlan.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                leaveType.getId(),
                leaveType.getName(),
                leavePlan.getPlannedFromDate(),
                leavePlan.getPlannedToDate(),
                leavePlan.getNumberOfDays(),
                leavePlan.getNotes(),
                leavePlan.getStatus(),
                leavePlan.getCreatedAt(),
                leavePlan.getUpdatedAt()
        );
    }
}
