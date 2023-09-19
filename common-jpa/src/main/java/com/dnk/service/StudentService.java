package com.dnk.service;

import com.dnk.entity.AppUser;
import com.dnk.entity.Student;

import java.util.List;

public interface StudentService {
    Student findByAppUser(AppUser appUser);

    List<Student> findScheduleByDay(String day, Boolean isEvenWeek);

}
