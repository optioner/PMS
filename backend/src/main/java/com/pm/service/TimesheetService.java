package com.pm.service;

import com.pm.model.entity.Task;
import com.pm.model.entity.Timesheet;
import com.pm.model.entity.User;
import com.pm.repository.TaskRepository;
import com.pm.repository.TimesheetRepository;
import com.pm.repository.UserRepository;
import com.pm.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

import com.pm.model.enums.TimesheetStatus;
import java.time.LocalDateTime;

import com.pm.model.enums.NotificationType;

@Service
public class TimesheetService {
    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Timesheet> getMyTimesheets() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return timesheetRepository.findByUserId(userDetails.getId());
    }

    public Timesheet createTimesheet(Timesheet timesheet, Long taskId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        timesheet.setUser(user);
        timesheet.setTask(task);
        timesheet.setStatus(TimesheetStatus.DRAFT);
        
        return timesheetRepository.save(timesheet);
    }

    public Timesheet submitTimesheet(Long id) {
        Timesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));
        // Validate user ownership...
        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        timesheet.setSubmittedAt(LocalDateTime.now());
        return timesheetRepository.save(timesheet);
    }

    public Timesheet approveTimesheet(Long id) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User approver = userRepository.findById(userDetails.getId()).orElseThrow();

        Timesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));
        
        timesheet.setStatus(TimesheetStatus.APPROVED);
        timesheet.setApprovedBy(approver);
        timesheet.setApprovedAt(LocalDateTime.now());
        
        // Update task actual hours
        Task task = timesheet.getTask();
        task.setActualHours(task.getActualHours().add(timesheet.getHours()));
        taskRepository.save(task);

        // Notification
        notificationService.createNotification(
            timesheet.getUser(),
            NotificationType.TIMESHEET_APPROVED,
            "Timesheet Approved",
            "Your timesheet for task '" + task.getTitle() + "' has been approved.",
            "timesheet",
            timesheet.getId()
        );

        return timesheetRepository.save(timesheet);
    }

    public Timesheet rejectTimesheet(Long id, String reason) {
        Timesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));
        
        timesheet.setStatus(TimesheetStatus.REJECTED);
        timesheet.setRejectionReason(reason);
        
        Task task = timesheet.getTask();

        // Notification
        notificationService.createNotification(
            timesheet.getUser(),
            NotificationType.TIMESHEET_REJECTED,
            "Timesheet Rejected",
            "Your timesheet for task '" + task.getTitle() + "' has been rejected. Reason: " + reason,
            "timesheet",
            timesheet.getId()
        );
        
        return timesheetRepository.save(timesheet);
    }
}
