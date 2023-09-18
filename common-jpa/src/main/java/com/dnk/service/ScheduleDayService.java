package com.dnk.service;

import com.dnk.entity.ScheduleDay;

import java.util.List;

public interface ScheduleDayService {
    List<ScheduleDay> getScheduleDaysByScheduleId(Long scheduleId);
}
