package com.pm.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pm.model.enums.ProjectPriority;
import com.pm.model.enums.ProjectStatus;
import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 20, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User createdBy;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "estimated_end_date")
    private LocalDate estimatedEndDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProjectStatus status = ProjectStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProjectPriority priority = ProjectPriority.MEDIUM;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
