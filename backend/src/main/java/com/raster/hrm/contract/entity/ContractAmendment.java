package com.raster.hrm.contract.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_amendments")
public class ContractAmendment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private EmploymentContract contract;

    @Column(name = "amendment_date", nullable = false)
    private LocalDate amendmentDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "old_terms", columnDefinition = "TEXT")
    private String oldTerms;

    @Column(name = "new_terms", columnDefinition = "TEXT")
    private String newTerms;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EmploymentContract getContract() {
        return contract;
    }

    public void setContract(EmploymentContract contract) {
        this.contract = contract;
    }

    public LocalDate getAmendmentDate() {
        return amendmentDate;
    }

    public void setAmendmentDate(LocalDate amendmentDate) {
        this.amendmentDate = amendmentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOldTerms() {
        return oldTerms;
    }

    public void setOldTerms(String oldTerms) {
        this.oldTerms = oldTerms;
    }

    public String getNewTerms() {
        return newTerms;
    }

    public void setNewTerms(String newTerms) {
        this.newTerms = newTerms;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
