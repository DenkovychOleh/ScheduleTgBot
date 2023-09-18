package com.dnk.service;

import com.dnk.entity.Lesson;

import java.util.List;

public interface LessonService {
    List<Lesson> getLessonsByScheduleDayId(Long scheduleDayId);
}
