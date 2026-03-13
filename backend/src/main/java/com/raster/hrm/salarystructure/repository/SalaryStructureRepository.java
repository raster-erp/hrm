package com.raster.hrm.salarystructure.repository;

import com.raster.hrm.salarystructure.entity.SalaryStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {

    List<SalaryStructure> findByActive(boolean active);

    boolean existsByCode(String code);

    Page<SalaryStructure> findAll(Pageable pageable);
}
