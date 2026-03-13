package com.raster.hrm.tds.entity;

import com.raster.hrm.employee.entity.Employee;

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
@Table(name = "tax_computations")
public class TaxComputation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "financial_year", nullable = false, length = 10)
    private String financialYear;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "gross_annual_income", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossAnnualIncome;

    @Column(name = "total_exemptions", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalExemptions;

    @Column(name = "taxable_income", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxableIncome;

    @Column(name = "total_annual_tax", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAnnualTax;

    @Column(name = "monthly_tds", nullable = false, precision = 14, scale = 2)
    private BigDecimal monthlyTds;

    @Column(name = "cess", nullable = false, precision = 14, scale = 2)
    private BigDecimal cess;

    @Column(name = "surcharge", nullable = false, precision = 14, scale = 2)
    private BigDecimal surcharge;

    @Column(name = "tds_deducted_till_date", nullable = false, precision = 14, scale = 2)
    private BigDecimal tdsDeductedTillDate;

    @Column(name = "remaining_tds", nullable = false, precision = 14, scale = 2)
    private BigDecimal remainingTds;

    @Enumerated(EnumType.STRING)
    @Column(name = "regime", nullable = false, length = 10)
    private TaxRegime regime;

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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(String financialYear) {
        this.financialYear = financialYear;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public BigDecimal getGrossAnnualIncome() {
        return grossAnnualIncome;
    }

    public void setGrossAnnualIncome(BigDecimal grossAnnualIncome) {
        this.grossAnnualIncome = grossAnnualIncome;
    }

    public BigDecimal getTotalExemptions() {
        return totalExemptions;
    }

    public void setTotalExemptions(BigDecimal totalExemptions) {
        this.totalExemptions = totalExemptions;
    }

    public BigDecimal getTaxableIncome() {
        return taxableIncome;
    }

    public void setTaxableIncome(BigDecimal taxableIncome) {
        this.taxableIncome = taxableIncome;
    }

    public BigDecimal getTotalAnnualTax() {
        return totalAnnualTax;
    }

    public void setTotalAnnualTax(BigDecimal totalAnnualTax) {
        this.totalAnnualTax = totalAnnualTax;
    }

    public BigDecimal getMonthlyTds() {
        return monthlyTds;
    }

    public void setMonthlyTds(BigDecimal monthlyTds) {
        this.monthlyTds = monthlyTds;
    }

    public BigDecimal getCess() {
        return cess;
    }

    public void setCess(BigDecimal cess) {
        this.cess = cess;
    }

    public BigDecimal getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(BigDecimal surcharge) {
        this.surcharge = surcharge;
    }

    public BigDecimal getTdsDeductedTillDate() {
        return tdsDeductedTillDate;
    }

    public void setTdsDeductedTillDate(BigDecimal tdsDeductedTillDate) {
        this.tdsDeductedTillDate = tdsDeductedTillDate;
    }

    public BigDecimal getRemainingTds() {
        return remainingTds;
    }

    public void setRemainingTds(BigDecimal remainingTds) {
        this.remainingTds = remainingTds;
    }

    public TaxRegime getRegime() {
        return regime;
    }

    public void setRegime(TaxRegime regime) {
        this.regime = regime;
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
