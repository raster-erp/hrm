package com.raster.hrm.contract.service;

import com.raster.hrm.contract.dto.ContractAmendmentRequest;
import com.raster.hrm.contract.dto.ContractAmendmentResponse;
import com.raster.hrm.contract.dto.ContractRequest;
import com.raster.hrm.contract.dto.ContractResponse;
import com.raster.hrm.contract.entity.ContractAmendment;
import com.raster.hrm.contract.entity.ContractStatus;
import com.raster.hrm.contract.entity.ContractType;
import com.raster.hrm.contract.entity.EmploymentContract;
import com.raster.hrm.contract.repository.ContractAmendmentRepository;
import com.raster.hrm.contract.repository.ContractRepository;
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
import java.util.List;

@Service
@Transactional
public class ContractService {

    private static final Logger log = LoggerFactory.getLogger(ContractService.class);

    private final ContractRepository contractRepository;
    private final ContractAmendmentRepository amendmentRepository;
    private final EmployeeRepository employeeRepository;

    public ContractService(ContractRepository contractRepository,
                           ContractAmendmentRepository amendmentRepository,
                           EmployeeRepository employeeRepository) {
        this.contractRepository = contractRepository;
        this.amendmentRepository = amendmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getAll(Pageable pageable) {
        return contractRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ContractResponse getById(Long id) {
        var contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));
        return mapToResponse(contract);
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getByEmployee(Long employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        return contractRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getExpiringContracts(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before or equal to end date");
        }
        return contractRepository.findByEndDateBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ContractResponse create(ContractRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var contract = new EmploymentContract();
        mapRequestToEntity(request, contract, employee);

        var saved = contractRepository.save(contract);
        log.info("Created contract with id: {} for employee: {}", saved.getId(), employee.getEmployeeCode());
        return mapToResponse(saved);
    }

    public ContractResponse update(Long id, ContractRequest request) {
        var contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));

        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        mapRequestToEntity(request, contract, employee);

        var saved = contractRepository.save(contract);
        log.info("Updated contract with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public ContractResponse renewContract(Long id, ContractRequest request) {
        var oldContract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));

        if (oldContract.getStatus() != ContractStatus.ACTIVE) {
            throw new BadRequestException("Only active contracts can be renewed");
        }

        oldContract.setStatus(ContractStatus.RENEWED);
        contractRepository.save(oldContract);
        log.info("Set contract id: {} to RENEWED status", id);

        var employee = oldContract.getEmployee();
        var newContract = new EmploymentContract();
        mapRequestToEntity(request, newContract, employee);
        newContract.setStatus(ContractStatus.ACTIVE);

        var saved = contractRepository.save(newContract);
        log.info("Created renewed contract with id: {} for employee: {}", saved.getId(), employee.getEmployeeCode());
        return mapToResponse(saved);
    }

    public ContractAmendmentResponse addAmendment(Long contractId, ContractAmendmentRequest request) {
        var contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));

        var amendment = new ContractAmendment();
        amendment.setContract(contract);
        amendment.setAmendmentDate(request.amendmentDate());
        amendment.setDescription(request.description());
        amendment.setOldTerms(request.oldTerms());
        amendment.setNewTerms(request.newTerms());

        var saved = amendmentRepository.save(amendment);
        log.info("Added amendment id: {} to contract id: {}", saved.getId(), contractId);
        return mapToAmendmentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ContractAmendmentResponse> getAmendments(Long contractId) {
        contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));
        return amendmentRepository.findByContractId(contractId).stream()
                .map(this::mapToAmendmentResponse)
                .toList();
    }

    private void mapRequestToEntity(ContractRequest request, EmploymentContract contract, Employee employee) {
        contract.setEmployee(employee);
        contract.setContractType(ContractType.valueOf(request.contractType()));
        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());
        contract.setTerms(request.terms());

        if (request.status() != null && !request.status().isBlank()) {
            contract.setStatus(ContractStatus.valueOf(request.status()));
        }
    }

    private ContractResponse mapToResponse(EmploymentContract contract) {
        var employee = contract.getEmployee();
        return new ContractResponse(
                contract.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                contract.getContractType().name(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getTerms(),
                contract.getStatus().name(),
                contract.getCreatedAt(),
                contract.getUpdatedAt()
        );
    }

    private ContractAmendmentResponse mapToAmendmentResponse(ContractAmendment amendment) {
        return new ContractAmendmentResponse(
                amendment.getId(),
                amendment.getContract().getId(),
                amendment.getAmendmentDate(),
                amendment.getDescription(),
                amendment.getOldTerms(),
                amendment.getNewTerms(),
                amendment.getCreatedAt()
        );
    }
}
