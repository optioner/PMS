package com.pm.repository;

import com.pm.model.entity.Task;
import com.pm.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    
    long countByAssigneeIdAndStatus(Long assigneeId, TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :userId AND t.dueDate < CURRENT_TIMESTAMP AND t.status <> 'DONE' AND t.status <> 'CANCELLED'")
    long countOverdueTasks(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :userId AND t.id <> :excludeTaskId AND t.status <> 'DONE' AND t.status <> 'CANCELLED' AND " +
           "(t.plannedStartDate IS NOT NULL AND t.dueDate IS NOT NULL) AND " +
           "(t.plannedStartDate < :end AND t.dueDate > :start)")
    List<Task> findOverlappingTasks(@Param("userId") Long userId, @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end, @Param("excludeTaskId") Long excludeTaskId);
}
