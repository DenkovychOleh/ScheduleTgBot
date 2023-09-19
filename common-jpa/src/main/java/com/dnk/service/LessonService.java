package com.dnk.service;

import com.dnk.entity.Lesson;

import java.util.List;

public interface LessonService {
    List<Lesson> findByDayNameAndStudentAndEvenWeek(Long studentId, String dayName, Boolean isEvenWeek);
    List<Lesson> findByWeekdaysAndStudentAndEvenWeek(Long studentId, List<String> weekday, Boolean isEvenWeek);
}
