package com.dnk.service.impl;

import com.dnk.dao.StudentDAO;
import com.dnk.entity.AppUser;
import com.dnk.entity.Student;
import com.dnk.exception.ScheduleException;
import com.dnk.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class StudentServiceImpl implements StudentService {
    private final StudentDAO studentDAO;
    @Override
    public Student findByAppUser(AppUser appUser) {
        return studentDAO.findByAppUser(appUser).orElseThrow(() -> new ScheduleException("Помилка знаходження студента за даним користувачем, щоб привʼязати аккаунта напишить - @oleh_denkovych"));
    }
}
