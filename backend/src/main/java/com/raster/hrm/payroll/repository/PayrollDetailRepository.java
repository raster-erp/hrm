package com.raster.hrm.payroll.repository;

import com.raster.hrm.payroll.entity.PayrollDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollDetailRepository extends JpaRepository<PayrollDetail, Long> {

    List<PayrollDetail> findByPayrollRunId(Long payrollRunId);

    Page<PayrollDetail> findByPayrollRunId(Long payrollRunId, Pageable pageable);

    Optional<PayrollDetail> findByPayrollRunIdAndEmployeeId(Long payrollRunId, Long employeeId);
}
