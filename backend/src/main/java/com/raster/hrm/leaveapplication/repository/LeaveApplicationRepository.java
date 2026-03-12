package com.raster.hrm.leaveapplication.repository;

import com.raster.hrm.leaveapplication.entity.LeaveApplication;
import com.raster.hrm.leaveapplication.entity.LeaveApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    Page<LeaveApplication> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<LeaveApplication> findByStatus(LeaveApplicationStatus status, Pageable pageable);

    Page<LeaveApplication> findByLeaveTypeId(Long leaveTypeId, Pageable pageable);

    Page<LeaveApplication> findByFromDateGreaterThanEqualAndToDateLessThanEqual(
            LocalDate fromDate, LocalDate toDate, Pageable pageable);

    Page<LeaveApplication> findByEmployeeIdAndFromDateGreaterThanEqualAndToDateLessThanEqual(
            Long employeeId, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    List<LeaveApplication> findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            LeaveApplicationStatus status, LocalDate end, LocalDate start);

    List<LeaveApplication> findByEmployeeIdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long employeeId, LeaveApplicationStatus status, LocalDate end, LocalDate start);

    List<LeaveApplication> findByEmployee_Department_IdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long departmentId, LeaveApplicationStatus status, LocalDate end, LocalDate start);
}
