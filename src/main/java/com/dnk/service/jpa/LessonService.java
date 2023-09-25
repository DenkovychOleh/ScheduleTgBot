package com.dnk.service.jpa;

import com.dnk.entity.Lesson;
import com.dnk.entity.ScheduleDay;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonService {
    List<Lesson> findByDayNameAndStudentAndEvenWeek(Long studentId, String dayName, Boolean isEvenWeek);
    List<Lesson> findByWeekdaysAndStudentAndEvenWeek(Long studentId, List<String> weekday, Boolean isEvenWeek);
    List<Lesson> findByDayNameAndEvenWeek(ScheduleDay scheduleDay);
    List<Lesson> findByWeekdaysAndEvenWeek(List<String> weekday, Boolean isEvenWeek);
    List<Lesson> findByScheduleDay(List<ScheduleDay> scheduleDays);
}
