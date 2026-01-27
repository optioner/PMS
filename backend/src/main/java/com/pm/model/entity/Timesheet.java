package com.pm.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pm.model.enums.TimesheetStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "timesheets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "task_id", "date"})
})
@Data
@NoArgsConstructor
public class Timesheet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Task task;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal hours;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TimesheetStatus status = TimesheetStatus.DRAFT;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
