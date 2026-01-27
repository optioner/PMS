package com.pm.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pm.model.enums.TaskPriority;
import com.pm.model.enums.TaskStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "story_points", precision = 4, scale = 1)
    private BigDecimal storyPoints;

    @Column(name = "estimated_hours", precision = 6, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "actual_hours", precision = 6, scale = 2)
    private BigDecimal actualHours = BigDecimal.ZERO;

    @Column(name = "planned_start_date")
    private LocalDateTime plannedStartDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignee_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User reporter;

    // Self-referencing for sub-tasks (Splitting)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    @JsonIgnoreProperties({"subTasks", "dependencies", "project", "hibernateLazyInitializer", "handler"})
    private Task parentTask;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Task> subTasks = new HashSet<>();

    // Dependencies
    @ManyToMany
    @JoinTable(
        name = "task_dependencies",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "depends_on_task_id")
    )
    @JsonIgnoreProperties({"dependencies", "subTasks", "project", "hibernateLazyInitializer", "handler"})
    private Set<Task> dependencies = new HashSet<>();

    // Tags
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "task_tags",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

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
