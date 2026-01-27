package com.pm.model.dto.response;

import java.math.BigDecimal;

public class DashboardStatsResponse {
    private TaskStats taskStats;
    private TimeStats timeStats;

    public TaskStats getTaskStats() { return taskStats; }
    public void setTaskStats(TaskStats taskStats) { this.taskStats = taskStats; }

    public TimeStats getTimeStats() { return timeStats; }
    public void setTimeStats(TimeStats timeStats) { this.timeStats = timeStats; }
    
    public static class TaskStats {
        private long todo;
        private long inProgress;
        private long review;
        private long done;
        private long overdue;

        public long getTodo() { return todo; }
        public void setTodo(long todo) { this.todo = todo; }

        public long getInProgress() { return inProgress; }
        public void setInProgress(long inProgress) { this.inProgress = inProgress; }

        public long getReview() { return review; }
        public void setReview(long review) { this.review = review; }

        public long getDone() { return done; }
        public void setDone(long done) { this.done = done; }

        public long getOverdue() { return overdue; }
        public void setOverdue(long overdue) { this.overdue = overdue; }
    }

    public static class TimeStats {
        private BigDecimal weekTotal;
        private BigDecimal monthTotal;

        public BigDecimal getWeekTotal() { return weekTotal; }
        public void setWeekTotal(BigDecimal weekTotal) { this.weekTotal = weekTotal; }

        public BigDecimal getMonthTotal() { return monthTotal; }
        public void setMonthTotal(BigDecimal monthTotal) { this.monthTotal = monthTotal; }
    }
}
