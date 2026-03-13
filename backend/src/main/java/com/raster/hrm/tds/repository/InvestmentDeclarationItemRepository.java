package com.raster.hrm.tds.repository;

import com.raster.hrm.tds.entity.InvestmentDeclarationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentDeclarationItemRepository extends JpaRepository<InvestmentDeclarationItem, Long> {
    List<InvestmentDeclarationItem> findByDeclarationId(Long declarationId);
}
