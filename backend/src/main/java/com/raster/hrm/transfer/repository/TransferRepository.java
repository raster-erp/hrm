package com.raster.hrm.transfer.repository;

import com.raster.hrm.transfer.entity.Transfer;
import com.raster.hrm.transfer.entity.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByEmployeeId(Long employeeId);

    List<Transfer> findByStatus(TransferStatus status);

    Page<Transfer> findByStatus(TransferStatus status, Pageable pageable);
}
