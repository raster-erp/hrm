package com.raster.hrm.contract.repository;

import com.raster.hrm.contract.entity.ContractStatus;
import com.raster.hrm.contract.entity.EmploymentContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<EmploymentContract, Long> {

    List<EmploymentContract> findByEmployeeId(Long employeeId);

    Page<EmploymentContract> findByStatus(ContractStatus status, Pageable pageable);

    List<EmploymentContract> findByEndDateBetween(LocalDate startDate, LocalDate endDate);
}
