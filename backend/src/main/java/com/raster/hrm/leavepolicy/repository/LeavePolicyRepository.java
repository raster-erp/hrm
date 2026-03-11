package com.raster.hrm.leavepolicy.repository;

import com.raster.hrm.leavepolicy.entity.LeavePolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {

    List<LeavePolicy> findByLeaveTypeId(Long leaveTypeId);

    List<LeavePolicy> findByActive(boolean active);

    boolean existsByName(String name);

    Page<LeavePolicy> findAll(Pageable pageable);
}
