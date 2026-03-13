package com.raster.hrm.tds.repository;

import com.raster.hrm.tds.entity.InvestmentDeclaration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentDeclarationRepository extends JpaRepository<InvestmentDeclaration, Long> {
    Optional<InvestmentDeclaration> findByEmployeeIdAndFinancialYear(Long employeeId, String financialYear);
    List<InvestmentDeclaration> findByFinancialYear(String financialYear);
    Page<InvestmentDeclaration> findByFinancialYear(String financialYear, Pageable pageable);
    boolean existsByEmployeeIdAndFinancialYear(Long employeeId, String financialYear);
}
