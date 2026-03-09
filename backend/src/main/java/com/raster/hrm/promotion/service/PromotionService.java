package com.raster.hrm.promotion.service;

import com.raster.hrm.designation.entity.Designation;
import com.raster.hrm.designation.repository.DesignationRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.promotion.dto.PromotionRequest;
import com.raster.hrm.promotion.dto.PromotionResponse;
import com.raster.hrm.promotion.entity.Promotion;
import com.raster.hrm.promotion.entity.PromotionStatus;
import com.raster.hrm.promotion.repository.PromotionRepository;
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
public class PromotionService {

    private static final Logger log = LoggerFactory.getLogger(PromotionService.class);

    private final PromotionRepository promotionRepository;
    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;

    public PromotionService(PromotionRepository promotionRepository,
                            EmployeeRepository employeeRepository,
                            DesignationRepository designationRepository) {
        this.promotionRepository = promotionRepository;
        this.employeeRepository = employeeRepository;
        this.designationRepository = designationRepository;
    }

    @Transactional(readOnly = true)
    public Page<PromotionResponse> getAll(Pageable pageable) {
        return promotionRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public PromotionResponse getById(Long id) {
        var promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
        return mapToResponse(promotion);
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getByEmployeeId(Long employeeId) {
        return promotionRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getPendingPromotions() {
        return promotionRepository.findByStatus(PromotionStatus.PENDING).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PromotionResponse create(PromotionRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var promotion = new Promotion();
        promotion.setEmployee(employee);
        promotion.setOldGrade(request.oldGrade());
        promotion.setNewGrade(request.newGrade());
        promotion.setEffectiveDate(request.effectiveDate());
        promotion.setReason(request.reason());
        promotion.setStatus(PromotionStatus.PENDING);

        if (request.oldDesignationId() != null) {
            var oldDesignation = designationRepository.findById(request.oldDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.oldDesignationId()));
            promotion.setOldDesignation(oldDesignation);
        }

        if (request.newDesignationId() != null) {
            var newDesignation = designationRepository.findById(request.newDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.newDesignationId()));
            promotion.setNewDesignation(newDesignation);
        }

        var saved = promotionRepository.save(promotion);
        log.info("Created promotion with id: {} for employee id: {}", saved.getId(), employee.getId());
        return mapToResponse(saved);
    }

    public PromotionResponse approve(Long id, Long approvedById) {
        var promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        if (promotion.getStatus() != PromotionStatus.PENDING) {
            throw new BadRequestException("Promotion can only be approved when in PENDING status");
        }

        var approver = employeeRepository.findById(approvedById)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", approvedById));

        promotion.setStatus(PromotionStatus.APPROVED);
        promotion.setApprovedBy(approver);
        promotion.setApprovedAt(LocalDateTime.now());

        var saved = promotionRepository.save(promotion);
        log.info("Approved promotion with id: {} by employee id: {}", saved.getId(), approvedById);
        return mapToResponse(saved);
    }

    public PromotionResponse reject(Long id, Long approvedById) {
        var promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        if (promotion.getStatus() != PromotionStatus.PENDING) {
            throw new BadRequestException("Promotion can only be rejected when in PENDING status");
        }

        var approver = employeeRepository.findById(approvedById)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", approvedById));

        promotion.setStatus(PromotionStatus.REJECTED);
        promotion.setApprovedBy(approver);
        promotion.setApprovedAt(LocalDateTime.now());

        var saved = promotionRepository.save(promotion);
        log.info("Rejected promotion with id: {} by employee id: {}", saved.getId(), approvedById);
        return mapToResponse(saved);
    }

    public PromotionResponse execute(Long id) {
        var promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        if (promotion.getStatus() != PromotionStatus.APPROVED) {
            throw new BadRequestException("Promotion must be in APPROVED status before execution");
        }

        promotion.setStatus(PromotionStatus.EXECUTED);

        if (promotion.getNewDesignation() != null) {
            var employee = promotion.getEmployee();
            employee.setDesignation(promotion.getNewDesignation());
            employeeRepository.save(employee);
        }

        var saved = promotionRepository.save(promotion);
        log.info("Executed promotion with id: {} for employee id: {}", saved.getId(), promotion.getEmployee().getId());
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        if (promotion.getStatus() != PromotionStatus.PENDING) {
            throw new BadRequestException("Only promotions in PENDING status can be deleted");
        }

        promotionRepository.delete(promotion);
        log.info("Deleted promotion with id: {}", id);
    }

    private PromotionResponse mapToResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getEmployee().getId(),
                promotion.getEmployee().getEmployeeCode(),
                promotion.getEmployee().getFirstName() + " " + promotion.getEmployee().getLastName(),
                promotion.getOldDesignation() != null ? promotion.getOldDesignation().getId() : null,
                promotion.getOldDesignation() != null ? promotion.getOldDesignation().getTitle() : null,
                promotion.getNewDesignation() != null ? promotion.getNewDesignation().getId() : null,
                promotion.getNewDesignation() != null ? promotion.getNewDesignation().getTitle() : null,
                promotion.getOldGrade(),
                promotion.getNewGrade(),
                promotion.getEffectiveDate(),
                promotion.getStatus().name(),
                promotion.getReason(),
                promotion.getApprovedBy() != null ? promotion.getApprovedBy().getId() : null,
                promotion.getApprovedBy() != null
                        ? promotion.getApprovedBy().getFirstName() + " " + promotion.getApprovedBy().getLastName()
                        : null,
                promotion.getApprovedAt(),
                promotion.getCreatedAt(),
                promotion.getUpdatedAt()
        );
    }
}
