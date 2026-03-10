package com.raster.hrm.shiftroster.repository;

import com.raster.hrm.shiftroster.entity.ShiftRoster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftRosterRepository extends JpaRepository<ShiftRoster, Long> {

    List<ShiftRoster> findByEmployeeId(Long employeeId);

    List<ShiftRoster> findByShiftId(Long shiftId);

    @Query("SELECT sr FROM ShiftRoster sr WHERE sr.employee.id = :employeeId " +
           "AND sr.effectiveDate <= :endDate " +
           "AND (sr.endDate IS NULL OR sr.endDate >= :startDate)")
    List<ShiftRoster> findOverlapping(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT sr FROM ShiftRoster sr WHERE sr.employee.id = :employeeId " +
           "AND sr.id <> :excludeId " +
           "AND sr.effectiveDate <= :endDate " +
           "AND (sr.endDate IS NULL OR sr.endDate >= :startDate)")
    List<ShiftRoster> findOverlappingExcluding(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId);

    Page<ShiftRoster> findAll(Pageable pageable);

    Page<ShiftRoster> findByEmployeeId(Long employeeId, Pageable pageable);
}
