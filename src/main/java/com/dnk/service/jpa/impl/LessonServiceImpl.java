package com.dnk.service.jpa.impl;

import com.dnk.dao.LessonDAO;
import com.dnk.entity.Lesson;
import com.dnk.entity.ScheduleDay;
import com.dnk.exception.ScheduleException;
import com.dnk.service.jpa.LessonService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class LessonServiceImpl implements LessonService {

    private final LessonDAO lessonDAO;

    @Override
    public List<Lesson> findByDayNameAndStudentAndEvenWeek(Long studentId, String dayName, Boolean isEvenWeek) {
        return lessonDAO.findByDayNameAndStudentAndEvenWeek(studentId, dayName, isEvenWeek)
                .filter(lessons -> !lessons.isEmpty())
                .orElseThrow(() -> new ScheduleException("Цього тижня, " + dayName + " явка на пари не обовʼязкова"));
    }

    @Override
    public List<Lesson> findByWeekdaysAndStudentAndEvenWeek(Long studentId, List<String> weekday, Boolean isEvenWeek) {
        return lessonDAO.findByWeekdaysAndStudentAndEvenWeek(studentId, isEvenWeek, weekday)
                .filter(lessons -> !lessons.isEmpty())
                .orElseThrow(() -> new ScheduleException("Розклад на цей тиждень відстуній"));
    }

    @Override
    public List<Lesson> findByDayNameAndEvenWeek(ScheduleDay scheduleDay) {
        return lessonDAO.findByDayNameAndEvenWeek(scheduleDay)
                .filter(lessons -> !lessons.isEmpty())
                .orElseThrow(() -> new ScheduleException("Розклад відстуній"));
    }

    @Override
    public List<Lesson> findByWeekdaysAndEvenWeek(List<String> weekday, Boolean isEvenWeek) {
        return lessonDAO.findByWeekdaysAndEvenWeek(weekday, isEvenWeek)
                .filter(lessons -> !lessons.isEmpty())
                .orElseThrow(() -> new ScheduleException("Розклад відстуній"));
    }

    @Override
    public List<Lesson> findByScheduleDay(List<ScheduleDay> scheduleDays) {
        return lessonDAO.findByScheduleDay(scheduleDays)
                .filter(lessons -> !lessons.isEmpty())
                .orElseThrow(() -> new ScheduleException("Розклад відстуній"));
    }
}
