package com.raster.hrm.tds;

import com.raster.hrm.exception.BadRequestException;
import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.tds.dto.TaxSlabRequest;
import com.raster.hrm.tds.entity.TaxRegime;
import com.raster.hrm.tds.entity.TaxSlab;
import com.raster.hrm.tds.repository.TaxSlabRepository;
import com.raster.hrm.tds.service.TaxSlabService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
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
class TaxSlabServiceTest {

    @Mock
    private TaxSlabRepository taxSlabRepository;

    @InjectMocks
    private TaxSlabService taxSlabService;

    private TaxSlab createSlab(Long id, TaxRegime regime, String fy, BigDecimal from, BigDecimal to, BigDecimal rate) {
        var slab = new TaxSlab();
        slab.setId(id);
        slab.setRegime(regime);
        slab.setFinancialYear(fy);
        slab.setSlabFrom(from);
        slab.setSlabTo(to);
        slab.setRate(rate);
        slab.setDescription("Test slab");
        slab.setActive(true);
        slab.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        slab.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        return slab;
    }

    private TaxSlabRequest createRequest() {
        return new TaxSlabRequest(
                "NEW", "2025-26",
                new BigDecimal("300000"), new BigDecimal("700000"),
                new BigDecimal("5.00"), "5% slab"
        );
    }

    @Test
    void getAll_shouldReturnPageOfSlabs() {
        var slabs = List.of(
                createSlab(1L, TaxRegime.NEW, "2025-26", new BigDecimal("0"), new BigDecimal("300000"), new BigDecimal("0")),
                createSlab(2L, TaxRegime.NEW, "2025-26", new BigDecimal("300000"), new BigDecimal("700000"), new BigDecimal("5"))
        );
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<>(slabs, pageable, 2);
        when(taxSlabRepository.findAll(pageable)).thenReturn(page);

        var result = taxSlabService.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("NEW", result.getContent().get(0).regime());
        assertEquals("NEW", result.getContent().get(1).regime());
        verify(taxSlabRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 20);
        var page = new PageImpl<TaxSlab>(List.of(), pageable, 0);
        when(taxSlabRepository.findAll(pageable)).thenReturn(page);

        var result = taxSlabService.getAll(pageable);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getById_shouldReturnSlab() {
        var slab = createSlab(1L, TaxRegime.NEW, "2025-26", new BigDecimal("0"), new BigDecimal("300000"), new BigDecimal("0"));
        when(taxSlabRepository.findById(1L)).thenReturn(Optional.of(slab));

        var result = taxSlabService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("NEW", result.regime());
        assertEquals("2025-26", result.financialYear());
        assertEquals(new BigDecimal("0"), result.slabFrom());
        assertEquals(new BigDecimal("300000"), result.slabTo());
        assertEquals(new BigDecimal("0"), result.rate());
        assertTrue(result.active());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(taxSlabRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taxSlabService.getById(999L));
    }

    @Test
    void getByRegimeAndYear_shouldReturnSlabs() {
        var slabs = List.of(
                createSlab(1L, TaxRegime.NEW, "2025-26", new BigDecimal("0"), new BigDecimal("300000"), new BigDecimal("0"))
        );
        when(taxSlabRepository.findByRegimeAndFinancialYearAndActiveOrderBySlabFromAsc(TaxRegime.NEW, "2025-26", true))
                .thenReturn(slabs);

        var result = taxSlabService.getByRegimeAndYear("NEW", "2025-26");

        assertEquals(1, result.size());
        assertEquals("NEW", result.get(0).regime());
        assertEquals("2025-26", result.get(0).financialYear());
    }

    @Test
    void create_shouldCreateAndReturnSlab() {
        var request = createRequest();
        when(taxSlabRepository.existsByRegimeAndFinancialYearAndSlabFrom(TaxRegime.NEW, "2025-26", new BigDecimal("300000")))
                .thenReturn(false);
        when(taxSlabRepository.save(any(TaxSlab.class))).thenAnswer(invocation -> {
            TaxSlab s = invocation.getArgument(0);
            s.setId(1L);
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        var result = taxSlabService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("NEW", result.regime());
        assertEquals("2025-26", result.financialYear());
        assertEquals(new BigDecimal("300000"), result.slabFrom());
        assertEquals(new BigDecimal("700000"), result.slabTo());
        assertEquals(new BigDecimal("5.00"), result.rate());
        verify(taxSlabRepository).save(any(TaxSlab.class));
    }

    @Test
    void create_shouldThrowWhenDuplicate() {
        var request = createRequest();
        when(taxSlabRepository.existsByRegimeAndFinancialYearAndSlabFrom(TaxRegime.NEW, "2025-26", new BigDecimal("300000")))
                .thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> taxSlabService.create(request));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(taxSlabRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateAndReturnSlab() {
        var slab = createSlab(1L, TaxRegime.NEW, "2025-26", new BigDecimal("300000"), new BigDecimal("700000"), new BigDecimal("5"));
        var request = new TaxSlabRequest("NEW", "2025-26",
                new BigDecimal("300000"), new BigDecimal("700000"),
                new BigDecimal("10.00"), "Updated slab");
        when(taxSlabRepository.findById(1L)).thenReturn(Optional.of(slab));
        when(taxSlabRepository.save(any(TaxSlab.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = taxSlabService.update(1L, request);

        assertNotNull(result);
        assertEquals("NEW", result.regime());
        assertEquals(new BigDecimal("10.00"), result.rate());
        verify(taxSlabRepository).save(any(TaxSlab.class));
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        var request = createRequest();
        when(taxSlabRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taxSlabService.update(999L, request));
        verify(taxSlabRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNewKeyAlreadyExists() {
        var slab = createSlab(1L, TaxRegime.NEW, "2025-26", new BigDecimal("0"), new BigDecimal("300000"), new BigDecimal("0"));
        var request = new TaxSlabRequest("NEW", "2025-26",
                new BigDecimal("300000"), new BigDecimal("700000"),
                new BigDecimal("5.00"), "Changed slab");
        when(taxSlabRepository.findById(1L)).thenReturn(Optional.of(slab));
        when(taxSlabRepository.existsByRegimeAndFinancialYearAndSlabFrom(TaxRegime.NEW, "2025-26", new BigDecimal("300000")))
                .thenReturn(true);

        var ex = assertThrows(BadRequestException.class,
                () -> taxSlabService.update(1L, request));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(taxSlabRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameKey() {
        var slab = createSlab(1L, TaxRegime.NEW, "2025-26", new BigDecimal("300000"), new BigDecimal("700000"), new BigDecimal("5"));
        var request = new TaxSlabRequest("NEW", "2025-26",
                new BigDecimal("300000"), new BigDecimal("700000"),
                new BigDecimal("10.00"), "Updated rate");
        when(taxSlabRepository.findById(1L)).thenReturn(Optional.of(slab));
        when(taxSlabRepository.save(any(TaxSlab.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = taxSlabService.update(1L, request);

        assertEquals(new BigDecimal("10.00"), result.rate());
        verify(taxSlabRepository).save(any(TaxSlab.class));
    }

    @Test
    void delete_shouldDeleteSlab() {
        var slab = createSlab(1L, TaxRegime.NEW, "2025-26", new BigDecimal("0"), new BigDecimal("300000"), new BigDecimal("0"));
        when(taxSlabRepository.findById(1L)).thenReturn(Optional.of(slab));

        taxSlabService.delete(1L);

        verify(taxSlabRepository).delete(slab);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(taxSlabRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taxSlabService.delete(999L));
        verify(taxSlabRepository, never()).delete(any());
    }
}
