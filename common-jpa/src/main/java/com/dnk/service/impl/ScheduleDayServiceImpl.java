package com.dnk.service.impl;

import com.dnk.dao.ScheduleDayDAO;
import com.dnk.entity.ScheduleDay;
import com.dnk.exception.ScheduleException;
import com.dnk.service.ScheduleDayService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ScheduleDayServiceImpl implements ScheduleDayService {
    private final ScheduleDayDAO scheduleDayDAO;

    @Override
    public List<ScheduleDay> getScheduleDaysByScheduleId(Long scheduleId) {
        return scheduleDayDAO.findBySchedules_Id(scheduleId)
                .filter(lessons -> !lessons.isEmpty())
                .orElseThrow(() -> new ScheduleException("[getScheduleDaysByScheduleId()] Розклад відсутній"));
    }

    @Override
    public List<ScheduleDay> findBySchedules_StudentIdAndIsEvenWeek(Long studentId, Boolean isEvenWeek) {
        return scheduleDayDAO.findBySchedules_StudentIdAndIsEvenWeek(studentId,isEvenWeek)
                .filter(lessons -> !lessons.isEmpty())
                .orElseThrow(() -> new ScheduleException("Error findBySchedules_StudentIdAndIsEvenWeek."));
    }
}
