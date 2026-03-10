package com.raster.hrm.rotationpattern.repository;

import com.raster.hrm.rotationpattern.entity.RotationPattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RotationPatternRepository extends JpaRepository<RotationPattern, Long> {

    boolean existsByName(String name);

    Page<RotationPattern> findAll(Pageable pageable);
}
