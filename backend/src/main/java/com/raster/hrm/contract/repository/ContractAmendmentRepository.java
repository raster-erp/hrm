package com.raster.hrm.contract.repository;

import com.raster.hrm.contract.entity.ContractAmendment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractAmendmentRepository extends JpaRepository<ContractAmendment, Long> {

    List<ContractAmendment> findByContractId(Long contractId);
}
