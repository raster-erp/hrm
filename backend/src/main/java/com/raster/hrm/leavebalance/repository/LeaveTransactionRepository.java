package com.raster.hrm.leavebalance.repository;

import com.raster.hrm.leavebalance.entity.LeaveTransaction;
import com.raster.hrm.leavebalance.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveTransactionRepository extends JpaRepository<LeaveTransaction, Long> {

    Page<LeaveTransaction> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<LeaveTransaction> findByEmployeeIdAndLeaveTypeId(Long employeeId, Long leaveTypeId, Pageable pageable);

    Page<LeaveTransaction> findByEmployeeIdAndTransactionType(Long employeeId, TransactionType transactionType,
                                                              Pageable pageable);

    Page<LeaveTransaction> findByEmployeeIdAndLeaveTypeIdAndTransactionType(Long employeeId, Long leaveTypeId,
                                                                            TransactionType transactionType,
                                                                            Pageable pageable);
}
