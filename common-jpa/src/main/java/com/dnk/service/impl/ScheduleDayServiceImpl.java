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
                .orElseThrow(() -> new ScheduleException("fds"));
    }

//    @Override
//    public List<String> findDistinctDayNamesOrderedById() {
//        return scheduleDayDAO.findDistinctDayNamesOrderedById()
//                .filter(strings -> !strings.isEmpty())
//                .orElseThrow(() -> new ScheduleException("Розклад на цей тиждень не знайдено"));
//    }
}
