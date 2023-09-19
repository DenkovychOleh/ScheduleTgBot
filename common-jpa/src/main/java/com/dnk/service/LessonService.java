package com.dnk.service;

import com.dnk.entity.Lesson;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonService {
    List<Lesson> findByDayNameAndStudentAndEvenWeek(Long studentId, String dayName, Boolean isEvenWeek);
    List<Lesson> findByWeekdaysAndStudentAndEvenWeek(Long studentId, List<String> weekday, Boolean isEvenWeek);
}
