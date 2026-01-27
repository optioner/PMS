package com.pm.model.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public class ProjectReportResponse {
    private Long projectId;
    private String projectName;
    private TaskStats taskStats;
    private TimeStats timeStats;
    private Map<String, Long> taskDistribution;
    private java.util.List<UserEfficiency> userEfficiency;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public TaskStats getTaskStats() { return taskStats; }
    public void setTaskStats(TaskStats taskStats) { this.taskStats = taskStats; }

    public TimeStats getTimeStats() { return timeStats; }
    public void setTimeStats(TimeStats timeStats) { this.timeStats = timeStats; }

    public Map<String, Long> getTaskDistribution() { return taskDistribution; }
    public void setTaskDistribution(Map<String, Long> taskDistribution) { this.taskDistribution = taskDistribution; }

    public java.util.List<UserEfficiency> getUserEfficiency() { return userEfficiency; }
    public void setUserEfficiency(java.util.List<UserEfficiency> userEfficiency) { this.userEfficiency = userEfficiency; }

    public static class TaskStats {
        private long total;
        private long completed;
        private double completionRate;
        private long overdue;

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }

        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }

        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

        public long getOverdue() { return overdue; }
        public void setOverdue(long overdue) { this.overdue = overdue; }
    }

    public static class TimeStats {
        private BigDecimal totalEstimated;
        private BigDecimal totalActual;

        public BigDecimal getTotalEstimated() { return totalEstimated; }
        public void setTotalEstimated(BigDecimal totalEstimated) { this.totalEstimated = totalEstimated; }

        public BigDecimal getTotalActual() { return totalActual; }
        public void setTotalActual(BigDecimal totalActual) { this.totalActual = totalActual; }
    }

    public static class UserEfficiency {
        private String userName;
        private long assignedTasks;
        private long completedTasks;
        private BigDecimal totalEstimatedHours;
        private BigDecimal totalActualHours;
        private Double efficiencyRatio; // (Est / Act) * 100

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public long getAssignedTasks() { return assignedTasks; }
        public void setAssignedTasks(long assignedTasks) { this.assignedTasks = assignedTasks; }

        public long getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }

        public BigDecimal getTotalEstimatedHours() { return totalEstimatedHours; }
        public void setTotalEstimatedHours(BigDecimal totalEstimatedHours) { this.totalEstimatedHours = totalEstimatedHours; }

        public BigDecimal getTotalActualHours() { return totalActualHours; }
        public void setTotalActualHours(BigDecimal totalActualHours) { this.totalActualHours = totalActualHours; }

        public Double getEfficiencyRatio() { return efficiencyRatio; }
        public void setEfficiencyRatio(Double efficiencyRatio) { this.efficiencyRatio = efficiencyRatio; }
    }
}
