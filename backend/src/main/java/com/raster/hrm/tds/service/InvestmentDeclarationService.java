package com.raster.hrm.tds.service;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.tds.dto.InvestmentDeclarationItemResponse;
import com.raster.hrm.tds.dto.InvestmentDeclarationRequest;
import com.raster.hrm.tds.dto.InvestmentDeclarationResponse;
import com.raster.hrm.tds.dto.ProofSubmissionRequest;
import com.raster.hrm.tds.dto.ProofVerificationRequest;
import com.raster.hrm.tds.entity.DeclarationStatus;
import com.raster.hrm.tds.entity.InvestmentDeclaration;
import com.raster.hrm.tds.entity.InvestmentDeclarationItem;
import com.raster.hrm.tds.entity.ProofStatus;
import com.raster.hrm.tds.entity.TaxRegime;
import com.raster.hrm.tds.repository.InvestmentDeclarationItemRepository;
import com.raster.hrm.tds.repository.InvestmentDeclarationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class InvestmentDeclarationService {

    private static final Logger log = LoggerFactory.getLogger(InvestmentDeclarationService.class);

    private final InvestmentDeclarationRepository investmentDeclarationRepository;
    private final InvestmentDeclarationItemRepository investmentDeclarationItemRepository;
    private final EmployeeRepository employeeRepository;

    public InvestmentDeclarationService(InvestmentDeclarationRepository investmentDeclarationRepository,
                                        InvestmentDeclarationItemRepository investmentDeclarationItemRepository,
                                        EmployeeRepository employeeRepository) {
        this.investmentDeclarationRepository = investmentDeclarationRepository;
        this.investmentDeclarationItemRepository = investmentDeclarationItemRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<InvestmentDeclarationResponse> getAll(Pageable pageable) {
        return investmentDeclarationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public InvestmentDeclarationResponse getById(Long id) {
        var declaration = investmentDeclarationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InvestmentDeclaration", "id", id));
        var items = investmentDeclarationItemRepository.findByDeclarationId(declaration.getId());
        declaration.setItems(items);
        return mapToResponse(declaration);
    }

    @Transactional(readOnly = true)
    public InvestmentDeclarationResponse getByEmployeeAndYear(Long employeeId, String financialYear) {
        var declaration = investmentDeclarationRepository.findByEmployeeIdAndFinancialYear(employeeId, financialYear)
                .orElseThrow(() -> new ResourceNotFoundException("InvestmentDeclaration", "employeeId+financialYear",
                        employeeId + "/" + financialYear));
        var items = investmentDeclarationItemRepository.findByDeclarationId(declaration.getId());
        declaration.setItems(items);
        return mapToResponse(declaration);
    }

    @Transactional(readOnly = true)
    public Page<InvestmentDeclarationResponse> getByFinancialYear(String financialYear, Pageable pageable) {
        return investmentDeclarationRepository.findByFinancialYear(financialYear, pageable)
                .map(this::mapToResponse);
    }

    public InvestmentDeclarationResponse create(InvestmentDeclarationRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        if (investmentDeclarationRepository.existsByEmployeeIdAndFinancialYear(request.employeeId(), request.financialYear())) {
            throw new BadRequestException("Investment declaration already exists for employee '"
                    + request.employeeId() + "' and year '" + request.financialYear() + "'");
        }

        var declaration = new InvestmentDeclaration();
        declaration.setEmployee(employee);
        declaration.setFinancialYear(request.financialYear());
        declaration.setRegime(TaxRegime.valueOf(request.regime()));
        declaration.setRemarks(request.remarks());
        declaration.setStatus(DeclarationStatus.DRAFT);

        var totalDeclared = BigDecimal.ZERO;
        if (request.items() != null) {
            for (var itemRequest : request.items()) {
                var item = new InvestmentDeclarationItem();
                item.setDeclaration(declaration);
                item.setSection(itemRequest.section());
                item.setDescription(itemRequest.description());
                item.setDeclaredAmount(itemRequest.declaredAmount());
                declaration.getItems().add(item);
                totalDeclared = totalDeclared.add(itemRequest.declaredAmount());
            }
        }
        declaration.setTotalDeclaredAmount(totalDeclared.setScale(2, RoundingMode.HALF_UP));
        declaration.setTotalVerifiedAmount(BigDecimal.ZERO);

        var saved = investmentDeclarationRepository.save(declaration);
        log.info("Created investment declaration with id: {} for employee: {} year: {}",
                saved.getId(), employee.getId(), request.financialYear());
        return mapToResponse(saved);
    }

    public InvestmentDeclarationResponse update(Long id, InvestmentDeclarationRequest request) {
        var declaration = investmentDeclarationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InvestmentDeclaration", "id", id));

        if (declaration.getStatus() != DeclarationStatus.DRAFT) {
            throw new BadRequestException("Investment declaration can only be updated in DRAFT status. Current status: "
                    + declaration.getStatus());
        }

        declaration.setRegime(TaxRegime.valueOf(request.regime()));
        declaration.setRemarks(request.remarks());

        declaration.getItems().clear();

        var totalDeclared = BigDecimal.ZERO;
        if (request.items() != null) {
            for (var itemRequest : request.items()) {
                var item = new InvestmentDeclarationItem();
                item.setDeclaration(declaration);
                item.setSection(itemRequest.section());
                item.setDescription(itemRequest.description());
                item.setDeclaredAmount(itemRequest.declaredAmount());
                declaration.getItems().add(item);
                totalDeclared = totalDeclared.add(itemRequest.declaredAmount());
            }
        }
        declaration.setTotalDeclaredAmount(totalDeclared.setScale(2, RoundingMode.HALF_UP));

        var saved = investmentDeclarationRepository.save(declaration);
        log.info("Updated investment declaration with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public InvestmentDeclarationResponse submit(Long id) {
        var declaration = investmentDeclarationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InvestmentDeclaration", "id", id));

        if (declaration.getStatus() != DeclarationStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT declarations can be submitted. Current status: "
                    + declaration.getStatus());
        }

        declaration.setStatus(DeclarationStatus.SUBMITTED);
        declaration.setSubmittedAt(LocalDateTime.now());

        var saved = investmentDeclarationRepository.save(declaration);
        log.info("Submitted investment declaration with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public InvestmentDeclarationResponse verify(Long id, Long verifiedById) {
        var declaration = investmentDeclarationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InvestmentDeclaration", "id", id));

        if (declaration.getStatus() != DeclarationStatus.SUBMITTED) {
            throw new BadRequestException("Only SUBMITTED declarations can be verified. Current status: "
                    + declaration.getStatus());
        }

        var items = investmentDeclarationItemRepository.findByDeclarationId(declaration.getId());
        var totalVerified = items.stream()
                .map(InvestmentDeclarationItem::getVerifiedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        declaration.setStatus(DeclarationStatus.VERIFIED);
        declaration.setVerifiedAt(LocalDateTime.now());
        declaration.setVerifiedBy(verifiedById);
        declaration.setTotalVerifiedAmount(totalVerified);

        var saved = investmentDeclarationRepository.save(declaration);
        log.info("Verified investment declaration with id: {} by user: {}", saved.getId(), verifiedById);
        return mapToResponse(saved);
    }

    public InvestmentDeclarationResponse reject(Long id, String remarks) {
        var declaration = investmentDeclarationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InvestmentDeclaration", "id", id));

        if (declaration.getStatus() != DeclarationStatus.SUBMITTED) {
            throw new BadRequestException("Only SUBMITTED declarations can be rejected. Current status: "
                    + declaration.getStatus());
        }

        declaration.setStatus(DeclarationStatus.REJECTED);
        declaration.setRemarks(remarks);

        var saved = investmentDeclarationRepository.save(declaration);
        log.info("Rejected investment declaration with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public void submitProof(ProofSubmissionRequest request) {
        var item = investmentDeclarationItemRepository.findById(request.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("InvestmentDeclarationItem", "id", request.itemId()));

        item.setProofDocumentName(request.proofDocumentName());
        item.setProofStatus(ProofStatus.SUBMITTED);
        if (request.declaredAmount() != null) {
            item.setDeclaredAmount(request.declaredAmount());
        }

        investmentDeclarationItemRepository.save(item);
        log.info("Submitted proof for declaration item id: {}", request.itemId());
    }

    public void verifyProof(ProofVerificationRequest request) {
        var item = investmentDeclarationItemRepository.findById(request.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("InvestmentDeclarationItem", "id", request.itemId()));

        item.setVerifiedAmount(request.verifiedAmount());
        item.setProofStatus(ProofStatus.valueOf(request.status()));
        item.setProofRemarks(request.remarks());

        investmentDeclarationItemRepository.save(item);
        log.info("Verified proof for declaration item id: {} with status: {}", request.itemId(), request.status());
    }

    public void delete(Long id) {
        var declaration = investmentDeclarationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InvestmentDeclaration", "id", id));
        investmentDeclarationRepository.delete(declaration);
        log.info("Deleted investment declaration with id: {}", id);
    }

    private InvestmentDeclarationResponse mapToResponse(InvestmentDeclaration declaration) {
        List<InvestmentDeclarationItemResponse> itemResponses = declaration.getItems().stream()
                .map(this::mapItemToResponse)
                .toList();

        var employee = declaration.getEmployee();
        var employeeName = employee.getFirstName() + " " + employee.getLastName();

        return new InvestmentDeclarationResponse(
                declaration.getId(),
                employee.getId(),
                employeeName,
                declaration.getFinancialYear(),
                declaration.getRegime().name(),
                declaration.getTotalDeclaredAmount(),
                declaration.getTotalVerifiedAmount(),
                declaration.getStatus().name(),
                declaration.getRemarks(),
                declaration.getSubmittedAt(),
                declaration.getVerifiedAt(),
                declaration.getVerifiedBy(),
                itemResponses,
                declaration.getCreatedAt(),
                declaration.getUpdatedAt()
        );
    }

    private InvestmentDeclarationItemResponse mapItemToResponse(InvestmentDeclarationItem item) {
        return new InvestmentDeclarationItemResponse(
                item.getId(),
                item.getSection(),
                item.getDescription(),
                item.getDeclaredAmount(),
                item.getVerifiedAmount(),
                item.getProofStatus().name(),
                item.getProofDocumentName(),
                item.getProofRemarks(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
