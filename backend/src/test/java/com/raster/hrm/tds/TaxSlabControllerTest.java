package com.raster.hrm.tds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.tds.controller.TaxSlabController;
import com.raster.hrm.tds.dto.TaxSlabRequest;
import com.raster.hrm.tds.dto.TaxSlabResponse;
import com.raster.hrm.tds.service.TaxSlabService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaxSlabController.class)
class TaxSlabControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaxSlabService taxSlabService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/tax-slabs";

    private TaxSlabResponse createResponse() {
        return new TaxSlabResponse(1L, "NEW", "2025-26",
                new BigDecimal("300000"), new BigDecimal("700000"),
                new BigDecimal("5.00"), "5% slab", true,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0));
    }

    private TaxSlabRequest createRequest() {
        return new TaxSlabRequest("NEW", "2025-26",
                new BigDecimal("300000"), new BigDecimal("700000"),
                new BigDecimal("5.00"), "5% slab");
    }

    @Test
    void getAll_shouldReturnPageOfSlabs() throws Exception {
        var slabs = List.of(createResponse(),
                new TaxSlabResponse(2L, "NEW", "2025-26",
                        new BigDecimal("700000"), new BigDecimal("1000000"),
                        new BigDecimal("10.00"), "10% slab", true,
                        LocalDateTime.of(2024, 1, 15, 10, 0),
                        LocalDateTime.of(2024, 1, 15, 10, 0)));
        var page = new PageImpl<>(slabs, PageRequest.of(0, 20), 2);
        when(taxSlabService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].regime").value("NEW"))
                .andExpect(jsonPath("$.content[1].rate").value(10.00))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_shouldReturnSlab() throws Exception {
        var response = createResponse();
        when(taxSlabService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.regime").value("NEW"))
                .andExpect(jsonPath("$.financialYear").value("2025-26"))
                .andExpect(jsonPath("$.rate").value(5.00))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(taxSlabService.getById(999L))
                .thenThrow(new ResourceNotFoundException("TaxSlab", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByRegimeAndYear_shouldReturnSlabs() throws Exception {
        var slabs = List.of(createResponse());
        when(taxSlabService.getByRegimeAndYear("NEW", "2025-26")).thenReturn(slabs);

        mockMvc.perform(get(BASE_URL + "/regime/NEW/year/2025-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].regime").value("NEW"));
    }

    @Test
    void create_shouldReturn201WithCreatedSlab() throws Exception {
        var request = createRequest();
        var response = createResponse();
        when(taxSlabService.create(any(TaxSlabRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.regime").value("NEW"))
                .andExpect(jsonPath("$.financialYear").value("2025-26"));
    }

    @Test
    void create_shouldReturn400WhenDuplicate() throws Exception {
        var request = createRequest();
        when(taxSlabService.create(any(TaxSlabRequest.class)))
                .thenThrow(new BadRequestException("Tax slab already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenValidationFails() throws Exception {
        var request = new TaxSlabRequest(null, "", null, null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdatedSlab() throws Exception {
        var request = createRequest();
        var response = createResponse();
        when(taxSlabService.update(eq(1L), any(TaxSlabRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.regime").value("NEW"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var request = createRequest();
        when(taxSlabService.update(eq(999L), any(TaxSlabRequest.class)))
                .thenThrow(new ResourceNotFoundException("TaxSlab", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(taxSlabService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(taxSlabService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("TaxSlab", "id", 999L))
                .when(taxSlabService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}
