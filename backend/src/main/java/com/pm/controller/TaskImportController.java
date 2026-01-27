package com.pm.controller;

import com.pm.model.dto.response.MessageResponse;
import com.pm.service.TaskImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1")
public class TaskImportController {

    @Autowired
    private TaskImportService taskImportService;

    @PostMapping("/projects/{projectId}/import")
    public ResponseEntity<MessageResponse> importTasks(@PathVariable Long projectId, @RequestParam("file") MultipartFile file) {
        if (!CSVHelper.hasCSVFormat(file)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Please upload a CSV file!"));
        }

        try {
            int count = taskImportService.importTasks(projectId, file);
            return ResponseEntity.ok(new MessageResponse("Uploaded the file successfully: " + file.getOriginalFilename() + ". Imported " + count + " tasks."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("Could not upload the file: " + file.getOriginalFilename() + "! Error: " + e.getMessage()));
        }
    }
    
    // Inner helper class or standalone utility
    public static class CSVHelper {
        public static boolean hasCSVFormat(MultipartFile file) {
            return "text/csv".equals(file.getContentType()) || "application/vnd.ms-excel".equals(file.getContentType());
        }
    }
}
