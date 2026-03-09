package com.raster.hrm.idcard.service;

import com.raster.hrm.idcard.dto.IdCardRequest;
import com.raster.hrm.idcard.dto.IdCardResponse;
import com.raster.hrm.idcard.entity.IdCard;
import com.raster.hrm.idcard.entity.IdCardStatus;
import com.raster.hrm.idcard.repository.IdCardRepository;
import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class IdCardService {

    private static final Logger log = LoggerFactory.getLogger(IdCardService.class);

    private final IdCardRepository idCardRepository;
    private final EmployeeRepository employeeRepository;

    public IdCardService(IdCardRepository idCardRepository,
                         EmployeeRepository employeeRepository) {
        this.idCardRepository = idCardRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<IdCardResponse> getAll(Pageable pageable) {
        return idCardRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public IdCardResponse getById(Long id) {
        var idCard = idCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IdCard", "id", id));
        return mapToResponse(idCard);
    }

    @Transactional(readOnly = true)
    public List<IdCardResponse> getByEmployeeId(Long employeeId) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        return idCardRepository.findByEmployeeId(employee.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public IdCardResponse create(IdCardRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        var idCard = new IdCard();
        idCard.setEmployee(employee);
        idCard.setCardNumber(generateCardNumber());
        idCard.setIssueDate(request.issueDate());
        idCard.setExpiryDate(request.expiryDate());

        var saved = idCardRepository.save(idCard);
        log.info("Created ID card with id: {} for employee: {}", saved.getId(), employee.getEmployeeCode());
        return mapToResponse(saved);
    }

    public IdCardResponse update(Long id, IdCardRequest request) {
        var idCard = idCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IdCard", "id", id));

        var employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        idCard.setEmployee(employee);
        idCard.setIssueDate(request.issueDate());
        idCard.setExpiryDate(request.expiryDate());

        var saved = idCardRepository.save(idCard);
        log.info("Updated ID card with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public IdCardResponse updateStatus(Long id, IdCardStatus status) {
        var idCard = idCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IdCard", "id", id));

        idCard.setStatus(status);
        var saved = idCardRepository.save(idCard);
        log.info("Updated status of ID card id: {} to {}", id, status);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var idCard = idCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IdCard", "id", id));
        idCardRepository.delete(idCard);
        log.info("Deleted ID card with id: {}", id);
    }

    private String generateCardNumber() {
        String cardNumber;
        do {
            cardNumber = "IDC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (idCardRepository.existsByCardNumber(cardNumber));
        return cardNumber;
    }

    private IdCardResponse mapToResponse(IdCard idCard) {
        var employee = idCard.getEmployee();
        return new IdCardResponse(
                idCard.getId(),
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName() + " " + employee.getLastName(),
                idCard.getCardNumber(),
                idCard.getIssueDate(),
                idCard.getExpiryDate(),
                idCard.getStatus().name(),
                idCard.getCreatedAt(),
                idCard.getUpdatedAt()
        );
    }
}
