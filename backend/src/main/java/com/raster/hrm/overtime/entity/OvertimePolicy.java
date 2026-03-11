package com.raster.hrm.overtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "overtime_policies")
public class OvertimePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private OvertimePolicyType type;

    @Column(name = "rate_multiplier", nullable = false, precision = 4, scale = 2)
    private BigDecimal rateMultiplier;

    @Column(name = "min_overtime_minutes", nullable = false)
    private Integer minOvertimeMinutes = 0;

    @Column(name = "max_overtime_minutes_per_day")
    private Integer maxOvertimeMinutesPerDay;

    @Column(name = "max_overtime_minutes_per_month")
    private Integer maxOvertimeMinutesPerMonth;

    @Column(name = "requires_approval", nullable = false)
    private boolean requiresApproval = true;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OvertimePolicyType getType() {
        return type;
    }

    public void setType(OvertimePolicyType type) {
        this.type = type;
    }

    public BigDecimal getRateMultiplier() {
        return rateMultiplier;
    }

    public void setRateMultiplier(BigDecimal rateMultiplier) {
        this.rateMultiplier = rateMultiplier;
    }

    public Integer getMinOvertimeMinutes() {
        return minOvertimeMinutes;
    }

    public void setMinOvertimeMinutes(Integer minOvertimeMinutes) {
        this.minOvertimeMinutes = minOvertimeMinutes;
    }

    public Integer getMaxOvertimeMinutesPerDay() {
        return maxOvertimeMinutesPerDay;
    }

    public void setMaxOvertimeMinutesPerDay(Integer maxOvertimeMinutesPerDay) {
        this.maxOvertimeMinutesPerDay = maxOvertimeMinutesPerDay;
    }

    public Integer getMaxOvertimeMinutesPerMonth() {
        return maxOvertimeMinutesPerMonth;
    }

    public void setMaxOvertimeMinutesPerMonth(Integer maxOvertimeMinutesPerMonth) {
        this.maxOvertimeMinutesPerMonth = maxOvertimeMinutesPerMonth;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
