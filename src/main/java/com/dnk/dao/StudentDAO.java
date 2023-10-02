package com.dnk.dao;

import com.dnk.entity.AppUser;
import com.dnk.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentDAO extends JpaRepository<Student, Long> {
    Optional<Student> findByAppUser(AppUser appUser);

    @Query("SELECT stu FROM Student stu JOIN stu.schedules s JOIN s.scheduleDay sd " +
            "WHERE sd.isEvenWeek = :isEvenWeek AND sd.dayName = :weekday")
    Optional<List<Student>> findScheduleByDay(
            @Param("weekday") String weekday,
            @Param("isEvenWeek") Boolean isEvenWeek
    );

    @Query("SELECT s FROM Student s WHERE s.appUser = null")
    Optional<List<Student>> findStudentsByWithoutAppUser();

    boolean existsById(long id);

    Optional<Student> findById (long id);
}
