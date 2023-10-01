package com.dnk.service.jpa.impl;

import com.dnk.dao.StudentDAO;
import com.dnk.entity.AppUser;
import com.dnk.entity.Student;
import com.dnk.exception.ScheduleException;
import com.dnk.service.jpa.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class StudentServiceImpl implements StudentService {

    private final StudentDAO studentDAO;
    @Override
    public Student findByAppUser(AppUser appUser) {
        return studentDAO.findByAppUser(appUser)
                .orElseThrow(() -> new ScheduleException("Помилка знаходження студента за даним користувачем, щоб привʼязати аккаунта напишить - @oleh_denkovych"));
    }

    @Override
    public List<Student> findScheduleByDay(String day, Boolean isEvenWeek) {
        return studentDAO.findScheduleByDay(day,isEvenWeek)
                .filter(students -> !students.isEmpty())
                .orElseThrow(() -> new ScheduleException("Розклад на цей день не знайдено"));
    }
}