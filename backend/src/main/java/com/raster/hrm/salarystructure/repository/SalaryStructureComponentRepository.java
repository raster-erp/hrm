package com.raster.hrm.salarystructure.repository;

import com.raster.hrm.salarystructure.entity.SalaryStructureComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryStructureComponentRepository extends JpaRepository<SalaryStructureComponent, Long> {

    List<SalaryStructureComponent> findBySalaryStructureIdOrderBySortOrder(Long salaryStructureId);

    void deleteBySalaryStructureId(Long salaryStructureId);
}
