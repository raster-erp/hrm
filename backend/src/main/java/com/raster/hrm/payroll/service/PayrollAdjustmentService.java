package com.raster.hrm.payroll.service;

import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.payroll.dto.PayrollAdjustmentRequest;
import com.raster.hrm.payroll.dto.PayrollAdjustmentResponse;
import com.raster.hrm.payroll.entity.AdjustmentType;
import com.raster.hrm.payroll.entity.PayrollAdjustment;
import com.raster.hrm.payroll.entity.PayrollRunStatus;
import com.raster.hrm.payroll.repository.PayrollAdjustmentRepository;
import com.raster.hrm.payroll.repository.PayrollRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PayrollAdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(PayrollAdjustmentService.class);

    private final PayrollAdjustmentRepository payrollAdjustmentRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final EmployeeRepository employeeRepository;

    public PayrollAdjustmentService(PayrollAdjustmentRepository payrollAdjustmentRepository,
                                    PayrollRunRepository payrollRunRepository,
                                    EmployeeRepository employeeRepository) {
        this.payrollAdjustmentRepository = payrollAdjustmentRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<PayrollAdjustmentResponse> getByRunId(Long runId) {
        payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", runId));
        return payrollAdjustmentRepository.findByPayrollRunId(runId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PayrollAdjustmentResponse create(PayrollAdjustmentRequest request) {
        var run = payrollRunRepository.findById(request.payrollRunId())
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", request.payrollRunId()));

        if (run.getStatus() != PayrollRunStatus.DRAFT && run.getStatus() != PayrollRunStatus.COMPUTED) {
            throw new BadRequestException("Payroll run must be in DRAFT or COMPUTED status to add adjustments. Current status: "
                    + run.getStatus());
        }

        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var adjustment = new PayrollAdjustment();
        adjustment.setPayrollRun(run);
        adjustment.setEmployee(employee);
        adjustment.setAdjustmentType(AdjustmentType.valueOf(request.adjustmentType()));
        adjustment.setComponentName(request.componentName());
        adjustment.setAmount(request.amount());
        adjustment.setReason(request.reason());

        var saved = payrollAdjustmentRepository.save(adjustment);
        log.info("Created payroll adjustment id: {} for run: {} employee: {}",
                saved.getId(), request.payrollRunId(), request.employeeId());
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var adjustment = payrollAdjustmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollAdjustment", "id", id));

        var run = adjustment.getPayrollRun();
        if (run.getStatus() != PayrollRunStatus.DRAFT && run.getStatus() != PayrollRunStatus.COMPUTED) {
            throw new BadRequestException("Payroll run must be in DRAFT or COMPUTED status to delete adjustments. Current status: "
                    + run.getStatus());
        }

        payrollAdjustmentRepository.delete(adjustment);
        log.info("Deleted payroll adjustment id: {}", id);
    }

    private PayrollAdjustmentResponse mapToResponse(PayrollAdjustment adjustment) {
        var employee = adjustment.getEmployee();
        return new PayrollAdjustmentResponse(
                adjustment.getId(),
                adjustment.getPayrollRun().getId(),
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getEmployeeCode(),
                adjustment.getAdjustmentType().name(),
                adjustment.getComponentName(),
                adjustment.getAmount(),
                adjustment.getReason(),
                adjustment.getCreatedAt()
        );
    }
}
