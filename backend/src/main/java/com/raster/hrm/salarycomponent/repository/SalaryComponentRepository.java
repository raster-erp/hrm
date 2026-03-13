package com.raster.hrm.salarycomponent.repository;

import com.raster.hrm.salarycomponent.entity.SalaryComponent;
import com.raster.hrm.salarycomponent.entity.SalaryComponentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, Long> {

    List<SalaryComponent> findByType(SalaryComponentType type);

    List<SalaryComponent> findByActive(boolean active);

    boolean existsByCode(String code);

    Page<SalaryComponent> findAll(Pageable pageable);
}
