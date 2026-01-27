package com.pm.repository;

import com.pm.model.entity.ProjectMember;
import com.pm.model.entity.ProjectMemberKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberKey> {
    List<ProjectMember> findByProjectId(Long projectId);
    List<ProjectMember> findByUserId(Long userId);
}
