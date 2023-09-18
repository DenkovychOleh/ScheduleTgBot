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
        return lessonDAO.findByScheduleDay_Id(scheduleDayId).orElseThrow(() -> new ScheduleException("Помилка отримання предметів за даною датою."));
    }
}
