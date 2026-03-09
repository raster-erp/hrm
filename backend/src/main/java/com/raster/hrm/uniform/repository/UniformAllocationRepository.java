package com.raster.hrm.uniform.repository;

import com.raster.hrm.uniform.entity.AllocationStatus;
import com.raster.hrm.uniform.entity.UniformAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UniformAllocationRepository extends JpaRepository<UniformAllocation, Long> {

    List<UniformAllocation> findByEmployeeId(Long employeeId);

    List<UniformAllocation> findByUniformId(Long uniformId);

    List<UniformAllocation> findByStatus(AllocationStatus status);

    Page<UniformAllocation> findAll(Pageable pageable);
}
