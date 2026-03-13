package com.raster.hrm.payroll.repository;

import com.raster.hrm.payroll.entity.PayrollAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollAdjustmentRepository extends JpaRepository<PayrollAdjustment, Long> {

    List<PayrollAdjustment> findByPayrollRunId(Long payrollRunId);

    List<PayrollAdjustment> findByPayrollRunIdAndEmployeeId(Long payrollRunId, Long employeeId);
}
