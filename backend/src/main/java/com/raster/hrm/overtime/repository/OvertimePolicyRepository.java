package com.raster.hrm.overtime.repository;

import com.raster.hrm.overtime.entity.OvertimePolicy;
import com.raster.hrm.overtime.entity.OvertimePolicyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OvertimePolicyRepository extends JpaRepository<OvertimePolicy, Long> {

    List<OvertimePolicy> findByType(OvertimePolicyType type);

    List<OvertimePolicy> findByActive(boolean active);

    boolean existsByName(String name);

    Page<OvertimePolicy> findAll(Pageable pageable);
}
