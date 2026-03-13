package com.raster.hrm.tds.repository;

import com.raster.hrm.tds.entity.TaxRegime;
import com.raster.hrm.tds.entity.TaxSlab;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxSlabRepository extends JpaRepository<TaxSlab, Long> {
    List<TaxSlab> findByRegimeAndFinancialYearAndActiveOrderBySlabFromAsc(TaxRegime regime, String financialYear, boolean active);
    List<TaxSlab> findByRegimeAndFinancialYearOrderBySlabFromAsc(TaxRegime regime, String financialYear);
    List<TaxSlab> findByFinancialYear(String financialYear);
    Page<TaxSlab> findAll(Pageable pageable);
    boolean existsByRegimeAndFinancialYearAndSlabFrom(TaxRegime regime, String financialYear, java.math.BigDecimal slabFrom);
}
