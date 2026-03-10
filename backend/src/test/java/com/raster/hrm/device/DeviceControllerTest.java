package com.raster.hrm.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.device.controller.DeviceController;
import com.raster.hrm.device.dto.DeviceRequest;
import com.raster.hrm.device.dto.DeviceResponse;
import com.raster.hrm.device.entity.DeviceStatus;
import com.raster.hrm.device.entity.DeviceType;
import com.raster.hrm.device.service.DeviceService;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/devices";

    private DeviceResponse createResponse(Long id, String serialNumber, String name, String type, String status) {
        return new DeviceResponse(
                id, serialNumber, name, type,
                "Main Entrance", "192.168.1.100", status,
                LocalDateTime.of(2024, 6, 1, 12, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0)
        );
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
    void getAll_shouldReturnPageOfDevices() throws Exception {
        var devices = List.of(
                createResponse(1L, "BIO-001", "Main Entrance", "BIOMETRIC", "ACTIVE"),
                createResponse(2L, "RFID-001", "Side Gate", "RFID", "ACTIVE")
        );
        var page = new PageImpl<>(devices, PageRequest.of(0, 20), 2);
        when(deviceService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].serialNumber").value("BIO-001"))
                .andExpect(jsonPath("$.content[1].serialNumber").value("RFID-001"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnDevice() throws Exception {
        var response = createResponse(1L, "BIO-001", "Main Entrance", "BIOMETRIC", "ACTIVE");
        when(deviceService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.serialNumber").value("BIO-001"))
                .andExpect(jsonPath("$.name").value("Main Entrance"))
                .andExpect(jsonPath("$.type").value("BIOMETRIC"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(deviceService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Device", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBySerialNumber_shouldReturnDevice() throws Exception {
        var response = createResponse(1L, "BIO-001", "Main Entrance", "BIOMETRIC", "ACTIVE");
        when(deviceService.getBySerialNumber("BIO-001")).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/serial/BIO-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber").value("BIO-001"));
    }

    @Test
    void getBySerialNumber_shouldReturn404WhenNotFound() throws Exception {
        when(deviceService.getBySerialNumber("UNKNOWN"))
                .thenThrow(new ResourceNotFoundException("Device", "serialNumber", "UNKNOWN"));

        mockMvc.perform(get(BASE_URL + "/serial/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByStatus_shouldReturnDevices() throws Exception {
        var devices = List.of(
                createResponse(1L, "BIO-001", "Main Entrance", "BIOMETRIC", "ACTIVE")
        );
        when(deviceService.getByStatus(DeviceStatus.ACTIVE)).thenReturn(devices);

        mockMvc.perform(get(BASE_URL + "/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].serialNumber").value("BIO-001"));
    }

    @Test
    void getByType_shouldReturnDevices() throws Exception {
        var devices = List.of(
                createResponse(1L, "RFID-001", "Side Gate", "RFID", "ACTIVE")
        );
        when(deviceService.getByType(DeviceType.RFID)).thenReturn(devices);

        mockMvc.perform(get(BASE_URL + "/type/RFID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("RFID"));
    }

    @Test
    void create_shouldReturn201WithCreatedDevice() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "BIO-001", "Main Entrance Biometric", "BIOMETRIC", "ACTIVE");
        when(deviceService.create(any(DeviceRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.serialNumber").value("BIO-001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void create_shouldReturn400WhenSerialNumberExists() throws Exception {
        var request = createRequest();
        when(deviceService.create(any(DeviceRequest.class)))
                .thenThrow(new BadRequestException("Device with serial number 'BIO-001' already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new DeviceRequest("", "", null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedDevice() throws Exception {
        var request = createRequest();
        var response = createResponse(1L, "BIO-001", "Main Entrance Biometric", "BIOMETRIC", "ACTIVE");
        when(deviceService.update(eq(1L), any(DeviceRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.serialNumber").value("BIO-001"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(deviceService.update(eq(999L), any(DeviceRequest.class)))
                .thenThrow(new ResourceNotFoundException("Device", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_shouldReturnUpdatedDevice() throws Exception {
        var response = createResponse(1L, "BIO-001", "Main Entrance", "BIOMETRIC", "INACTIVE");
        when(deviceService.updateStatus(eq(1L), eq(DeviceStatus.INACTIVE))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "INACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void updateStatus_shouldReturn404WhenNotFound() throws Exception {
        when(deviceService.updateStatus(eq(999L), eq(DeviceStatus.INACTIVE)))
                .thenThrow(new ResourceNotFoundException("Device", "id", 999L));

        mockMvc.perform(patch(BASE_URL + "/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "INACTIVE"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void recordSync_shouldReturnUpdatedDevice() throws Exception {
        var response = createResponse(1L, "BIO-001", "Main Entrance", "BIOMETRIC", "ACTIVE");
        when(deviceService.recordSync(1L)).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/1/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber").value("BIO-001"));
    }

    @Test
    void recordSync_shouldReturn404WhenNotFound() throws Exception {
        when(deviceService.recordSync(999L))
                .thenThrow(new ResourceNotFoundException("Device", "id", 999L));

        mockMvc.perform(post(BASE_URL + "/999/sync"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(deviceService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(deviceService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Device", "id", 999L))
                .when(deviceService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
