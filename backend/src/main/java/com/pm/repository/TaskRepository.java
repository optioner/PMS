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
}
