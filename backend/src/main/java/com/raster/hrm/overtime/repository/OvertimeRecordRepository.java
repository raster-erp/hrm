package com.raster.hrm.overtime.repository;

import com.raster.hrm.overtime.entity.OvertimeRecord;
import com.raster.hrm.overtime.entity.OvertimeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OvertimeRecordRepository extends JpaRepository<OvertimeRecord, Long> {

    Page<OvertimeRecord> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<OvertimeRecord> findByStatus(OvertimeStatus status, Pageable pageable);

    Page<OvertimeRecord> findByEmployeeIdAndStatus(Long employeeId, OvertimeStatus status, Pageable pageable);

    @Query("SELECT o FROM OvertimeRecord o WHERE o.overtimeDate BETWEEN :startDate AND :endDate")
    Page<OvertimeRecord> findByDateRange(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          Pageable pageable);

    @Query("SELECT o FROM OvertimeRecord o WHERE o.employee.id = :employeeId " +
           "AND o.overtimeDate BETWEEN :startDate AND :endDate")
    List<OvertimeRecord> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT o FROM OvertimeRecord o WHERE o.employee.id = :employeeId " +
           "AND o.overtimeDate = :overtimeDate")
    List<OvertimeRecord> findByEmployeeIdAndOvertimeDate(@Param("employeeId") Long employeeId,
                                                          @Param("overtimeDate") LocalDate overtimeDate);

    @Query("SELECT COALESCE(SUM(o.overtimeMinutes), 0) FROM OvertimeRecord o " +
           "WHERE o.employee.id = :employeeId AND o.status = :status " +
           "AND o.overtimeDate BETWEEN :startDate AND :endDate")
    Integer sumOvertimeMinutesByEmployeeAndStatusAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("status") OvertimeStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Page<OvertimeRecord> findAll(Pageable pageable);
}
