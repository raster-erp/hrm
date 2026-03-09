package com.raster.hrm.transfer.service;

import com.raster.hrm.department.entity.Department;
import com.raster.hrm.department.repository.DepartmentRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.transfer.dto.TransferRequest;
import com.raster.hrm.transfer.dto.TransferResponse;
import com.raster.hrm.transfer.entity.Transfer;
import com.raster.hrm.transfer.entity.TransferStatus;
import com.raster.hrm.transfer.entity.TransferType;
import com.raster.hrm.transfer.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final TransferRepository transferRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public TransferService(TransferRepository transferRepository,
                           EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository) {
        this.transferRepository = transferRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getAll(Pageable pageable) {
        return transferRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public TransferResponse getById(Long id) {
        var transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", "id", id));
        return mapToResponse(transfer);
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> getByEmployeeId(Long employeeId) {
        return transferRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> getPendingTransfers() {
        return transferRepository.findByStatus(TransferStatus.PENDING).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TransferResponse create(TransferRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var transfer = new Transfer();
        transfer.setEmployee(employee);
        transfer.setFromBranch(request.fromBranch());
        transfer.setToBranch(request.toBranch());
        try {
            transfer.setTransferType(TransferType.valueOf(request.transferType()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid transfer type: " + request.transferType());
        }
        transfer.setEffectiveDate(request.effectiveDate());
        transfer.setReason(request.reason());
        transfer.setStatus(TransferStatus.PENDING);

        if (request.fromDepartmentId() != null) {
            var fromDepartment = departmentRepository.findById(request.fromDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.fromDepartmentId()));
            transfer.setFromDepartment(fromDepartment);
        }

        if (request.toDepartmentId() != null) {
            var toDepartment = departmentRepository.findById(request.toDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.toDepartmentId()));
            transfer.setToDepartment(toDepartment);
        }

        var saved = transferRepository.save(transfer);
        log.info("Created transfer with id: {} for employee id: {}", saved.getId(), employee.getId());
        return mapToResponse(saved);
    }

    public TransferResponse approve(Long id, Long approvedById) {
        var transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", "id", id));

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new BadRequestException("Transfer can only be approved when in PENDING status");
        }

        var approver = employeeRepository.findById(approvedById)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", approvedById));

        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setApprovedBy(approver);
        transfer.setApprovedAt(LocalDateTime.now());

        var saved = transferRepository.save(transfer);
        log.info("Approved transfer with id: {} by employee id: {}", saved.getId(), approvedById);
        return mapToResponse(saved);
    }

    public TransferResponse reject(Long id, Long approvedById) {
        var transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", "id", id));

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new BadRequestException("Transfer can only be rejected when in PENDING status");
        }

        var approver = employeeRepository.findById(approvedById)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", approvedById));

        transfer.setStatus(TransferStatus.REJECTED);
        transfer.setApprovedBy(approver);
        transfer.setApprovedAt(LocalDateTime.now());

        var saved = transferRepository.save(transfer);
        log.info("Rejected transfer with id: {} by employee id: {}", saved.getId(), approvedById);
        return mapToResponse(saved);
    }

    public TransferResponse execute(Long id) {
        var transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", "id", id));

        if (transfer.getStatus() != TransferStatus.APPROVED) {
            throw new BadRequestException("Transfer must be in APPROVED status before execution");
        }

        transfer.setStatus(TransferStatus.EXECUTED);

        if (transfer.getToDepartment() != null) {
            var employee = transfer.getEmployee();
            employee.setDepartment(transfer.getToDepartment());
            employeeRepository.save(employee);
        }

        var saved = transferRepository.save(transfer);
        log.info("Executed transfer with id: {} for employee id: {}", saved.getId(), transfer.getEmployee().getId());
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", "id", id));

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new BadRequestException("Only transfers in PENDING status can be deleted");
        }

        transferRepository.delete(transfer);
        log.info("Deleted transfer with id: {}", id);
    }

    private TransferResponse mapToResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getEmployee().getId(),
                transfer.getEmployee().getEmployeeCode(),
                transfer.getEmployee().getFirstName() + " " + transfer.getEmployee().getLastName(),
                transfer.getFromDepartment() != null ? transfer.getFromDepartment().getId() : null,
                transfer.getFromDepartment() != null ? transfer.getFromDepartment().getName() : null,
                transfer.getToDepartment() != null ? transfer.getToDepartment().getId() : null,
                transfer.getToDepartment() != null ? transfer.getToDepartment().getName() : null,
                transfer.getFromBranch(),
                transfer.getToBranch(),
                transfer.getTransferType().name(),
                transfer.getEffectiveDate(),
                transfer.getStatus().name(),
                transfer.getReason(),
                transfer.getApprovedBy() != null ? transfer.getApprovedBy().getId() : null,
                transfer.getApprovedBy() != null
                        ? transfer.getApprovedBy().getFirstName() + " " + transfer.getApprovedBy().getLastName()
                        : null,
                transfer.getApprovedAt(),
                transfer.getCreatedAt(),
                transfer.getUpdatedAt()
        );
    }
}
