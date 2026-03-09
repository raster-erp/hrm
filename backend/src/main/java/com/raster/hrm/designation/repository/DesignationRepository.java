package com.raster.hrm.designation.repository;

import com.raster.hrm.designation.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

    Optional<Designation> findByCode(String code);

    List<Designation> findByDepartmentId(Long departmentId);

    List<Designation> findByActiveTrue();

    boolean existsByCode(String code);

    @Query(value = "SELECT COUNT(*) FROM employees WHERE designation_id = :designationId", nativeQuery = true)
    long countEmployeesByDesignationId(@Param("designationId") Long designationId);
}
