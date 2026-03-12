package com.raster.hrm.compoff.repository;

import com.raster.hrm.compoff.entity.CompOffCredit;
import com.raster.hrm.compoff.entity.CompOffStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompOffCreditRepository extends JpaRepository<CompOffCredit, Long> {

    Page<CompOffCredit> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<CompOffCredit> findByStatus(CompOffStatus status, Pageable pageable);

    Page<CompOffCredit> findByEmployeeIdAndStatus(Long employeeId, CompOffStatus status, Pageable pageable);

    Optional<CompOffCredit> findByEmployeeIdAndWorkedDateAndStatusIn(
            Long employeeId, LocalDate workedDate, List<CompOffStatus> statuses);

    long countByEmployeeIdAndStatus(Long employeeId, CompOffStatus status);

    List<CompOffCredit> findByStatusAndExpiryDateBefore(CompOffStatus status, LocalDate date);

    List<CompOffCredit> findByEmployeeIdAndStatusOrderByExpiryDateAsc(Long employeeId, CompOffStatus status);
}
