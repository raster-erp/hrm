package com.raster.hrm.tds.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "professional_tax_slabs")
public class ProfessionalTaxSlab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "slab_from", nullable = false, precision = 14, scale = 2)
    private BigDecimal slabFrom;

    @Column(name = "slab_to", precision = 14, scale = 2)
    private BigDecimal slabTo;

    @Column(name = "monthly_tax", nullable = false, precision = 14, scale = 2)
    private BigDecimal monthlyTax;

    @Column(name = "february_tax", precision = 14, scale = 2)
    private BigDecimal februaryTax;

    @Column(name = "active", nullable = false)
    private boolean active = true;

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public BigDecimal getSlabFrom() {
        return slabFrom;
    }

    public void setSlabFrom(BigDecimal slabFrom) {
        this.slabFrom = slabFrom;
    }

    public BigDecimal getSlabTo() {
        return slabTo;
    }

    public void setSlabTo(BigDecimal slabTo) {
        this.slabTo = slabTo;
    }

    public BigDecimal getMonthlyTax() {
        return monthlyTax;
    }

    public void setMonthlyTax(BigDecimal monthlyTax) {
        this.monthlyTax = monthlyTax;
    }

    public BigDecimal getFebruaryTax() {
        return februaryTax;
    }

    public void setFebruaryTax(BigDecimal februaryTax) {
        this.februaryTax = februaryTax;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
