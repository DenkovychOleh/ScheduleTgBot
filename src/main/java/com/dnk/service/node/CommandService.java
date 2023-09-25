package com.dnk.service.node;

import com.dnk.entity.AppUser;

public interface CommandService {
    String help();
    String showScheduleToday(AppUser appUser);
    String showMyScheduleToday(AppUser appUser);
    String showScheduleTomorrow(AppUser appUser);
    String showMyScheduleTomorrow(AppUser appUser);
    String showScheduleThisWeek(AppUser appUser);
    String showMyScheduleThisWeek(AppUser appUser);
    String showScheduleNextWeek(AppUser appUser);
    String showMyScheduleNextWeek(AppUser appUser);
    String showStudentsThisWeek();
    String showStudentsNextWeek();
    String setNotificationOn(AppUser appUser);
    String setNotificationOff(AppUser appUser);
}
