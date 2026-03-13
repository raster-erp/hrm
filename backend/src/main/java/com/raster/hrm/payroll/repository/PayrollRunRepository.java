package com.raster.hrm.payroll.repository;

import com.raster.hrm.payroll.entity.PayrollRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {

    Optional<PayrollRun> findByPeriodYearAndPeriodMonth(int periodYear, int periodMonth);

    Page<PayrollRun> findAllByOrderByPeriodYearDescPeriodMonthDesc(Pageable pageable);
}
