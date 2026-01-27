package com.pm.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pm.model.enums.ProjectRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_members")
@Data
@NoArgsConstructor
public class ProjectMember {
    @EmbeddedId
    private ProjectMemberKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProjectRole role = ProjectRole.MEMBER;

    @Column(name = "joined_at", insertable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        // joinedAt uses DEFAULT CURRENT_TIMESTAMP in DB, but JPA might need manual set if not using @CreationTimestamp or database default definition fully with JPA
        // Let's rely on DB default or set it manually if needed.
    }
}
