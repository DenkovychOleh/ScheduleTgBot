package com.dnk.service;

import com.dnk.dao.LessonDAO;
import com.dnk.dao.ScheduleDAO;
import com.dnk.dao.ScheduleDayDAO;
import com.dnk.dao.StudentDAO;
import com.dnk.entity.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
public class ScheduleService {
    private final StudentDAO studentDAO;
    private final ScheduleDAO scheduleDAO;
    private final ScheduleDayDAO scheduleDayDAO;
    private final LessonDAO lessonDAO;

    public ScheduleService(StudentDAO studentDAO, ScheduleDAO scheduleDAO, ScheduleDayDAO scheduleDayDAO, LessonDAO lessonDAO) {
        this.studentDAO = studentDAO;
        this.scheduleDAO = scheduleDAO;
        this.scheduleDayDAO = scheduleDayDAO;
        this.lessonDAO = lessonDAO;
    }

    public Optional<List<Lesson>> getLessonsForStudentById(Long studentId) {
        Optional<List<Schedule>> optionalScheduleList = scheduleDAO.findByStudentId(studentId);
        if (optionalScheduleList.isPresent()) {
            List<Schedule> schedules = optionalScheduleList.get();
            List<Lesson> lessons = new ArrayList<>();

            for (Schedule schedule : schedules) {
                Optional<List<ScheduleDay>> optionalScheduleDayList = scheduleDayDAO.findBySchedules_Id(schedule.getId());
                if(optionalScheduleDayList.isPresent()) {
                    List<ScheduleDay> scheduleDays = optionalScheduleDayList.get();
                    for (ScheduleDay scheduleDay : scheduleDays) {
                        Optional<List<Lesson>> optionalLessonList = lessonDAO.findByScheduleDay_Id(scheduleDay.getId());
                        if(optionalLessonList.isPresent()) {
                            List<Lesson> dayLessons = optionalLessonList.get();
                            lessons.addAll(dayLessons);
                        } else {
                            return Optional.empty();
                        }
                    }
                } else {
                    return Optional.empty();
                }
            }
            return Optional.of(lessons);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Student> findByAppUser(AppUser appUser) {
        return studentDAO.findByAppUser(appUser);
    }
}
