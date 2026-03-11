package com.raster.hrm.wfh.repository;

import com.raster.hrm.wfh.entity.WfhRequest;
import com.raster.hrm.wfh.entity.WfhStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WfhRequestRepository extends JpaRepository<WfhRequest, Long> {

    Page<WfhRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<WfhRequest> findByStatus(WfhStatus status, Pageable pageable);

    @Query("SELECT w FROM WfhRequest w WHERE w.requestDate BETWEEN :startDate AND :endDate")
    Page<WfhRequest> findByDateRange(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     Pageable pageable);

    @Query("SELECT w FROM WfhRequest w WHERE w.employee.id = :employeeId AND w.requestDate BETWEEN :startDate AND :endDate")
    List<WfhRequest> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    List<WfhRequest> findByRequestDateAndStatus(LocalDate requestDate, WfhStatus status);

    Page<WfhRequest> findAll(Pageable pageable);
}
