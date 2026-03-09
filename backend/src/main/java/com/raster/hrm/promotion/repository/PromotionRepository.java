package com.raster.hrm.promotion.repository;

import com.raster.hrm.promotion.entity.Promotion;
import com.raster.hrm.promotion.entity.PromotionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findByEmployeeId(Long employeeId);

    List<Promotion> findByStatus(PromotionStatus status);

    Page<Promotion> findByStatus(PromotionStatus status, Pageable pageable);
}
