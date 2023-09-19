package com.dnk.dao;

import com.dnk.entity.ScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ScheduleDayDAO extends JpaRepository<ScheduleDay, Long> {
    @Query("SELECT DISTINCT s.dayName FROM ScheduleDay s")
    Optional<List<String>> findDistinctDayNames();
}
