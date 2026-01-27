package com.pm.service;

import com.pm.model.entity.Project;
import com.pm.model.entity.User;
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

    public List<Project> getAllProjectsForCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return projectRepository.findByManagerId(userDetails.getId());
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
        return projectRepository.save(project);
    }

    public Project getProjectById(Long id) {
        // In a real app, check permission
        return projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}
