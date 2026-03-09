package com.raster.hrm.uniform.repository;

import com.raster.hrm.uniform.entity.Uniform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UniformRepository extends JpaRepository<Uniform, Long> {

    List<Uniform> findByActive(boolean active);

    List<Uniform> findByType(String type);

    Page<Uniform> findAll(Pageable pageable);
}
