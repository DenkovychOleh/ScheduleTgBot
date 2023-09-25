package com.dnk.service.jpa.impl;

import com.dnk.dao.ScheduleDayDAO;
import com.dnk.entity.ScheduleDay;
import com.dnk.exception.ScheduleException;
import com.dnk.service.jpa.ScheduleDayService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ScheduleDayServiceImpl implements ScheduleDayService {

    ScheduleDayDAO scheduleDayDAO;

    @Override
    public List<String> findDistinctDayNames() {
        return scheduleDayDAO.findDistinctDayNames()
                .filter(strings -> !strings.isEmpty())
                .map(strings -> strings.subList(0, 5))
                .orElseThrow(() -> new ScheduleException("Дні не знайдено"));

    }

    @Override
    public ScheduleDay findByDayNameAndIsEvenWeek(String dayName, Boolean isEvenWeek) {
        return scheduleDayDAO.findByDayNameAndIsEvenWeek(dayName,isEvenWeek).orElseThrow(() -> new ScheduleException("День не знайдено"));
    }

    @Override
    public List<ScheduleDay> findByIsEvenWeek(Boolean isEvenWeek) {
        return scheduleDayDAO.findByIsEvenWeek(isEvenWeek)
                .filter(strings -> !strings.isEmpty())
                .orElseThrow(() -> new ScheduleException("Дні не знайдено"));
    }
}
