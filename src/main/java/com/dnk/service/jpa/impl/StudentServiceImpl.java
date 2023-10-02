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

    @Override
    public List<Student> findStudentsByWithoutAppUser() {
        return studentDAO.findStudentsByWithoutAppUser()
                .filter(students -> !students.isEmpty())
                .orElseThrow(() -> new ScheduleException("Студентів з пустими телеграми не знайдено"));
    }

    @Override
    public boolean existsById(long id) {
        return studentDAO.existsById(id);
    }

    @Override
    public Student findById(long id) {
        return studentDAO.findById(id)
                .orElseThrow(() -> new ScheduleException("Помилка студента за його Id"));

    }
    @Override
    public Student save(Student student) {
        return studentDAO.save(student);
    }
}
