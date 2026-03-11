package com.raster.hrm.leavepolicyassignment.repository;

import com.raster.hrm.leavepolicyassignment.entity.AssignmentType;
import com.raster.hrm.leavepolicyassignment.entity.LeavePolicyAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeavePolicyAssignmentRepository extends JpaRepository<LeavePolicyAssignment, Long> {

    List<LeavePolicyAssignment> findByLeavePolicyId(Long leavePolicyId);

    List<LeavePolicyAssignment> findByAssignmentType(AssignmentType assignmentType);

    List<LeavePolicyAssignment> findByDepartmentId(Long departmentId);

    List<LeavePolicyAssignment> findByDesignationId(Long designationId);

    List<LeavePolicyAssignment> findByEmployeeId(Long employeeId);

    List<LeavePolicyAssignment> findByActive(boolean active);

    Page<LeavePolicyAssignment> findAll(Pageable pageable);
}
