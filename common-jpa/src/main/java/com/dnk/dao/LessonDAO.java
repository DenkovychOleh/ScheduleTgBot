package com.dnk.dao;

import com.dnk.entity.Lesson;
import com.dnk.entity.ScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonDAO extends JpaRepository<Lesson, Long> {
   Optional<List<Lesson>> findByScheduleDay_Id(Long scheduleDayId);

   @Query("SELECT l FROM Lesson l JOIN l.scheduleDay sd JOIN sd.schedules s JOIN s.student st " +
           "WHERE sd.dayName = :dayName AND st.id = :studentId AND sd.isEvenWeek = :isEvenWeek")
   Optional<List<Lesson>> findByDayNameAndStudentAndEvenWeek(
           @Param("dayName") String dayName,
           @Param("studentId") Long studentId,
           @Param("isEvenWeek") Boolean isEvenWeek
   );

   @Query("SELECT l FROM Lesson l JOIN l.scheduleDay sd JOIN sd.schedules s JOIN s.student st " +
           "WHERE sd.dayName IN (:weekday) " +
           "AND st.id = :studentId AND sd.isEvenWeek = :isEvenWeek")
   Optional<List<Lesson>> findByWeekdaysAndStudentAndEvenWeek(
           @Param("studentId") Long studentId,
           @Param("isEvenWeek") Boolean isEvenWeek,
           @Param("weekday") List<String> weekday
   );

}
