package com.pm.service;

import com.pm.model.entity.Project;
import com.pm.model.entity.ProjectMember;
import com.pm.model.entity.ProjectMemberKey;
import com.pm.model.entity.User;
import com.pm.model.enums.ProjectRole;
import com.pm.repository.ProjectMemberRepository;
import com.pm.repository.ProjectRepository;
import com.pm.repository.UserRepository;
import com.pm.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    public List<Project> getAllProjectsForCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return projectRepository.findByMemberUserId(userDetails.getId());
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public ProjectMember addMember(Long projectId, Long userId, ProjectRole role) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        ProjectMember member = new ProjectMember();
        member.setId(new ProjectMemberKey(projectId, userId));
        member.setProject(project);
        member.setUser(user);
        member.setRole(role != null ? role : ProjectRole.MEMBER);
        return projectMemberRepository.save(member);
    }

    public void removeMember(Long projectId, Long userId) {
        ProjectMemberKey key = new ProjectMemberKey(projectId, userId);
        if (projectMemberRepository.existsById(key)) {
            projectMemberRepository.deleteById(key);
        }
    }

    public Project createProject(Project project) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("User not found"));
        project.setManager(user);
        project.setCreatedBy(user);
        
        // Generate Project Code: PROJ-YYYYMMDD-XXX
        String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        project.setCode("PROJ-" + datePart + "-" + System.currentTimeMillis() % 1000);

        // Set default status if null (though entity might handle it)
        if (project.getStatus() == null) {
            project.setStatus(com.pm.model.enums.ProjectStatus.DRAFT);
        }
        Project savedProject = projectRepository.save(project);

        // Add creator as Project Manager member
        ProjectMember member = new ProjectMember();
        member.setId(new ProjectMemberKey(savedProject.getId(), user.getId()));
        member.setProject(savedProject);
        member.setUser(user);
        member.setRole(ProjectRole.MANAGER);
        projectMemberRepository.save(member);

        return savedProject;
    }

    public Project getProjectById(Long id) {
        // In a real app, check permission
        return projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}
