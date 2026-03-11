package com.raster.hrm.attendanceregularization.entity;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "regularization_requests")
public class RegularizationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private RegularizationType type;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "original_punch_in")
    private LocalDateTime originalPunchIn;

    @Column(name = "original_punch_out")
    private LocalDateTime originalPunchOut;

    @Column(name = "corrected_punch_in", nullable = false)
    private LocalDateTime correctedPunchIn;

    @Column(name = "corrected_punch_out", nullable = false)
    private LocalDateTime correctedPunchOut;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RegularizationStatus status = RegularizationStatus.PENDING;

    @Column(name = "approval_level", nullable = false)
    private Integer approvalLevel = 0;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

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

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public RegularizationType getType() {
        return type;
    }

    public void setType(RegularizationType type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getOriginalPunchIn() {
        return originalPunchIn;
    }

    public void setOriginalPunchIn(LocalDateTime originalPunchIn) {
        this.originalPunchIn = originalPunchIn;
    }

    public LocalDateTime getOriginalPunchOut() {
        return originalPunchOut;
    }

    public void setOriginalPunchOut(LocalDateTime originalPunchOut) {
        this.originalPunchOut = originalPunchOut;
    }

    public LocalDateTime getCorrectedPunchIn() {
        return correctedPunchIn;
    }

    public void setCorrectedPunchIn(LocalDateTime correctedPunchIn) {
        this.correctedPunchIn = correctedPunchIn;
    }

    public LocalDateTime getCorrectedPunchOut() {
        return correctedPunchOut;
    }

    public void setCorrectedPunchOut(LocalDateTime correctedPunchOut) {
        this.correctedPunchOut = correctedPunchOut;
    }

    public RegularizationStatus getStatus() {
        return status;
    }

    public void setStatus(RegularizationStatus status) {
        this.status = status;
    }

    public Integer getApprovalLevel() {
        return approvalLevel;
    }

    public void setApprovalLevel(Integer approvalLevel) {
        this.approvalLevel = approvalLevel;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
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
