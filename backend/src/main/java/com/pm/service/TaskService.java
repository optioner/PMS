package com.pm.service;

import com.pm.model.entity.Project;
import com.pm.model.entity.Task;
import com.pm.repository.ProjectRepository;
import com.pm.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.pm.model.entity.User;
import com.pm.repository.UserRepository;
import com.pm.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pm.model.enums.NotificationType;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    public Task createTask(Long projectId, Task task) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User reporter = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        task.setProject(project);
        task.setReporter(reporter);
        
        // Explicitly fetch assignee from DB to ensure it's a managed entity if provided
        if (task.getAssignee() != null && task.getAssignee().getId() != null) {
            User assignee = userRepository.findById(task.getAssignee().getId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null); // Ensure null if invalid ID
        }
        
        if (task.getActualHours() == null) {
            task.setActualHours(BigDecimal.ZERO);
        }
        
        if (task.getAssignee() != null && task.getPlannedStartDate() != null && task.getDueDate() != null) {
            List<Task> overlaps = taskRepository.findOverlappingTasks(
                task.getAssignee().getId(), 
                task.getPlannedStartDate(), 
                task.getDueDate(), 
                0L // ID 0 means new task, checking against existing
            );
            if (!overlaps.isEmpty()) {
                throw new RuntimeException("Task overlap detected! User already has " + overlaps.size() + " task(s) during this period.");
            }
        }
        
        Task savedTask = taskRepository.save(task);

        if (savedTask.getAssignee() != null && !savedTask.getAssignee().getId().equals(reporter.getId())) {
            notificationService.createNotification(
                savedTask.getAssignee(),
                NotificationType.TASK_ASSIGNED,
                "New Task Assigned",
                "You have been assigned to task: " + savedTask.getTitle(),
                "task",
                savedTask.getId()
            );
        }

        return savedTask;
    }

    public Task updateTask(Long id, Task taskDetails) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        User oldAssignee = task.getAssignee();

        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());
        task.setPriority(taskDetails.getPriority());
        task.setPlannedStartDate(taskDetails.getPlannedStartDate());
        task.setDueDate(taskDetails.getDueDate());
        task.setAssignee(taskDetails.getAssignee());
        task.setStoryPoints(taskDetails.getStoryPoints());
        task.setEstimatedHours(taskDetails.getEstimatedHours());
        
        // Auto-update actual start/end based on status
        if (task.getStatus() == com.pm.model.enums.TaskStatus.IN_PROGRESS && task.getStartedAt() == null) {
            task.setStartedAt(LocalDateTime.now());
        }
        if (task.getStatus() == com.pm.model.enums.TaskStatus.DONE && task.getCompletedAt() == null) {
            task.setCompletedAt(LocalDateTime.now());
        }

        // Actual hours are updated via timesheets usually, but direct edit for now if needed
        if (taskDetails.getActualHours() != null) {
            task.setActualHours(taskDetails.getActualHours());
        }

        // Handle Tags
        if (taskDetails.getTags() != null) {
            task.setTags(taskDetails.getTags());
        }

        // Handle Dependencies
        if (taskDetails.getDependencies() != null) {
            task.setDependencies(taskDetails.getDependencies());
        }

        // Handle Parent Task (Splitting)
        if (taskDetails.getParentTask() != null) {
            task.setParentTask(taskDetails.getParentTask());
        }
        
        // Check for overlaps on update if dates or assignee changed
        if (task.getAssignee() != null && task.getPlannedStartDate() != null && task.getDueDate() != null) {
             List<Task> overlaps = taskRepository.findOverlappingTasks(
                task.getAssignee().getId(), 
                task.getPlannedStartDate(), 
                task.getDueDate(), 
                task.getId()
            );
            if (!overlaps.isEmpty()) {
                throw new RuntimeException("Task overlap detected! User already has " + overlaps.size() + " task(s) during this period.");
            }
        }
        
        Task updatedTask = taskRepository.save(task);

        // Notify if assignee changed
        if (updatedTask.getAssignee() != null && (oldAssignee == null || !oldAssignee.getId().equals(updatedTask.getAssignee().getId()))) {
             notificationService.createNotification(
                updatedTask.getAssignee(),
                NotificationType.TASK_ASSIGNED,
                "Task Assigned",
                "You have been assigned to task: " + updatedTask.getTitle(),
                "task",
                updatedTask.getId()
            );
        }

        return updatedTask;
    }
    
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
