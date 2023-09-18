package com.dnk.service.impl;

import com.dnk.dao.ScheduleDAO;
import com.dnk.entity.Schedule;
import com.dnk.exception.ScheduleException;
import com.dnk.service.ScheduleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleDAO scheduleDAO;

    @Override
    public List<Schedule> getSchedulesForStudentById(Long studentId) {
        return scheduleDAO.findByStudentId(studentId)
                .filter(lessons -> !lessons.isEmpty())
                .orElseThrow(() -> new ScheduleException("Помилка отримання розкладу для даного студента."));
    }
}
