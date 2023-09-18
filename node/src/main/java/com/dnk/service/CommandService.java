package com.dnk.service;

import com.dnk.entity.AppUser;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandService {
    String help();
    String showStudentScheduleToday(AppUser appUser);
    String showStudentScheduleTomorrow(AppUser appUser);
    String showStudentScheduleThisWeek(AppUser appUser);
    String showStudentScheduleNextWeek(AppUser appUser);
    String showScheduleThisWeek();
    String showScheduleNextWeek();
}
