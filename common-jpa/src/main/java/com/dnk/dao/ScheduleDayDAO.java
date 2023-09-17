package com.dnk.dao;

import com.dnk.entity.ScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Schedules;

import java.util.List;
import java.util.Optional;

public interface ScheduleDayDAO extends JpaRepository<ScheduleDay, Long> {
    Optional<List<ScheduleDay>> findBySchedules_Id(Long scheduleId);
}
