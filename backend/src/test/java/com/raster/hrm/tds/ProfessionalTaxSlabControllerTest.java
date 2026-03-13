package com.raster.hrm.tds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.tds.controller.ProfessionalTaxSlabController;
import com.raster.hrm.tds.dto.ProfessionalTaxSlabRequest;
import com.raster.hrm.tds.dto.ProfessionalTaxSlabResponse;
import com.raster.hrm.tds.service.ProfessionalTaxSlabService;
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

@WebMvcTest(ProfessionalTaxSlabController.class)
class ProfessionalTaxSlabControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfessionalTaxSlabService professionalTaxSlabService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/professional-tax-slabs";

    private ProfessionalTaxSlabResponse createResponse() {
        return new ProfessionalTaxSlabResponse(
                1L, "Karnataka",
                new BigDecimal("15000"), new BigDecimal("25000"),
                new BigDecimal("200"), new BigDecimal("300"),
                true,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0));
    }

    private ProfessionalTaxSlabRequest createRequest() {
        return new ProfessionalTaxSlabRequest(
                "Karnataka",
                new BigDecimal("15000"), new BigDecimal("25000"),
                new BigDecimal("200"), new BigDecimal("300"));
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        var slabs = List.of(createResponse());
        var page = new PageImpl<>(slabs, PageRequest.of(0, 20), 1);
        when(professionalTaxSlabService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].state").value("Karnataka"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getById_shouldReturn() throws Exception {
        var response = createResponse();
        when(professionalTaxSlabService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.state").value("Karnataka"))
                .andExpect(jsonPath("$.monthlyTax").value(200))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getById_shouldReturn404() throws Exception {
        when(professionalTaxSlabService.getById(999L))
                .thenThrow(new ResourceNotFoundException("ProfessionalTaxSlab", "id", 999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByState_shouldReturn() throws Exception {
        var slabs = List.of(createResponse());
        when(professionalTaxSlabService.getByState("Karnataka")).thenReturn(slabs);

        mockMvc.perform(get(BASE_URL + "/state/Karnataka"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].state").value("Karnataka"));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var request = createRequest();
        var response = createResponse();
        when(professionalTaxSlabService.create(any(ProfessionalTaxSlabRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.state").value("Karnataka"));
    }

    @Test
    void create_shouldReturn400WhenDuplicate() throws Exception {
        var request = createRequest();
        when(professionalTaxSlabService.create(any(ProfessionalTaxSlabRequest.class)))
                .thenThrow(new BadRequestException("Professional tax slab already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var request = createRequest();
        var response = createResponse();
        when(professionalTaxSlabService.update(eq(1L), any(ProfessionalTaxSlabRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.state").value("Karnataka"));
    }

    @Test
    void update_shouldReturn404() throws Exception {
        var request = createRequest();
        when(professionalTaxSlabService.update(eq(999L), any(ProfessionalTaxSlabRequest.class)))
                .thenThrow(new ResourceNotFoundException("ProfessionalTaxSlab", "id", 999L));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(professionalTaxSlabService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(professionalTaxSlabService).delete(1L);
    }

    @Test
    void delete_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("ProfessionalTaxSlab", "id", 999L))
                .when(professionalTaxSlabService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void computePT_shouldReturnResult() throws Exception {
        when(professionalTaxSlabService.computeProfessionalTax(100L, 4)).thenReturn(new BigDecimal("200"));

        mockMvc.perform(get(BASE_URL + "/compute/employee/100/month/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionalTax").value(200));
    }
}
