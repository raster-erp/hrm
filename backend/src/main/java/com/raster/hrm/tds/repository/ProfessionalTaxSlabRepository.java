package com.raster.hrm.tds.repository;

import com.raster.hrm.tds.entity.ProfessionalTaxSlab;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProfessionalTaxSlabRepository extends JpaRepository<ProfessionalTaxSlab, Long> {
    List<ProfessionalTaxSlab> findByStateAndActiveOrderBySlabFromAsc(String state, boolean active);
    List<ProfessionalTaxSlab> findByState(String state);
    Page<ProfessionalTaxSlab> findAll(Pageable pageable);
    boolean existsByStateAndSlabFrom(String state, BigDecimal slabFrom);
}
