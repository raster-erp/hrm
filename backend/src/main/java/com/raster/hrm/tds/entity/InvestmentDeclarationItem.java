package com.raster.hrm.tds.entity;

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
@Table(name = "investment_declaration_items")
public class InvestmentDeclarationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declaration_id", nullable = false)
    private InvestmentDeclaration declaration;

    @Column(name = "section", nullable = false, length = 20)
    private String section;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "declared_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal declaredAmount;

    @Column(name = "verified_amount", precision = 14, scale = 2)
    private BigDecimal verifiedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "proof_status", nullable = false, length = 20)
    private ProofStatus proofStatus = ProofStatus.PENDING;

    @Column(name = "proof_document_name", length = 200)
    private String proofDocumentName;

    @Column(name = "proof_remarks", length = 500)
    private String proofRemarks;

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

    public InvestmentDeclaration getDeclaration() {
        return declaration;
    }

    public void setDeclaration(InvestmentDeclaration declaration) {
        this.declaration = declaration;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDeclaredAmount() {
        return declaredAmount;
    }

    public void setDeclaredAmount(BigDecimal declaredAmount) {
        this.declaredAmount = declaredAmount;
    }

    public BigDecimal getVerifiedAmount() {
        return verifiedAmount;
    }

    public void setVerifiedAmount(BigDecimal verifiedAmount) {
        this.verifiedAmount = verifiedAmount;
    }

    public ProofStatus getProofStatus() {
        return proofStatus;
    }

    public void setProofStatus(ProofStatus proofStatus) {
        this.proofStatus = proofStatus;
    }

    public String getProofDocumentName() {
        return proofDocumentName;
    }

    public void setProofDocumentName(String proofDocumentName) {
        this.proofDocumentName = proofDocumentName;
    }

    public String getProofRemarks() {
        return proofRemarks;
    }

    public void setProofRemarks(String proofRemarks) {
        this.proofRemarks = proofRemarks;
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
