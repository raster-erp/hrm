package com.raster.hrm.separation.repository;

import com.raster.hrm.separation.entity.NoDues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoDuesRepository extends JpaRepository<NoDues, Long> {

    List<NoDues> findBySeparationId(Long separationId);

    long countBySeparationIdAndCleared(Long separationId, boolean cleared);
}
