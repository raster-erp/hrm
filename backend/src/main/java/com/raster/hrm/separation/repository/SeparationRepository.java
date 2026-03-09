package com.raster.hrm.separation.repository;

import com.raster.hrm.separation.entity.Separation;
import com.raster.hrm.separation.entity.SeparationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeparationRepository extends JpaRepository<Separation, Long> {

    List<Separation> findByEmployeeId(Long employeeId);

    List<Separation> findByStatus(SeparationStatus status);
}
