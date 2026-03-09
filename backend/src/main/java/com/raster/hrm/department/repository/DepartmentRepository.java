package com.raster.hrm.department.repository;

import com.raster.hrm.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCode(String code);

    List<Department> findByParentId(Long parentId);

    List<Department> findByParentIsNull();

    List<Department> findByActiveTrue();

    boolean existsByCode(String code);

    @Query(value = "SELECT COUNT(*) FROM employees WHERE department_id = :departmentId", nativeQuery = true)
    long countEmployeesByDepartmentId(@Param("departmentId") Long departmentId);
}
