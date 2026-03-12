package com.raster.hrm.employeesalary.repository;

import com.raster.hrm.employeesalary.entity.EmployeeSalaryDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeSalaryDetailRepository extends JpaRepository<EmployeeSalaryDetail, Long> {

    List<EmployeeSalaryDetail> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);

    Page<EmployeeSalaryDetail> findAll(Pageable pageable);
}
