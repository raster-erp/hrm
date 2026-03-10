package com.raster.hrm.device;

import com.raster.hrm.device.dto.DeviceRequest;
import com.raster.hrm.device.entity.Device;
import com.raster.hrm.device.entity.DeviceStatus;
import com.raster.hrm.device.entity.DeviceType;
import com.raster.hrm.device.repository.DeviceRepository;
import com.raster.hrm.device.service.DeviceService;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    private Device createDevice(Long id, String serialNumber, String name, DeviceType type) {
        var device = new Device();
        device.setId(id);
        device.setSerialNumber(serialNumber);
        device.setName(name);
        device.setType(type);
        device.setLocation("Main Entrance");
        device.setIpAddress("192.168.1.100");
        device.setStatus(DeviceStatus.ACTIVE);
        device.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        device.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return device;
    }

    private DeviceRequest createRequest() {
        return new DeviceRequest(
                "BIO-001",
                "Main Entrance Biometric",
                "BIOMETRIC",
                "Main Entrance",
                "192.168.1.100"
        );
    }

    @Test
    void getAll_shouldReturnPageOfDevices() {
        var devices = List.of(
                createDevice(1L, "BIO-001", "Main Entrance", DeviceType.BIOMETRIC),
                createDevice(2L, "RFID-001", "Side Gate", DeviceType.RFID)
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(devices, pageable, 2);
        when(deviceRepository.findAll(pageable)).thenReturn(page);

        var result = deviceService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("BIO-001", result.getContent().get(0).serialNumber());
        assertEquals("RFID-001", result.getContent().get(1).serialNumber());
        verify(deviceRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<Device>(List.of(), pageable, 0);
        when(deviceRepository.findAll(pageable)).thenReturn(page);

        var result = deviceService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnDevice() {
        var device = createDevice(1L, "BIO-001", "Main Entrance", DeviceType.BIOMETRIC);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        var result = deviceService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("BIO-001", result.serialNumber());
        assertEquals("Main Entrance", result.name());
        assertEquals("BIOMETRIC", result.type());
        assertEquals("ACTIVE", result.status());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> deviceService.getById(999L));
    }

    @Test
    void getBySerialNumber_shouldReturnDevice() {
        var device = createDevice(1L, "BIO-001", "Main Entrance", DeviceType.BIOMETRIC);
        when(deviceRepository.findBySerialNumber("BIO-001")).thenReturn(Optional.of(device));

        var result = deviceService.getBySerialNumber("BIO-001");

        assertNotNull(result);
        assertEquals("BIO-001", result.serialNumber());
    }

    @Test
    void getBySerialNumber_shouldThrowWhenNotFound() {
        when(deviceRepository.findBySerialNumber("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> deviceService.getBySerialNumber("UNKNOWN"));
    }

    @Test
    void getByStatus_shouldReturnDevices() {
        var devices = List.of(
                createDevice(1L, "BIO-001", "Main Entrance", DeviceType.BIOMETRIC)
        );
        when(deviceRepository.findByStatus(DeviceStatus.ACTIVE)).thenReturn(devices);

        var result = deviceService.getByStatus(DeviceStatus.ACTIVE);

        assertEquals(1, result.size());
        assertEquals("BIO-001", result.get(0).serialNumber());
    }

    @Test
    void getByType_shouldReturnDevices() {
        var devices = List.of(
                createDevice(1L, "RFID-001", "Side Gate", DeviceType.RFID)
        );
        when(deviceRepository.findByType(DeviceType.RFID)).thenReturn(devices);

        var result = deviceService.getByType(DeviceType.RFID);

        assertEquals(1, result.size());
        assertEquals("RFID-001", result.get(0).serialNumber());
        assertEquals("RFID", result.get(0).type());
    }

    @Test
    void create_shouldCreateAndReturnDevice() {
        var request = createRequest();
        when(deviceRepository.existsBySerialNumber("BIO-001")).thenReturn(false);
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
            Device d = invocation.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(LocalDateTime.now());
            d.setUpdatedAt(LocalDateTime.now());
            return d;
        });

        var result = deviceService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("BIO-001", result.serialNumber());
        assertEquals("Main Entrance Biometric", result.name());
        assertEquals("BIOMETRIC", result.type());
        assertEquals("Main Entrance", result.location());
        assertEquals("192.168.1.100", result.ipAddress());
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void create_shouldThrowWhenSerialNumberExists() {
        var request = createRequest();
        when(deviceRepository.existsBySerialNumber("BIO-001")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> deviceService.create(request));
        assertTrue(ex.getMessage().contains("BIO-001"));
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateAndReturnDevice() {
        var device = createDevice(1L, "BIO-001", "Main Entrance", DeviceType.BIOMETRIC);
        var request = new DeviceRequest("BIO-001", "Updated Name", "RFID", "New Location", "10.0.0.1");
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = deviceService.update(1L, request);

        assertNotNull(result);
        assertEquals("Updated Name", result.name());
        assertEquals("RFID", result.type());
        assertEquals("New Location", result.location());
        assertEquals("10.0.0.1", result.ipAddress());
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void update_shouldThrowWhenDeviceNotFound() {
        var request = createRequest();
        when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> deviceService.update(999L, request));
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNewSerialNumberExists() {
        var device = createDevice(1L, "BIO-001", "Main Entrance", DeviceType.BIOMETRIC);
        var request = new DeviceRequest("BIO-002", "Updated Name", "BIOMETRIC", "Location", "10.0.0.1");
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.existsBySerialNumber("BIO-002")).thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> deviceService.update(1L, request));
        assertTrue(ex.getMessage().contains("BIO-002"));
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameSerialNumber() {
        var device = createDevice(1L, "BIO-001", "Main Entrance", DeviceType.BIOMETRIC);
        var request = new DeviceRequest("BIO-001", "Updated Name", "BIOMETRIC", "New Location", "10.0.0.1");
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = deviceService.update(1L, request);

        assertEquals("Updated Name", result.name());
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void updateStatus_shouldUpdateStatus() {
        var device = createDevice(1L, "BIO-001", "Main Entrance", DeviceType.BIOMETRIC);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = deviceService.updateStatus(1L, DeviceStatus.INACTIVE);

        assertEquals("INACTIVE", result.status());
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void updateStatus_shouldThrowWhenNotFound() {
        when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> deviceService.updateStatus(999L, DeviceStatus.INACTIVE));
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void updateStatus_shouldSetOfflineStatus() {
        var device = createDevice(1L, "BIO-001", "Main Entrance", DeviceType.BIOMETRIC);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = deviceService.updateStatus(1L, DeviceStatus.OFFLINE);

        assertEquals("OFFLINE", result.status());
    }

    @Test
    void recordSync_shouldUpdateLastSyncAt() {
        var device = createDevice(1L, "BIO-001", "Main Entrance", DeviceType.BIOMETRIC);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = deviceService.recordSync(1L);

        assertNotNull(result.lastSyncAt());
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void recordSync_shouldThrowWhenNotFound() {
        when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> deviceService.recordSync(999L));
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteDevice() {
        var device = createDevice(1L, "BIO-001", "Main Entrance", DeviceType.BIOMETRIC);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        deviceService.delete(1L);

        verify(deviceRepository).delete(device);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> deviceService.delete(999L));
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void getById_shouldMapResponseFieldsCorrectly() {
        var device = createDevice(1L, "BIO-001", "Main Entrance", DeviceType.BIOMETRIC);
        device.setLastSyncAt(LocalDateTime.of(2024, 6, 1, 12, 0));
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        var result = deviceService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("BIO-001", result.serialNumber());
        assertEquals("Main Entrance", result.name());
        assertEquals("BIOMETRIC", result.type());
        assertEquals("Main Entrance", result.location());
        assertEquals("192.168.1.100", result.ipAddress());
        assertEquals("ACTIVE", result.status());
        assertEquals(LocalDateTime.of(2024, 6, 1, 12, 0), result.lastSyncAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.createdAt());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), result.updatedAt());
    }

    @Test
    void getByStatus_shouldReturnEmptyListWhenNoDevices() {
        when(deviceRepository.findByStatus(DeviceStatus.OFFLINE)).thenReturn(List.of());

        var result = deviceService.getByStatus(DeviceStatus.OFFLINE);

        assertEquals(0, result.size());
    }

    @Test
    void getByType_shouldReturnEmptyListWhenNoDevices() {
        when(deviceRepository.findByType(DeviceType.BIOMETRIC)).thenReturn(List.of());

        var result = deviceService.getByType(DeviceType.BIOMETRIC);

        assertEquals(0, result.size());
    }
}
