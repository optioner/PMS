package com.pm.controller;

import com.pm.model.dto.response.DashboardStatsResponse;
import com.pm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pm.model.dto.response.ProjectReportResponse;
import org.springframework.web.bind.annotation.PathVariable;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/personal")
    public DashboardStatsResponse getPersonalStats() {
        return dashboardService.getPersonalStats();
    }

    @GetMapping("/project/{projectId}")
    public ProjectReportResponse getProjectReport(@PathVariable Long projectId) {
        return dashboardService.getProjectReport(projectId);
    }
}
