package com.dnk.dao;

import com.dnk.entity.Schedule;
import com.dnk.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleDAO extends JpaRepository<Schedule, Long> {
}
