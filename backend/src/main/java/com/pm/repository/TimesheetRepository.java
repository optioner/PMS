package com.pm.repository;

import com.pm.model.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    List<Timesheet> findByUserId(Long userId);
    List<Timesheet> findByTaskId(Long taskId);

    @Query("SELECT SUM(t.hours) FROM Timesheet t WHERE t.user.id = :userId AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumHoursByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
