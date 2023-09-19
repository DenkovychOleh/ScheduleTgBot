package com.dnk.service.impl;

import com.dnk.dao.ScheduleDayDAO;
import com.dnk.exception.ScheduleException;
import com.dnk.service.ScheduleDayService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
}
