package com.raster.hrm.leaveplan.repository;

import com.raster.hrm.leaveplan.entity.LeavePlan;
import com.raster.hrm.leaveplan.entity.LeavePlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeavePlanRepository extends JpaRepository<LeavePlan, Long> {

    Page<LeavePlan> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<LeavePlan> findByStatus(LeavePlanStatus status, Pageable pageable);

    Page<LeavePlan> findByEmployeeIdAndStatus(Long employeeId, LeavePlanStatus status, Pageable pageable);

    List<LeavePlan> findByPlannedFromDateGreaterThanEqualAndPlannedToDateLessThanEqual(
            LocalDate start, LocalDate end);

    List<LeavePlan> findByEmployeeIdAndPlannedFromDateGreaterThanEqualAndPlannedToDateLessThanEqual(
            Long employeeId, LocalDate start, LocalDate end);

    List<LeavePlan> findByEmployee_Department_IdAndPlannedFromDateLessThanEqualAndPlannedToDateGreaterThanEqual(
            Long departmentId, LocalDate end, LocalDate start);
}
