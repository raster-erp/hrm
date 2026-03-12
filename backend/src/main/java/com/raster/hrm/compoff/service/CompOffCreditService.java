package com.raster.hrm.compoff.service;

import com.raster.hrm.compoff.dto.CompOffApprovalRequest;
import com.raster.hrm.compoff.dto.CompOffBalanceResponse;
import com.raster.hrm.compoff.dto.CompOffCreditRequest;
import com.raster.hrm.compoff.dto.CompOffCreditResponse;
import com.raster.hrm.compoff.entity.CompOffCredit;
import com.raster.hrm.compoff.entity.CompOffStatus;
import com.raster.hrm.compoff.repository.CompOffCreditRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CompOffCreditService {

    private static final Logger log = LoggerFactory.getLogger(CompOffCreditService.class);
    private static final int DEFAULT_EXPIRY_DAYS = 90;

    private final CompOffCreditRepository compOffCreditRepository;
    private final EmployeeRepository employeeRepository;

    public CompOffCreditService(CompOffCreditRepository compOffCreditRepository,
                                EmployeeRepository employeeRepository) {
        this.compOffCreditRepository = compOffCreditRepository;
        this.employeeRepository = employeeRepository;
    }

    // ── Create Request ──────────────────────────────────────────────────

    public CompOffCreditResponse createRequest(CompOffCreditRequest request) {
        log.info("Creating comp-off credit request for employee {} worked date {}",
                request.employeeId(), request.workedDate());

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        compOffCreditRepository.findByEmployeeIdAndWorkedDateAndStatusIn(
                request.employeeId(), request.workedDate(),
                List.of(CompOffStatus.PENDING, CompOffStatus.APPROVED)
        ).ifPresent(existing -> {
            throw new BadRequestException("A comp-off request already exists for this employee on "
                    + request.workedDate() + " with status: " + existing.getStatus());
        });

        var credit = new CompOffCredit();
        credit.setEmployee(employee);
        credit.setWorkedDate(request.workedDate());
        credit.setReason(request.reason());
        credit.setCreditDate(LocalDate.now());
        credit.setExpiryDate(request.workedDate().plusDays(DEFAULT_EXPIRY_DAYS));
        credit.setHoursWorked(request.hoursWorked());
        credit.setStatus(CompOffStatus.PENDING);
        credit.setRemarks(request.remarks());

        CompOffCredit saved = compOffCreditRepository.save(credit);
        log.info("Comp-off credit request created with id {} for employee {}", saved.getId(), request.employeeId());
        return toResponse(saved);
    }

    // ── Approve / Reject ────────────────────────────────────────────────

    public CompOffCreditResponse approve(Long id, CompOffApprovalRequest request) {
        log.info("Processing comp-off approval for id {} with status {}", id, request.status());

        CompOffCredit credit = compOffCreditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CompOffCredit", "id", id));

        if (credit.getStatus() != CompOffStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING comp-off requests can be approved or rejected. Current status: "
                            + credit.getStatus());
        }

        if (request.status() != CompOffStatus.APPROVED && request.status() != CompOffStatus.REJECTED) {
            throw new BadRequestException("Approval status must be either APPROVED or REJECTED");
        }

        credit.setStatus(request.status());
        credit.setApprovedBy(request.approvedBy());
        credit.setApprovedAt(LocalDateTime.now());

        if (request.remarks() != null) {
            credit.setRemarks(request.remarks());
        }

        CompOffCredit saved = compOffCreditRepository.save(credit);
        log.info("Comp-off {} {} by {}", id, request.status(), request.approvedBy());
        return toResponse(saved);
    }

    // ── Balance ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CompOffBalanceResponse getBalance(Long employeeId) {
        log.debug("Fetching comp-off balance for employee {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        long total = compOffCreditRepository.countByEmployeeIdAndStatus(employeeId, CompOffStatus.APPROVED)
                + compOffCreditRepository.countByEmployeeIdAndStatus(employeeId, CompOffStatus.PENDING)
                + compOffCreditRepository.countByEmployeeIdAndStatus(employeeId, CompOffStatus.USED)
                + compOffCreditRepository.countByEmployeeIdAndStatus(employeeId, CompOffStatus.EXPIRED)
                + compOffCreditRepository.countByEmployeeIdAndStatus(employeeId, CompOffStatus.REJECTED);

        long approved = compOffCreditRepository.countByEmployeeIdAndStatus(employeeId, CompOffStatus.APPROVED);
        long pending = compOffCreditRepository.countByEmployeeIdAndStatus(employeeId, CompOffStatus.PENDING);
        long used = compOffCreditRepository.countByEmployeeIdAndStatus(employeeId, CompOffStatus.USED);
        long expired = compOffCreditRepository.countByEmployeeIdAndStatus(employeeId, CompOffStatus.EXPIRED);

        String employeeName = employee.getFirstName() + " " + employee.getLastName();

        return new CompOffBalanceResponse(
                employeeId, employeeName, total, approved, pending, used, expired, approved);
    }

    // ── Expire Credits ──────────────────────────────────────────────────

    public int expireCredits() {
        log.info("Running comp-off expiry check");

        List<CompOffCredit> expiredCredits = compOffCreditRepository
                .findByStatusAndExpiryDateBefore(CompOffStatus.APPROVED, LocalDate.now());

        for (CompOffCredit credit : expiredCredits) {
            credit.setStatus(CompOffStatus.EXPIRED);
        }

        compOffCreditRepository.saveAll(expiredCredits);
        log.info("Expired {} comp-off credits", expiredCredits.size());
        return expiredCredits.size();
    }

    // ── Queries ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CompOffCreditResponse getById(Long id) {
        log.debug("Fetching comp-off credit by id {}", id);
        return compOffCreditRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("CompOffCredit", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<CompOffCreditResponse> getByEmployee(Long employeeId, Pageable pageable) {
        log.debug("Fetching comp-off credits for employee {}", employeeId);
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        return compOffCreditRepository.findByEmployeeId(employeeId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CompOffCreditResponse> getByStatus(CompOffStatus status, Pageable pageable) {
        log.debug("Fetching comp-off credits by status {}", status);
        return compOffCreditRepository.findByStatus(status, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CompOffCreditResponse> getAll(Pageable pageable) {
        log.debug("Fetching all comp-off credits");
        return compOffCreditRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // ── Private helpers ─────────────────────────────────────────────────

    private CompOffCreditResponse toResponse(CompOffCredit credit) {
        Employee employee = credit.getEmployee();
        return new CompOffCreditResponse(
                credit.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                credit.getWorkedDate(),
                credit.getReason(),
                credit.getCreditDate(),
                credit.getExpiryDate(),
                credit.getHoursWorked(),
                credit.getStatus().name(),
                credit.getApprovedBy(),
                credit.getApprovedAt(),
                credit.getUsedDate(),
                credit.getRemarks(),
                credit.getCreatedAt(),
                credit.getUpdatedAt()
        );
    }
}
