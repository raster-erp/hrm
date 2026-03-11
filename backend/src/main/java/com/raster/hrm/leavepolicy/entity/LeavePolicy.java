package com.raster.hrm.leavepolicy.entity;

import com.raster.hrm.leavetype.entity.LeaveType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_policies")
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Enumerated(EnumType.STRING)
    @Column(name = "accrual_frequency", nullable = false, length = 20)
    private AccrualFrequency accrualFrequency;

    @Column(name = "accrual_days", nullable = false)
    private BigDecimal accrualDays;

    @Column(name = "max_accumulation")
    private BigDecimal maxAccumulation;

    @Column(name = "carry_forward_limit")
    private BigDecimal carryForwardLimit;

    @Column(name = "pro_rata_for_new_joiners", nullable = false)
    private boolean proRataForNewJoiners = false;

    @Column(name = "min_service_days_required", nullable = false)
    private int minServiceDaysRequired = 0;

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

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public AccrualFrequency getAccrualFrequency() {
        return accrualFrequency;
    }

    public void setAccrualFrequency(AccrualFrequency accrualFrequency) {
        this.accrualFrequency = accrualFrequency;
    }

    public BigDecimal getAccrualDays() {
        return accrualDays;
    }

    public void setAccrualDays(BigDecimal accrualDays) {
        this.accrualDays = accrualDays;
    }

    public BigDecimal getMaxAccumulation() {
        return maxAccumulation;
    }

    public void setMaxAccumulation(BigDecimal maxAccumulation) {
        this.maxAccumulation = maxAccumulation;
    }

    public BigDecimal getCarryForwardLimit() {
        return carryForwardLimit;
    }

    public void setCarryForwardLimit(BigDecimal carryForwardLimit) {
        this.carryForwardLimit = carryForwardLimit;
    }

    public boolean isProRataForNewJoiners() {
        return proRataForNewJoiners;
    }

    public void setProRataForNewJoiners(boolean proRataForNewJoiners) {
        this.proRataForNewJoiners = proRataForNewJoiners;
    }

    public int getMinServiceDaysRequired() {
        return minServiceDaysRequired;
    }

    public void setMinServiceDaysRequired(int minServiceDaysRequired) {
        this.minServiceDaysRequired = minServiceDaysRequired;
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
