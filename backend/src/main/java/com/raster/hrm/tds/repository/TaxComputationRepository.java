package com.raster.hrm.tds.repository;

import com.raster.hrm.tds.entity.TaxComputation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxComputationRepository extends JpaRepository<TaxComputation, Long> {
    List<TaxComputation> findByEmployeeIdAndFinancialYearOrderByMonthAsc(Long employeeId, String financialYear);
    Optional<TaxComputation> findByEmployeeIdAndFinancialYearAndMonth(Long employeeId, String financialYear, int month);
}
