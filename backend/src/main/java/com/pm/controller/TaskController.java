package com.pm.controller;

import com.pm.model.entity.Task;
import com.pm.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @GetMapping("/projects/{projectId}/tasks")
    public List<Task> getTasksByProject(@PathVariable Long projectId) {
        return taskService.getTasksByProject(projectId);
    }

    @PostMapping("/projects/{projectId}/tasks")
    public Task createTask(@PathVariable Long projectId, @RequestBody Task task) {
        return taskService.createTask(projectId, task);
    }

    @PutMapping("/tasks/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }
}
