package com.raster.hrm.separation.repository;

import com.raster.hrm.separation.entity.ExitChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExitChecklistRepository extends JpaRepository<ExitChecklist, Long> {

    List<ExitChecklist> findBySeparationId(Long separationId);

    long countBySeparationIdAndCleared(Long separationId, boolean cleared);
}
