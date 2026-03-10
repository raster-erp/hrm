package com.raster.hrm.attendance.repository;

import com.raster.hrm.attendance.entity.AttendancePunch;
import com.raster.hrm.attendance.entity.PunchDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendancePunchRepository extends JpaRepository<AttendancePunch, Long> {

    Page<AttendancePunch> findByEmployeeId(Long employeeId, Pageable pageable);

    List<AttendancePunch> findByDeviceId(Long deviceId);

    Page<AttendancePunch> findByPunchTimeBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AttendancePunch> findByEmployeeIdAndPunchTimeBetween(Long employeeId,
                                                              LocalDateTime from,
                                                              LocalDateTime to,
                                                              Pageable pageable);

    @Query("SELECT COUNT(p) > 0 FROM AttendancePunch p WHERE p.employee.id = :employeeId " +
            "AND p.device.id = :deviceId AND p.punchTime = :punchTime AND p.direction = :direction")
    boolean existsDuplicate(@Param("employeeId") Long employeeId,
                            @Param("deviceId") Long deviceId,
                            @Param("punchTime") LocalDateTime punchTime,
                            @Param("direction") PunchDirection direction);

    Page<AttendancePunch> findAll(Pageable pageable);
}
