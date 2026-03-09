package com.raster.hrm.employee.repository;

import com.raster.hrm.employee.entity.Employee;
import com.raster.hrm.employee.entity.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmail(String email);

    Page<Employee> findByDeletedFalse(Pageable pageable);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);

    List<Employee> findByDepartmentIdAndDeletedFalse(Long departmentId);

    List<Employee> findByEmploymentStatusAndDeletedFalse(EmploymentStatus employmentStatus);
}
