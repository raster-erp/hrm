package com.raster.hrm.attendancedeviation.repository;

import com.raster.hrm.attendancedeviation.entity.AttendanceDeviation;
import com.raster.hrm.attendancedeviation.entity.DeviationStatus;
import com.raster.hrm.attendancedeviation.entity.DeviationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceDeviationRepository extends JpaRepository<AttendanceDeviation, Long> {

    Page<AttendanceDeviation> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<AttendanceDeviation> findByType(DeviationType type, Pageable pageable);

    Page<AttendanceDeviation> findByStatus(DeviationStatus status, Pageable pageable);

    @Query("SELECT d FROM AttendanceDeviation d WHERE d.deviationDate BETWEEN :startDate AND :endDate")
    Page<AttendanceDeviation> findByDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               Pageable pageable);

    @Query("SELECT d FROM AttendanceDeviation d WHERE d.employee.id = :employeeId " +
           "AND d.deviationDate BETWEEN :startDate AND :endDate")
    List<AttendanceDeviation> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT d FROM AttendanceDeviation d WHERE d.employee.id = :employeeId " +
           "AND d.deviationDate = :deviationDate AND d.type = :type")
    List<AttendanceDeviation> findByEmployeeIdAndDeviationDateAndType(@Param("employeeId") Long employeeId,
                                                                      @Param("deviationDate") LocalDate deviationDate,
                                                                      @Param("type") DeviationType type);

    Page<AttendanceDeviation> findAll(Pageable pageable);
}
