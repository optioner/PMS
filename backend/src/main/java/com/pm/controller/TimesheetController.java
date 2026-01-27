package com.pm.controller;

import com.pm.model.entity.Timesheet;
import com.pm.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/timesheets")
public class TimesheetController {
    @Autowired
    private TimesheetService timesheetService;

    @GetMapping
    public List<Timesheet> getMyTimesheets() {
        return timesheetService.getMyTimesheets();
    }

    @PostMapping
    public Timesheet createTimesheet(@RequestBody Timesheet timesheet, @RequestParam Long taskId) {
        return timesheetService.createTimesheet(timesheet, taskId);
    }

    @PostMapping("/{id}/submit")
    public Timesheet submitTimesheet(@PathVariable Long id) {
        return timesheetService.submitTimesheet(id);
    }

    @PostMapping("/{id}/approve")
    public Timesheet approveTimesheet(@PathVariable Long id) {
        return timesheetService.approveTimesheet(id);
    }

    @PostMapping("/{id}/reject")
    public Timesheet rejectTimesheet(@PathVariable Long id, @RequestBody String reason) {
        return timesheetService.rejectTimesheet(id, reason);
    }
}
