package com.raster.hrm.leaveapplication.repository;

import com.raster.hrm.leaveapplication.entity.LeaveApprovalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveApprovalLogRepository extends JpaRepository<LeaveApprovalLog, Long> {

    List<LeaveApprovalLog> findByLeaveApplicationIdOrderByCreatedAtAsc(Long leaveApplicationId);
}
