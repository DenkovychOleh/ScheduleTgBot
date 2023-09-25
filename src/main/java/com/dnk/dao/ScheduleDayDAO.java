package com.dnk.dao;

import com.dnk.entity.ScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScheduleDayDAO extends JpaRepository<ScheduleDay, Long> {
    @Query("SELECT s.dayName FROM ScheduleDay s ORDER BY s.id ASC")
    Optional<List<String>> findDistinctDayNames();
    Optional<ScheduleDay> findByDayNameAndIsEvenWeek(String dayName, Boolean isEvenWeek);

    Optional<List<ScheduleDay>> findByIsEvenWeek(Boolean isEvenWeek);
}
