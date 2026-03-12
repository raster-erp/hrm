package com.raster.hrm.leaveencashment.repository;

import com.raster.hrm.leaveencashment.entity.EncashmentStatus;
import com.raster.hrm.leaveencashment.entity.LeaveEncashment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveEncashmentRepository extends JpaRepository<LeaveEncashment, Long> {

    Page<LeaveEncashment> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<LeaveEncashment> findByStatus(EncashmentStatus status, Pageable pageable);

    Page<LeaveEncashment> findByEmployeeIdAndStatus(Long employeeId, EncashmentStatus status, Pageable pageable);

    List<LeaveEncashment> findByEmployeeIdAndLeaveTypeIdAndYear(Long employeeId, Long leaveTypeId, int year);

    Optional<LeaveEncashment> findByEmployeeIdAndLeaveTypeIdAndYearAndStatusIn(
            Long employeeId, Long leaveTypeId, int year, List<EncashmentStatus> statuses);
}
