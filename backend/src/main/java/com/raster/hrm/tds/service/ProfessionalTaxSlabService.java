package com.raster.hrm.tds.service;

import com.raster.hrm.employee.repository.EmployeeRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.tds.dto.ProfessionalTaxSlabRequest;
import com.raster.hrm.tds.dto.ProfessionalTaxSlabResponse;
import com.raster.hrm.tds.entity.ProfessionalTaxSlab;
import com.raster.hrm.tds.repository.ProfessionalTaxSlabRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class ProfessionalTaxSlabService {

    private static final Logger log = LoggerFactory.getLogger(ProfessionalTaxSlabService.class);
    private static final int FEBRUARY = 2;

    private final ProfessionalTaxSlabRepository professionalTaxSlabRepository;
    private final EmployeeRepository employeeRepository;

    public ProfessionalTaxSlabService(ProfessionalTaxSlabRepository professionalTaxSlabRepository,
                                       EmployeeRepository employeeRepository) {
        this.professionalTaxSlabRepository = professionalTaxSlabRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProfessionalTaxSlabResponse> getAll(Pageable pageable) {
        return professionalTaxSlabRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ProfessionalTaxSlabResponse getById(Long id) {
        var slab = professionalTaxSlabRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalTaxSlab", "id", id));
        return mapToResponse(slab);
    }

    @Transactional(readOnly = true)
    public List<ProfessionalTaxSlabResponse> getByState(String state) {
        return professionalTaxSlabRepository.findByState(state).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProfessionalTaxSlabResponse create(ProfessionalTaxSlabRequest request) {
        if (professionalTaxSlabRepository.existsByStateAndSlabFrom(request.state(), request.slabFrom())) {
            throw new BadRequestException("Professional tax slab already exists for state '"
                    + request.state() + "' with slabFrom '" + request.slabFrom() + "'");
        }

        var slab = new ProfessionalTaxSlab();
        slab.setState(request.state());
        slab.setSlabFrom(request.slabFrom());
        slab.setSlabTo(request.slabTo());
        slab.setMonthlyTax(request.monthlyTax());
        slab.setFebruaryTax(request.februaryTax());

        var saved = professionalTaxSlabRepository.save(slab);
        log.info("Created professional tax slab with id: {} for state: {}", saved.getId(), saved.getState());
        return mapToResponse(saved);
    }

    public ProfessionalTaxSlabResponse update(Long id, ProfessionalTaxSlabRequest request) {
        var slab = professionalTaxSlabRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalTaxSlab", "id", id));

        boolean keyChanged = !slab.getState().equals(request.state())
                || slab.getSlabFrom().compareTo(request.slabFrom()) != 0;
        if (keyChanged && professionalTaxSlabRepository.existsByStateAndSlabFrom(request.state(), request.slabFrom())) {
            throw new BadRequestException("Professional tax slab already exists for state '"
                    + request.state() + "' with slabFrom '" + request.slabFrom() + "'");
        }

        slab.setState(request.state());
        slab.setSlabFrom(request.slabFrom());
        slab.setSlabTo(request.slabTo());
        slab.setMonthlyTax(request.monthlyTax());
        slab.setFebruaryTax(request.februaryTax());

        var saved = professionalTaxSlabRepository.save(slab);
        log.info("Updated professional tax slab with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var slab = professionalTaxSlabRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalTaxSlab", "id", id));
        professionalTaxSlabRepository.delete(slab);
        log.info("Deleted professional tax slab with id: {}", id);
    }

    @Transactional(readOnly = true)
    public BigDecimal computeProfessionalTax(Long employeeId, int month) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        var state = employee.getState();
        if (state == null || state.isBlank()) {
            throw new BadRequestException("Employee state is not set for employee id: " + employeeId);
        }

        var slabs = professionalTaxSlabRepository.findByStateAndActiveOrderBySlabFromAsc(state, true);
        if (slabs.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        var monthlySalary = employee.getBasicSalary();
        for (var slab : slabs) {
            var withinFrom = monthlySalary.compareTo(slab.getSlabFrom()) >= 0;
            var withinTo = slab.getSlabTo() == null || monthlySalary.compareTo(slab.getSlabTo()) <= 0;

            if (withinFrom && withinTo) {
                if (month == FEBRUARY && slab.getFebruaryTax() != null) {
                    return slab.getFebruaryTax().setScale(2, RoundingMode.HALF_UP);
                }
                return slab.getMonthlyTax().setScale(2, RoundingMode.HALF_UP);
            }
        }

        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private ProfessionalTaxSlabResponse mapToResponse(ProfessionalTaxSlab slab) {
        return new ProfessionalTaxSlabResponse(
                slab.getId(),
                slab.getState(),
                slab.getSlabFrom(),
                slab.getSlabTo(),
                slab.getMonthlyTax(),
                slab.getFebruaryTax(),
                slab.isActive(),
                slab.getCreatedAt(),
                slab.getUpdatedAt()
        );
    }
}
