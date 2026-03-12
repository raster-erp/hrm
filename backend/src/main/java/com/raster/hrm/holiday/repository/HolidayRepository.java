package com.raster.hrm.holiday.repository;

import com.raster.hrm.holiday.entity.Holiday;
import com.raster.hrm.holiday.entity.HolidayType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    Page<Holiday> findByActive(boolean active, Pageable pageable);

    Page<Holiday> findByType(HolidayType type, Pageable pageable);

    Page<Holiday> findByRegion(String region, Pageable pageable);

    List<Holiday> findByDateBetween(LocalDate start, LocalDate end);

    List<Holiday> findByDateBetweenAndActiveTrue(LocalDate start, LocalDate end);

    List<Holiday> findByRegionAndDateBetweenAndActiveTrue(String region, LocalDate start, LocalDate end);
}
