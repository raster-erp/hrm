package com.raster.hrm.employee.repository;

import com.raster.hrm.employee.entity.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {

    List<EmployeeDocument> findByEmployeeId(Long employeeId);
}
