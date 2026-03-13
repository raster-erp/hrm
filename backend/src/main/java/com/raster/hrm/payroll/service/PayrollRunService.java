package com.raster.hrm.payroll.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.employeesalary.entity.EmployeeSalaryDetail;
import com.raster.hrm.employeesalary.repository.EmployeeSalaryDetailRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.payroll.dto.PayrollDetailResponse;
import com.raster.hrm.payroll.dto.PayrollRunRequest;
import com.raster.hrm.payroll.dto.PayrollRunResponse;
import com.raster.hrm.payroll.entity.AdjustmentType;
import com.raster.hrm.payroll.entity.PayrollAdjustment;
import com.raster.hrm.payroll.entity.PayrollDetail;
import com.raster.hrm.payroll.entity.PayrollRun;
import com.raster.hrm.payroll.entity.PayrollRunStatus;
import com.raster.hrm.payroll.repository.PayrollAdjustmentRepository;
import com.raster.hrm.payroll.repository.PayrollDetailRepository;
import com.raster.hrm.payroll.repository.PayrollRunRepository;
import com.raster.hrm.salarycomponent.entity.SalaryComponentType;
import com.raster.hrm.salarystructure.entity.SalaryStructureComponent;
import com.raster.hrm.salarystructure.repository.SalaryStructureComponentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PayrollRunService {

    private static final Logger log = LoggerFactory.getLogger(PayrollRunService.class);

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollDetailRepository payrollDetailRepository;
    private final PayrollAdjustmentRepository payrollAdjustmentRepository;
    private final EmployeeSalaryDetailRepository employeeSalaryDetailRepository;
    private final SalaryStructureComponentRepository salaryStructureComponentRepository;
    private final ObjectMapper objectMapper;

    public PayrollRunService(PayrollRunRepository payrollRunRepository,
                             PayrollDetailRepository payrollDetailRepository,
                             PayrollAdjustmentRepository payrollAdjustmentRepository,
                             EmployeeSalaryDetailRepository employeeSalaryDetailRepository,
                             SalaryStructureComponentRepository salaryStructureComponentRepository,
                             ObjectMapper objectMapper) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollDetailRepository = payrollDetailRepository;
        this.payrollAdjustmentRepository = payrollAdjustmentRepository;
        this.employeeSalaryDetailRepository = employeeSalaryDetailRepository;
        this.salaryStructureComponentRepository = salaryStructureComponentRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Page<PayrollRunResponse> getAll(Pageable pageable) {
        return payrollRunRepository.findAllByOrderByPeriodYearDescPeriodMonthDesc(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public PayrollRunResponse getById(Long id) {
        var run = payrollRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", id));
        return mapToResponse(run);
    }

    public PayrollRunResponse initialize(PayrollRunRequest request) {
        payrollRunRepository.findByPeriodYearAndPeriodMonth(request.periodYear(), request.periodMonth())
                .ifPresent(existing -> {
                    throw new BadRequestException("Payroll run already exists for period "
                            + request.periodYear() + "-" + request.periodMonth());
                });

        var run = new PayrollRun();
        run.setPeriodYear(request.periodYear());
        run.setPeriodMonth(request.periodMonth());
        run.setRunDate(LocalDate.now());
        run.setStatus(PayrollRunStatus.DRAFT);
        run.setNotes(request.notes());

        var saved = payrollRunRepository.save(run);
        log.info("Initialized payroll run for period {}-{} with id: {}",
                request.periodYear(), request.periodMonth(), saved.getId());
        return mapToResponse(saved);
    }

    public PayrollRunResponse computePayroll(Long runId) {
        var run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", runId));

        if (run.getStatus() != PayrollRunStatus.DRAFT && run.getStatus() != PayrollRunStatus.COMPUTED) {
            throw new BadRequestException("Payroll run must be in DRAFT or COMPUTED status to compute. Current status: "
                    + run.getStatus());
        }

        run.setStatus(PayrollRunStatus.PROCESSING);
        payrollRunRepository.save(run);
        log.info("Started payroll computation for run id: {}", runId);

        List<EmployeeSalaryDetail> activeSalaryDetails = employeeSalaryDetailRepository.findByActiveTrue();

        var runTotalGross = BigDecimal.ZERO;
        var runTotalDeductions = BigDecimal.ZERO;
        var runTotalNet = BigDecimal.ZERO;
        int employeeCount = 0;

        for (var salaryDetail : activeSalaryDetails) {
            var employee = salaryDetail.getEmployee();
            var salaryStructure = salaryDetail.getSalaryStructure();
            var basicSalary = salaryDetail.getBasicSalary();

            List<SalaryStructureComponent> components =
                    salaryStructureComponentRepository.findBySalaryStructureIdOrderBySortOrder(salaryStructure.getId());

            var grossSalary = BigDecimal.ZERO;
            var totalDeductions = BigDecimal.ZERO;
            List<Map<String, Object>> breakupList = new ArrayList<>();

            for (var component : components) {
                var salaryComponent = component.getSalaryComponent();
                var computedAmount = computeComponentAmount(component, basicSalary);

                if (salaryComponent.getType() == SalaryComponentType.EARNING) {
                    grossSalary = grossSalary.add(computedAmount);
                } else if (salaryComponent.getType() == SalaryComponentType.DEDUCTION) {
                    totalDeductions = totalDeductions.add(computedAmount);
                }

                Map<String, Object> breakupEntry = new HashMap<>();
                breakupEntry.put("name", salaryComponent.getName());
                breakupEntry.put("type", salaryComponent.getType().name());
                breakupEntry.put("amount", computedAmount);
                breakupList.add(breakupEntry);
            }

            // Apply adjustments
            List<PayrollAdjustment> adjustments =
                    payrollAdjustmentRepository.findByPayrollRunIdAndEmployeeId(runId, employee.getId());
            for (var adjustment : adjustments) {
                if (adjustment.getAdjustmentType() == AdjustmentType.ADDITION) {
                    grossSalary = grossSalary.add(adjustment.getAmount());
                } else if (adjustment.getAdjustmentType() == AdjustmentType.DEDUCTION) {
                    totalDeductions = totalDeductions.add(adjustment.getAmount());
                }

                Map<String, Object> adjustmentEntry = new HashMap<>();
                adjustmentEntry.put("name", adjustment.getComponentName());
                adjustmentEntry.put("type", adjustment.getAdjustmentType().name());
                adjustmentEntry.put("amount", adjustment.getAmount());
                breakupList.add(adjustmentEntry);
            }

            var netSalary = grossSalary.subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP);
            grossSalary = grossSalary.setScale(2, RoundingMode.HALF_UP);
            totalDeductions = totalDeductions.setScale(2, RoundingMode.HALF_UP);

            String breakupJson = serializeBreakup(breakupList);

            // Create or update payroll detail
            var detail = payrollDetailRepository.findByPayrollRunIdAndEmployeeId(runId, employee.getId())
                    .orElse(new PayrollDetail());
            detail.setPayrollRun(run);
            detail.setEmployee(employee);
            detail.setSalaryStructure(salaryStructure);
            detail.setBasicSalary(basicSalary);
            detail.setGrossSalary(grossSalary);
            detail.setTotalDeductions(totalDeductions);
            detail.setNetSalary(netSalary);
            detail.setComponentBreakup(breakupJson);
            payrollDetailRepository.save(detail);

            runTotalGross = runTotalGross.add(grossSalary);
            runTotalDeductions = runTotalDeductions.add(totalDeductions);
            runTotalNet = runTotalNet.add(netSalary);
            employeeCount++;
        }

        run.setTotalGross(runTotalGross.setScale(2, RoundingMode.HALF_UP));
        run.setTotalDeductions(runTotalDeductions.setScale(2, RoundingMode.HALF_UP));
        run.setTotalNet(runTotalNet.setScale(2, RoundingMode.HALF_UP));
        run.setEmployeeCount(employeeCount);
        run.setStatus(PayrollRunStatus.COMPUTED);

        var saved = payrollRunRepository.save(run);
        log.info("Completed payroll computation for run id: {} with {} employees", runId, employeeCount);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PayrollDetailResponse> getDetails(Long runId, Pageable pageable) {
        payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", runId));
        return payrollDetailRepository.findByPayrollRunId(runId, pageable)
                .map(this::mapDetailToResponse);
    }

    @Transactional(readOnly = true)
    public PayrollDetailResponse getDetailByEmployee(Long runId, Long employeeId) {
        var detail = payrollDetailRepository.findByPayrollRunIdAndEmployeeId(runId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollDetail", "employeeId", employeeId));
        return mapDetailToResponse(detail);
    }

    public PayrollRunResponse verify(Long runId) {
        var run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", runId));

        if (run.getStatus() != PayrollRunStatus.COMPUTED) {
            throw new BadRequestException("Payroll run must be in COMPUTED status to verify. Current status: "
                    + run.getStatus());
        }

        run.setStatus(PayrollRunStatus.VERIFIED);
        var saved = payrollRunRepository.save(run);
        log.info("Verified payroll run id: {}", runId);
        return mapToResponse(saved);
    }

    public PayrollRunResponse finalizeRun(Long runId) {
        var run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", runId));

        if (run.getStatus() != PayrollRunStatus.VERIFIED) {
            throw new BadRequestException("Payroll run must be in VERIFIED status to finalize. Current status: "
                    + run.getStatus());
        }

        run.setStatus(PayrollRunStatus.FINALIZED);
        var saved = payrollRunRepository.save(run);
        log.info("Finalized payroll run id: {}", runId);
        return mapToResponse(saved);
    }

    public PayrollRunResponse reverse(Long runId) {
        var run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", runId));

        if (run.getStatus() != PayrollRunStatus.COMPUTED && run.getStatus() != PayrollRunStatus.VERIFIED) {
            throw new BadRequestException("Payroll run must be in COMPUTED or VERIFIED status to reverse. Current status: "
                    + run.getStatus());
        }

        run.setStatus(PayrollRunStatus.REVERSED);
        var saved = payrollRunRepository.save(run);
        log.info("Reversed payroll run id: {}", runId);
        return mapToResponse(saved);
    }

    private BigDecimal computeComponentAmount(SalaryStructureComponent component, BigDecimal basicSalary) {
        return switch (component.getComputationType()) {
            case FIXED -> component.getFixedAmount() != null
                    ? component.getFixedAmount()
                    : BigDecimal.ZERO;
            case PERCENTAGE_OF_BASIC -> {
                var percentage = component.getPercentageValue() != null
                        ? component.getPercentageValue()
                        : BigDecimal.ZERO;
                yield basicSalary.multiply(percentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            case SLAB_BASED -> component.getFixedAmount() != null
                    ? component.getFixedAmount()
                    : BigDecimal.ZERO;
        };
    }

    private String serializeBreakup(List<Map<String, Object>> breakupList) {
        try {
            return objectMapper.writeValueAsString(breakupList);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize component breakup", e);
            return "[]";
        }
    }

    private PayrollRunResponse mapToResponse(PayrollRun run) {
        return new PayrollRunResponse(
                run.getId(),
                run.getPeriodYear(),
                run.getPeriodMonth(),
                run.getRunDate(),
                run.getStatus().name(),
                run.getTotalGross(),
                run.getTotalDeductions(),
                run.getTotalNet(),
                run.getEmployeeCount(),
                run.getNotes(),
                run.getCreatedAt(),
                run.getUpdatedAt()
        );
    }

    private PayrollDetailResponse mapDetailToResponse(PayrollDetail detail) {
        var employee = detail.getEmployee();
        var salaryStructure = detail.getSalaryStructure();
        return new PayrollDetailResponse(
                detail.getId(),
                detail.getPayrollRun().getId(),
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getEmployeeCode(),
                salaryStructure.getId(),
                salaryStructure.getName(),
                detail.getBasicSalary(),
                detail.getGrossSalary(),
                detail.getTotalDeductions(),
                detail.getNetSalary(),
                detail.getComponentBreakup(),
                detail.getDaysPayable(),
                detail.getLopDays(),
                detail.getCreatedAt(),
                detail.getUpdatedAt()
        );
    }
}
