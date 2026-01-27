package com.pm.controller;

import com.pm.model.entity.ProjectMember;
import com.pm.repository.ProjectMemberRepository;
import com.pm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1")
public class ProjectMemberController {

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/projects/{projectId}/members")
    public List<ProjectMember> getProjectMembers(@PathVariable Long projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }
}
