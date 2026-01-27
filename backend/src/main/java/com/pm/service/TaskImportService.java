package com.pm.service;

import com.pm.model.entity.Project;
import com.pm.model.entity.Task;
import com.pm.model.entity.User;
import com.pm.model.enums.TaskPriority;
import com.pm.model.enums.TaskStatus;
import com.pm.repository.ProjectRepository;
import com.pm.repository.TaskRepository;
import com.pm.repository.UserRepository;
import com.pm.security.UserDetailsImpl;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskImportService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public int importTasks(Long projectId, MultipartFile file) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId()).orElseThrow();

        List<Task> tasksToSave = new ArrayList<>();

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord csvRecord : csvParser) {
                Task task = new Task();
                task.setProject(project);
                task.setReporter(currentUser);
                
                // Title (Required)
                if (csvRecord.isMapped("Title")) {
                    task.setTitle(csvRecord.get("Title"));
                } else {
                    continue; // Skip if no title
                }

                // Description
                if (csvRecord.isMapped("Description")) {
                    task.setDescription(csvRecord.get("Description"));
                }

                // Priority
                if (csvRecord.isMapped("Priority")) {
                    try {
                        task.setPriority(TaskPriority.valueOf(csvRecord.get("Priority").toUpperCase()));
                    } catch (Exception e) {
                        task.setPriority(TaskPriority.MEDIUM);
                    }
                }

                // Status
                if (csvRecord.isMapped("Status")) {
                    try {
                        task.setStatus(TaskStatus.valueOf(csvRecord.get("Status").toUpperCase().replace(" ", "_")));
                    } catch (Exception e) {
                        task.setStatus(TaskStatus.TODO);
                    }
                }

                // Story Points
                if (csvRecord.isMapped("Story Points")) {
                    try {
                        task.setStoryPoints(new BigDecimal(csvRecord.get("Story Points")));
                    } catch (Exception e) {
                        // ignore
                    }
                }

                // Assignee (Email)
                if (csvRecord.isMapped("Assignee Email")) {
                    String email = csvRecord.get("Assignee Email");
                    userRepository.findByEmail(email).ifPresent(task::setAssignee);
                }

                tasksToSave.add(task);
            }

            taskRepository.saveAll(tasksToSave);
            return tasksToSave.size();

        } catch (Exception e) {
            throw new RuntimeException("Fail to parse CSV file: " + e.getMessage());
        }
    }
}
