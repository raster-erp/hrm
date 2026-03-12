package com.raster.hrm.holiday.service;

import com.raster.hrm.exception.ResourceNotFoundException;
import com.raster.hrm.holiday.dto.HolidayRequest;
import com.raster.hrm.holiday.dto.HolidayResponse;
import com.raster.hrm.holiday.entity.Holiday;
import com.raster.hrm.holiday.entity.HolidayType;
import com.raster.hrm.holiday.repository.HolidayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class HolidayService {

    private static final Logger log = LoggerFactory.getLogger(HolidayService.class);

    private final HolidayRepository holidayRepository;

    public HolidayService(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    // ── Create ──────────────────────────────────────────────────────────

    public HolidayResponse create(HolidayRequest request) {
        log.info("Creating holiday '{}' on {}", request.name(), request.date());

        var holiday = new Holiday();
        holiday.setName(request.name());
        holiday.setDate(request.date());
        holiday.setType(request.type());
        holiday.setRegion(request.region());
        holiday.setDescription(request.description());
        holiday.setActive(true);

        Holiday saved = holidayRepository.save(holiday);
        log.info("Holiday created with id {}", saved.getId());
        return toResponse(saved);
    }

    // ── Update ──────────────────────────────────────────────────────────

    public HolidayResponse update(Long id, HolidayRequest request) {
        log.info("Updating holiday {}", id);

        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", "id", id));

        holiday.setName(request.name());
        holiday.setDate(request.date());
        holiday.setType(request.type());
        holiday.setRegion(request.region());
        holiday.setDescription(request.description());

        Holiday saved = holidayRepository.save(holiday);
        log.info("Holiday {} updated", id);
        return toResponse(saved);
    }

    // ── Deactivate ──────────────────────────────────────────────────────

    public HolidayResponse deactivate(Long id) {
        log.info("Deactivating holiday {}", id);

        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", "id", id));

        holiday.setActive(false);

        Holiday saved = holidayRepository.save(holiday);
        log.info("Holiday {} deactivated", id);
        return toResponse(saved);
    }

    // ── Queries ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public HolidayResponse getById(Long id) {
        log.debug("Fetching holiday by id {}", id);
        return holidayRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<HolidayResponse> getAll(Pageable pageable) {
        log.debug("Fetching all holidays");
        return holidayRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<HolidayResponse> getByType(HolidayType type, Pageable pageable) {
        log.debug("Fetching holidays by type {}", type);
        return holidayRepository.findByType(type, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<HolidayResponse> getByRegion(String region, Pageable pageable) {
        log.debug("Fetching holidays by region {}", region);
        return holidayRepository.findByRegion(region, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<HolidayResponse> getByDateRange(LocalDate start, LocalDate end) {
        log.debug("Fetching holidays between {} and {}", start, end);
        return holidayRepository.findByDateBetween(start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HolidayResponse> getActiveByDateRange(LocalDate start, LocalDate end) {
        log.debug("Fetching active holidays between {} and {}", start, end);
        return holidayRepository.findByDateBetweenAndActiveTrue(start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HolidayResponse> getActiveByRegionAndDateRange(String region, LocalDate start, LocalDate end) {
        log.debug("Fetching active holidays for region {} between {} and {}", region, start, end);
        return holidayRepository.findByRegionAndDateBetweenAndActiveTrue(region, start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Private helpers ─────────────────────────────────────────────────

    private HolidayResponse toResponse(Holiday holiday) {
        return new HolidayResponse(
                holiday.getId(),
                holiday.getName(),
                holiday.getDate(),
                holiday.getType(),
                holiday.getRegion(),
                holiday.getDescription(),
                holiday.isActive(),
                holiday.getCreatedAt(),
                holiday.getUpdatedAt()
        );
    }
}
