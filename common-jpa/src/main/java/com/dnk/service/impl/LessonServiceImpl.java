package com.dnk.service.impl;

import com.dnk.dao.LessonDAO;
import com.dnk.entity.Lesson;
import com.dnk.exception.ScheduleException;
import com.dnk.service.LessonService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class LessonServiceImpl implements LessonService {
    private final LessonDAO lessonDAO;

    @Override
    public List<Lesson> getLessonsByScheduleDayId(Long scheduleDayId) {
        return lessonDAO.findByScheduleDay_Id(scheduleDayId)
                .filter(lessons -> !lessons.isEmpty())
                .orElseThrow(() -> new ScheduleException("Помилка отримання предметів за даною датою."));
    }

    @Override
    public List<Lesson> findByDayNameAndStudentAndEvenWeek(String dayName, Long studentId, Boolean isEvenWeek) {
        return lessonDAO.findByDayNameAndStudentAndEvenWeek(dayName, studentId, isEvenWeek)
                .filter(lessons -> !lessons.isEmpty())
                .orElseThrow(() -> new ScheduleException("Розклад за " + dayName + "відстуній"));
    }

    @Override
    public List<Lesson> findByWeekdaysAndStudentAndEvenWeek(Long studentId, Boolean isEvenWeek, List<String> weekday) {
        return lessonDAO.findByWeekdaysAndStudentAndEvenWeek(studentId, isEvenWeek, weekday)
                .filter(lessons -> !lessons.isEmpty())
                .orElseThrow(() -> new ScheduleException("Розклад на цей тиждень відстуній"));
    }

}
