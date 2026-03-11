package com.raster.hrm.attendancereport.repository;

import com.raster.hrm.attendancereport.entity.ReportSchedule;
import com.raster.hrm.attendancereport.entity.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {

    Page<ReportSchedule> findByReportType(ReportType type, Pageable pageable);

    List<ReportSchedule> findByActiveTrue();

    List<ReportSchedule> findByActiveTrueAndNextRunAtBefore(LocalDateTime now);
}
