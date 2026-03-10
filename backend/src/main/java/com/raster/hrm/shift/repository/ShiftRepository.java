package com.raster.hrm.shift.repository;

import com.raster.hrm.shift.entity.Shift;
import com.raster.hrm.shift.entity.ShiftType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByType(ShiftType type);

    List<Shift> findByActive(boolean active);

    boolean existsByName(String name);

    Page<Shift> findAll(Pageable pageable);
}
