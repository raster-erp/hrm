package com.raster.hrm.tds.service;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.tds.dto.TaxSlabRequest;
import com.raster.hrm.tds.dto.TaxSlabResponse;
import com.raster.hrm.tds.entity.TaxRegime;
import com.raster.hrm.tds.entity.TaxSlab;
import com.raster.hrm.tds.repository.TaxSlabRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaxSlabService {

    private static final Logger log = LoggerFactory.getLogger(TaxSlabService.class);

    private final TaxSlabRepository taxSlabRepository;

    public TaxSlabService(TaxSlabRepository taxSlabRepository) {
        this.taxSlabRepository = taxSlabRepository;
    }

    @Transactional(readOnly = true)
    public Page<TaxSlabResponse> getAll(Pageable pageable) {
        return taxSlabRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public TaxSlabResponse getById(Long id) {
        var slab = taxSlabRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaxSlab", "id", id));
        return mapToResponse(slab);
    }

    @Transactional(readOnly = true)
    public List<TaxSlabResponse> getByRegimeAndYear(String regime, String financialYear) {
        var taxRegime = TaxRegime.valueOf(regime);
        return taxSlabRepository.findByRegimeAndFinancialYearAndActiveOrderBySlabFromAsc(taxRegime, financialYear, true)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaxSlabResponse create(TaxSlabRequest request) {
        var taxRegime = TaxRegime.valueOf(request.regime());
        if (taxSlabRepository.existsByRegimeAndFinancialYearAndSlabFrom(taxRegime, request.financialYear(), request.slabFrom())) {
            throw new BadRequestException("Tax slab already exists for regime '" + request.regime()
                    + "', year '" + request.financialYear() + "', slabFrom '" + request.slabFrom() + "'");
        }

        var slab = new TaxSlab();
        slab.setRegime(taxRegime);
        slab.setFinancialYear(request.financialYear());
        slab.setSlabFrom(request.slabFrom());
        slab.setSlabTo(request.slabTo());
        slab.setRate(request.rate());
        slab.setDescription(request.description());

        var saved = taxSlabRepository.save(slab);
        log.info("Created tax slab with id: {} for regime: {} year: {}", saved.getId(), saved.getRegime(), saved.getFinancialYear());
        return mapToResponse(saved);
    }

    public TaxSlabResponse update(Long id, TaxSlabRequest request) {
        var slab = taxSlabRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaxSlab", "id", id));

        var taxRegime = TaxRegime.valueOf(request.regime());
        boolean keyChanged = !slab.getRegime().equals(taxRegime)
                || !slab.getFinancialYear().equals(request.financialYear())
                || slab.getSlabFrom().compareTo(request.slabFrom()) != 0;
        if (keyChanged && taxSlabRepository.existsByRegimeAndFinancialYearAndSlabFrom(taxRegime, request.financialYear(), request.slabFrom())) {
            throw new BadRequestException("Tax slab already exists for regime '" + request.regime()
                    + "', year '" + request.financialYear() + "', slabFrom '" + request.slabFrom() + "'");
        }

        slab.setRegime(taxRegime);
        slab.setFinancialYear(request.financialYear());
        slab.setSlabFrom(request.slabFrom());
        slab.setSlabTo(request.slabTo());
        slab.setRate(request.rate());
        slab.setDescription(request.description());

        var saved = taxSlabRepository.save(slab);
        log.info("Updated tax slab with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var slab = taxSlabRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaxSlab", "id", id));
        taxSlabRepository.delete(slab);
        log.info("Deleted tax slab with id: {}", id);
    }

    private TaxSlabResponse mapToResponse(TaxSlab slab) {
        return new TaxSlabResponse(
                slab.getId(),
                slab.getRegime().name(),
                slab.getFinancialYear(),
                slab.getSlabFrom(),
                slab.getSlabTo(),
                slab.getRate(),
                slab.getDescription(),
                slab.isActive(),
                slab.getCreatedAt(),
                slab.getUpdatedAt()
        );
    }
}
