package com.dnk.dao;

import com.dnk.entity.ScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleDayDAO extends JpaRepository<ScheduleDay, Long> {
}
