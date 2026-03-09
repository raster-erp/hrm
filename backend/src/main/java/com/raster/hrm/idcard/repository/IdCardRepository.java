package com.raster.hrm.idcard.repository;

import com.raster.hrm.idcard.entity.IdCard;
import com.raster.hrm.idcard.entity.IdCardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IdCardRepository extends JpaRepository<IdCard, Long> {

    List<IdCard> findByEmployeeId(Long employeeId);

    List<IdCard> findByStatus(IdCardStatus status);

    List<IdCard> findByExpiryDateBefore(LocalDate date);

    boolean existsByCardNumber(String cardNumber);

    Page<IdCard> findAll(Pageable pageable);
}
