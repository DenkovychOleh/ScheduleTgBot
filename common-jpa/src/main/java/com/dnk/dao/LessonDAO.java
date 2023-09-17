package com.dnk.dao;

import com.dnk.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonDAO extends JpaRepository<Lesson, Long> {
   Optional<List<Lesson>> findByScheduleDay_Id(Long scheduleDayId);
}
