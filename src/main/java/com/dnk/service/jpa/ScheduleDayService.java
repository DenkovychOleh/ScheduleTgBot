package com.dnk.service.jpa;

import com.dnk.entity.ScheduleDay;

import java.util.List;

public interface ScheduleDayService {
    List<String> findDistinctDayNames();
    ScheduleDay findByDayNameAndIsEvenWeek(String dayName, Boolean isEvenWeek);
    List<ScheduleDay> findByIsEvenWeek(Boolean isEvenWeek);
}
