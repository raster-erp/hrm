package com.raster.hrm.credential.repository;

import com.raster.hrm.credential.entity.Credential;
import com.raster.hrm.credential.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    List<Credential> findByEmployeeId(Long employeeId);

    List<Credential> findByVerificationStatus(VerificationStatus verificationStatus);

    List<Credential> findByExpiryDateBetween(LocalDate startDate, LocalDate endDate);

    List<Credential> findByExpiryDateBefore(LocalDate date);

    Page<Credential> findAll(Pageable pageable);
}
