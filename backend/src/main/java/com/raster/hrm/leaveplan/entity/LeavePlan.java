package com.raster.hrm.leaveplan.entity;

import com.raster.hrm.employee.entity.Employee;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_plans")
public class LeavePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "planned_from_date", nullable = false)
    private LocalDate plannedFromDate;

    @Column(name = "planned_to_date", nullable = false)
    private LocalDate plannedToDate;

    @Column(name = "number_of_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal numberOfDays;

    @Column(name = "notes", length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeavePlanStatus status = LeavePlanStatus.PLANNED;

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

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public LocalDate getPlannedFromDate() {
        return plannedFromDate;
    }

    public void setPlannedFromDate(LocalDate plannedFromDate) {
        this.plannedFromDate = plannedFromDate;
    }

    public LocalDate getPlannedToDate() {
        return plannedToDate;
    }

    public void setPlannedToDate(LocalDate plannedToDate) {
        this.plannedToDate = plannedToDate;
    }

    public BigDecimal getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(BigDecimal numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LeavePlanStatus getStatus() {
        return status;
    }

    public void setStatus(LeavePlanStatus status) {
        this.status = status;
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
