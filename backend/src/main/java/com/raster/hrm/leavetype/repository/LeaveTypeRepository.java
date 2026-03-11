package com.raster.hrm.leavetype.repository;

import com.raster.hrm.leavetype.entity.LeaveType;
import com.raster.hrm.leavetype.entity.LeaveTypeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    List<LeaveType> findByCategory(LeaveTypeCategory category);

    List<LeaveType> findByActive(boolean active);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    Page<LeaveType> findAll(Pageable pageable);
}
