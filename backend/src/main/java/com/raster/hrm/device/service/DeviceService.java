package com.raster.hrm.device.service;

import com.raster.hrm.device.dto.DeviceRequest;
import com.raster.hrm.device.dto.DeviceResponse;
import com.raster.hrm.device.entity.Device;
import com.raster.hrm.device.entity.DeviceStatus;
import com.raster.hrm.device.entity.DeviceType;
import com.raster.hrm.device.repository.DeviceRepository;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> getAll(Pageable pageable) {
        return deviceRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public DeviceResponse getById(Long id) {
        var device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
        return mapToResponse(device);
    }

    @Transactional(readOnly = true)
    public DeviceResponse getBySerialNumber(String serialNumber) {
        var device = deviceRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "serialNumber", serialNumber));
        return mapToResponse(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getByStatus(DeviceStatus status) {
        return deviceRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getByType(DeviceType type) {
        return deviceRepository.findByType(type).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DeviceResponse create(DeviceRequest request) {
        if (deviceRepository.existsBySerialNumber(request.serialNumber())) {
            throw new BadRequestException("Device with serial number '" + request.serialNumber() + "' already exists");
        }

        var device = new Device();
        device.setSerialNumber(request.serialNumber());
        device.setName(request.name());
        device.setType(DeviceType.valueOf(request.type()));
        device.setLocation(request.location());
        device.setIpAddress(request.ipAddress());

        var saved = deviceRepository.save(device);
        log.info("Created device with id: {} serial: {}", saved.getId(), saved.getSerialNumber());
        return mapToResponse(saved);
    }

    public DeviceResponse update(Long id, DeviceRequest request) {
        var device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));

        if (!device.getSerialNumber().equals(request.serialNumber())
                && deviceRepository.existsBySerialNumber(request.serialNumber())) {
            throw new BadRequestException("Device with serial number '" + request.serialNumber() + "' already exists");
        }

        device.setSerialNumber(request.serialNumber());
        device.setName(request.name());
        device.setType(DeviceType.valueOf(request.type()));
        device.setLocation(request.location());
        device.setIpAddress(request.ipAddress());

        var saved = deviceRepository.save(device);
        log.info("Updated device with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public DeviceResponse updateStatus(Long id, DeviceStatus status) {
        var device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));

        device.setStatus(status);
        var saved = deviceRepository.save(device);
        log.info("Updated status of device id: {} to {}", id, status);
        return mapToResponse(saved);
    }

    public DeviceResponse recordSync(Long id) {
        var device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));

        device.setLastSyncAt(LocalDateTime.now());
        var saved = deviceRepository.save(device);
        log.info("Recorded sync for device id: {}", id);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        var device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
        deviceRepository.delete(device);
        log.info("Deleted device with id: {}", id);
    }

    private DeviceResponse mapToResponse(Device device) {
        return new DeviceResponse(
                device.getId(),
                device.getSerialNumber(),
                device.getName(),
                device.getType().name(),
                device.getLocation(),
                device.getIpAddress(),
                device.getStatus().name(),
                device.getLastSyncAt(),
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }
}
