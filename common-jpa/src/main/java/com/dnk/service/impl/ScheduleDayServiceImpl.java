package com.dnk.service.impl;

import com.dnk.dao.ScheduleDayDAO;
import com.dnk.entity.ScheduleDay;
import com.dnk.exception.ScheduleException;
import com.dnk.service.ScheduleDayService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ScheduleDayServiceImpl implements ScheduleDayService {
    private final ScheduleDayDAO scheduleDayDAO;

    @Override
    public List<ScheduleDay> getScheduleDaysByScheduleId(Long scheduleId) {
        return scheduleDayDAO.findBySchedules_Id(scheduleId).orElseThrow(() -> new ScheduleException("Помилка отримання розкладу для даного розкладу."));
    }
}
