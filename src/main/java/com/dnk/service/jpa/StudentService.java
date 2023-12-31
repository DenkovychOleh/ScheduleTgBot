package com.dnk.service.jpa;

import com.dnk.entity.AppUser;
import com.dnk.entity.Student;

import java.util.List;

public interface StudentService {
    Student findByAppUser(AppUser appUser);

    List<Student> findScheduleByDay(String day, Boolean isEvenWeek);

    List<Student> findStudentsByWithoutAppUser();

    boolean existsById(long id);

    Student findById (long id);

    Student save(Student student);
}
