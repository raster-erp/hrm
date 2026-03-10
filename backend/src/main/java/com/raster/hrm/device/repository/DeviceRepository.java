package com.raster.hrm.device.repository;

import com.raster.hrm.device.entity.Device;
import com.raster.hrm.device.entity.DeviceStatus;
import com.raster.hrm.device.entity.DeviceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findBySerialNumber(String serialNumber);

    List<Device> findByStatus(DeviceStatus status);

    List<Device> findByType(DeviceType type);

    boolean existsBySerialNumber(String serialNumber);

    Page<Device> findAll(Pageable pageable);
}
