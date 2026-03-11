package com.raster.hrm.attendanceregularization.repository;

import com.raster.hrm.attendanceregularization.entity.RegularizationRequest;
import com.raster.hrm.attendanceregularization.entity.RegularizationStatus;
import com.raster.hrm.attendanceregularization.entity.RegularizationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegularizationRequestRepository extends JpaRepository<RegularizationRequest, Long> {

    Page<RegularizationRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<RegularizationRequest> findByStatus(RegularizationStatus status, Pageable pageable);

    Page<RegularizationRequest> findByType(RegularizationType type, Pageable pageable);

    @Query("SELECT r FROM RegularizationRequest r WHERE r.requestDate BETWEEN :startDate AND :endDate")
    Page<RegularizationRequest> findByDateRange(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 Pageable pageable);

    @Query("SELECT r FROM RegularizationRequest r WHERE r.employee.id = :employeeId " +
           "AND r.requestDate BETWEEN :startDate AND :endDate")
    List<RegularizationRequest> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);

    Page<RegularizationRequest> findAll(Pageable pageable);
}
