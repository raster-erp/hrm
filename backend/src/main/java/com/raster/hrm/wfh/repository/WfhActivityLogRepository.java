package com.raster.hrm.wfh.repository;

import com.raster.hrm.wfh.entity.WfhActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WfhActivityLogRepository extends JpaRepository<WfhActivityLog, Long> {

    List<WfhActivityLog> findByWfhRequestId(Long wfhRequestId);

    @Query("SELECT a FROM WfhActivityLog a WHERE a.wfhRequest.id = :requestId AND a.checkOutTime IS NULL")
    Optional<WfhActivityLog> findActiveByRequestId(@Param("requestId") Long requestId);

    @Query("SELECT a FROM WfhActivityLog a WHERE a.wfhRequest.employee.id = :employeeId AND CAST(a.checkInTime AS LocalDate) = :date")
    List<WfhActivityLog> findByEmployeeIdAndDate(@Param("employeeId") Long employeeId,
                                                  @Param("date") LocalDate date);
}
