package com.dnk.service;

import com.dnk.entity.Lesson;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonService {
    List<Lesson> getLessonsByScheduleDayId(Long scheduleDayId);
    List<Lesson> findByDayNameAndStudentAndEvenWeek(String dayName, Long studentId, Boolean isEvenWeek);
    List<Lesson> findByWeekdaysAndStudentAndEvenWeek(Long studentId, Boolean isEvenWeek, List<String> weekday);
}
