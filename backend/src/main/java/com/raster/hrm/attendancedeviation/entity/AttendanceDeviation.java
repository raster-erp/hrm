package com.raster.hrm.attendancedeviation.entity;

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
import java.time.LocalTime;

@Entity
@Table(name = "attendance_deviations")
public class AttendanceDeviation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "deviation_date", nullable = false)
    private LocalDate deviationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private DeviationType type;

    @Column(name = "deviation_minutes", nullable = false)
    private Integer deviationMinutes;

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;

    @Column(name = "actual_time", nullable = false)
    private LocalDateTime actualTime;

    @Column(name = "grace_period_minutes", nullable = false)
    private Integer gracePeriodMinutes = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "penalty_action", nullable = false, length = 30)
    private PenaltyAction penaltyAction = PenaltyAction.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeviationStatus status = DeviationStatus.PENDING;

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

    public LocalDate getDeviationDate() {
        return deviationDate;
    }

    public void setDeviationDate(LocalDate deviationDate) {
        this.deviationDate = deviationDate;
    }

    public DeviationType getType() {
        return type;
    }

    public void setType(DeviationType type) {
        this.type = type;
    }

    public Integer getDeviationMinutes() {
        return deviationMinutes;
    }

    public void setDeviationMinutes(Integer deviationMinutes) {
        this.deviationMinutes = deviationMinutes;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public LocalDateTime getActualTime() {
        return actualTime;
    }

    public void setActualTime(LocalDateTime actualTime) {
        this.actualTime = actualTime;
    }

    public Integer getGracePeriodMinutes() {
        return gracePeriodMinutes;
    }

    public void setGracePeriodMinutes(Integer gracePeriodMinutes) {
        this.gracePeriodMinutes = gracePeriodMinutes;
    }

    public PenaltyAction getPenaltyAction() {
        return penaltyAction;
    }

    public void setPenaltyAction(PenaltyAction penaltyAction) {
        this.penaltyAction = penaltyAction;
    }

    public DeviationStatus getStatus() {
        return status;
    }

    public void setStatus(DeviationStatus status) {
        this.status = status;
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
