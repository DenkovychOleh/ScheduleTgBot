package com.dnk.dao;

import com.dnk.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleDAO extends JpaRepository<Schedule, Long> {
}
