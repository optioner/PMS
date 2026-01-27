package com.pm.service;

import com.pm.model.dto.response.DashboardStatsResponse;
import com.pm.model.enums.TaskStatus;
import com.pm.repository.TaskRepository;
import com.pm.repository.TimesheetRepository;
import com.pm.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import com.pm.model.dto.response.ProjectReportResponse;
import com.pm.model.entity.Project;
import com.pm.model.entity.Task;
import com.pm.model.entity.User;
import com.pm.repository.ProjectRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public DashboardStatsResponse getPersonalStats() {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getId();

        DashboardStatsResponse response = new DashboardStatsResponse();
        
        // Task Stats
        DashboardStatsResponse.TaskStats taskStats = new DashboardStatsResponse.TaskStats();
        taskStats.setTodo(taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.TODO));
        taskStats.setInProgress(taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.IN_PROGRESS));
        taskStats.setReview(taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.REVIEW));
        taskStats.setDone(taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.DONE));
        taskStats.setOverdue(taskRepository.countOverdueTasks(userId));
        response.setTaskStats(taskStats);

        // Time Stats
        DashboardStatsResponse.TimeStats timeStats = new DashboardStatsResponse.TimeStats();
        
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = now.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal weekHours = timesheetRepository.sumHoursByUserIdAndDateRange(userId, startOfWeek, endOfWeek);
        BigDecimal monthHours = timesheetRepository.sumHoursByUserIdAndDateRange(userId, startOfMonth, endOfMonth);

        timeStats.setWeekTotal(weekHours != null ? weekHours : BigDecimal.ZERO);
        timeStats.setMonthTotal(monthHours != null ? monthHours : BigDecimal.ZERO);
        response.setTimeStats(timeStats);

        return response;
    }

    public ProjectReportResponse getProjectReport(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        
        ProjectReportResponse response = new ProjectReportResponse();
        response.setProjectId(project.getId());
        response.setProjectName(project.getName());
        
        // Task Stats
        ProjectReportResponse.TaskStats taskStats = new ProjectReportResponse.TaskStats();
        taskStats.setTotal(tasks.size());
        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        taskStats.setCompleted(completed);
        taskStats.setCompletionRate(tasks.isEmpty() ? 0 : (double) completed / tasks.size() * 100);
        long overdue = tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(java.time.LocalDateTime.now()) && t.getStatus() != TaskStatus.DONE).count();
        taskStats.setOverdue(overdue);
        response.setTaskStats(taskStats);
        
        // Time Stats
        ProjectReportResponse.TimeStats timeStats = new ProjectReportResponse.TimeStats();
        BigDecimal totalEstimated = tasks.stream()
                .map(Task::getEstimatedHours)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalActual = tasks.stream()
                .map(Task::getActualHours)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        timeStats.setTotalEstimated(totalEstimated);
        timeStats.setTotalActual(totalActual);
        response.setTimeStats(timeStats);
        
        // Task Distribution
        Map<String, Long> distribution = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            long count = tasks.stream().filter(t -> t.getStatus() == status).count();
            distribution.put(status.name(), count);
        }
        response.setTaskDistribution(distribution);

        // User Efficiency
        Map<User, List<Task>> tasksByUser = tasks.stream()
            .filter(t -> t.getAssignee() != null)
            .collect(Collectors.groupingBy(Task::getAssignee));
        
        List<ProjectReportResponse.UserEfficiency> efficiencyList = new ArrayList<>();
        
        for (Map.Entry<User, List<Task>> entry : tasksByUser.entrySet()) {
            User user = entry.getKey();
            List<Task> userTasks = entry.getValue();
            
            ProjectReportResponse.UserEfficiency eff = new ProjectReportResponse.UserEfficiency();
            eff.setUserName(user.getFullName());
            eff.setAssignedTasks(userTasks.size());
            eff.setCompletedTasks(userTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count());
            
            BigDecimal userEst = userTasks.stream()
                .map(Task::getEstimatedHours)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            BigDecimal userAct = userTasks.stream()
                .map(Task::getActualHours)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            eff.setTotalEstimatedHours(userEst);
            eff.setTotalActualHours(userAct);
            
            if (userAct.compareTo(BigDecimal.ZERO) > 0) {
                // Efficiency = (Est / Act) * 100. Higher is better (finished faster than est).
                double ratio = userEst.doubleValue() / userAct.doubleValue() * 100.0;
                eff.setEfficiencyRatio(ratio);
            } else {
                eff.setEfficiencyRatio(0.0);
            }
            
            efficiencyList.add(eff);
        }
        response.setUserEfficiency(efficiencyList);
        
        return response;
    }
}
