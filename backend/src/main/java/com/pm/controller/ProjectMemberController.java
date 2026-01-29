package com.pm.controller;

import com.pm.model.entity.ProjectMember;
import com.pm.repository.ProjectMemberRepository;
import com.pm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.pm.model.enums.ProjectRole;
import com.pm.service.ProjectService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1")
public class ProjectMemberController {

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectService projectService;

    @GetMapping("/projects/{projectId}/members")
    public List<ProjectMember> getProjectMembers(@PathVariable Long projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }

    @PostMapping("/projects/{projectId}/members")
    public ProjectMember addMember(@PathVariable Long projectId, @RequestBody AddMemberRequest request) {
        return projectService.addMember(projectId, request.getUserId(), request.getRole());
    }

    @DeleteMapping("/projects/{projectId}/members/{userId}")
    public void removeMember(@PathVariable Long projectId, @PathVariable Long userId) {
        projectService.removeMember(projectId, userId);
    }

    public static class AddMemberRequest {
        private Long userId;
        private ProjectRole role;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public ProjectRole getRole() { return role; }
        public void setRole(ProjectRole role) { this.role = role; }
    }
}
