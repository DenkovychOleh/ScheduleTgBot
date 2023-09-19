package com.dnk.service.impl;

import com.dnk.entity.AppUser;
import com.dnk.entity.Lesson;
import com.dnk.entity.Student;
import com.dnk.exception.ScheduleException;
import com.dnk.service.CommandService;
import com.dnk.service.LessonService;
import com.dnk.service.ScheduleDayService;
import com.dnk.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


@AllArgsConstructor
@Service
public class CommandServiceImpl implements CommandService {

    private final LessonService lessonService;

    private final StudentService studentService;

    private final ScheduleDayService scheduleDayService;


    @Override
    public String showStudentScheduleToday(AppUser appUser) {
        LocalDate today = LocalDate.now();
        return showStudentScheduleForDay(appUser, today);
    }

    @Override
    public String showStudentScheduleTomorrow(AppUser appUser) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return showStudentScheduleForDay(appUser, tomorrow);
    }

    @Override
    public String showStudentScheduleThisWeek(AppUser appUser) {
        return showStudentScheduleForWeek(appUser, false);
    }

    @Override
    public String showStudentScheduleNextWeek(AppUser appUser) {
        return showStudentScheduleForWeek(appUser, true);
    }

    @Override
    public String showScheduleThisWeek() {
        return showScheduleForWeek(false);
    }

    @Override
    public String showScheduleNextWeek() {
        return showScheduleForWeek(true);
    }

    public String help() {
        return "Cписок доступних команд:\n" +
                "/MyScheduleToday    - виводить твій розклад на сьогодні;\n" +
                "/MyScheduleTomorrow - виводить твій розклад на завтра;\n" +
                "/MyScheduleThisWeek - виводить твій розклад на цей тиждень;\n" +
                "/MyScheduleNextWeek - виводить твій розклад на наступний тиждень;\n" +
                "/ScheduleThisWeek   - виводить загальний розклад на цей тиждень;\n" +  //TODO Schedule for this week
                "/ScheduleNextWeek   - виводить загальний розклад на наступний тиждень;"; //TODO Schedule for next week
    }


    private String showScheduleForWeek(boolean isNextWeek) {
        try {
            List<String> weekdays = scheduleDayService.findDistinctDayNames();
            LocalDate startDate = isNextWeek ? getStartOfNextWeek() : getStartOfWeek();
            LocalDate endDate = getEndOfWeek(startDate);
            String weekPeriod = getWeekPeriod(startDate, endDate);
            boolean evenWeek = !isNextWeek;
            return buildScheduleResponses(weekdays, evenWeek, weekPeriod);
        } catch (ScheduleException exception) {
            return exception.getMessage();
        }
    }



    private String showStudentScheduleForDay(AppUser appUser, LocalDate date) {
        try {
            Student student = studentService.findByAppUser(appUser);
            Long studentId = student.getId();
            String dayName = formatDayName(date);
            String dateInfo = date.format(DateTimeFormatter.ofPattern("dd MMMM", new Locale("uk")));
            List<Lesson> todayLessons = lessonService
                    .findByDayNameAndStudentAndEvenWeek(studentId, dayName, isEvenWeek());
            return buildStudentScheduleResponse(todayLessons, student, dateInfo);
        } catch (ScheduleException exception) {
            return exception.getMessage();
        }
    }

    private String showStudentScheduleForWeek(AppUser appUser, boolean isNextWeek) {
        try {
            Student student = studentService.findByAppUser(appUser);
            Long studentId = student.getId();
            List<String> daysOfWeek = scheduleDayService.findDistinctDayNames();
            LocalDate startDate = isNextWeek ? getStartOfNextWeek() : getStartOfWeek();
            LocalDate endDate = getEndOfWeek(startDate);
            String weekPeriod = getWeekPeriod(startDate, endDate);
            boolean isEvenWeek = !isNextWeek;
            List<Lesson> lessonList = lessonService
                    .findByWeekdaysAndStudentAndEvenWeek(studentId, daysOfWeek, isEvenWeek);
            return buildStudentScheduleResponse(lessonList, student, weekPeriod);
        } catch (ScheduleException exception) {
            return exception.getMessage();
        }
    }

    private String buildStudentScheduleResponse(List<Lesson> lessons, Student student, String dateInfo) {
        StringBuilder scheduleStringBuilder = new StringBuilder();
        scheduleStringBuilder.append(student.getFirstName())
                .append(", ваш розклад:\n")
                .append("<b>").append(dateInfo).append("</b>")
                .append(":\n");
        String currentDay = "";

        for (Lesson lesson : lessons) {
            if (!lesson.getScheduleDay().getDayName().equals(currentDay)) {
                scheduleStringBuilder.append("\n<b>").append(lesson.getScheduleDay().getDayName()).append("</b>\n");
                currentDay = lesson.getScheduleDay().getDayName();
            }
            String lessonInfo = String.format(
                    "Предмет: %s\nВикладач: %s\nЧас: %s - %s\nКабінет: %d\n\n",
                    lesson.getTitle(),
                    lesson.getTeacher(),
                    lesson.getStartLesson(),
                    lesson.getEndLesson(),
                    lesson.getOffice()
            );
            scheduleStringBuilder.append(lessonInfo);
        }
        return scheduleStringBuilder.toString();
    }

    private String buildScheduleResponses(List<String> weekdays, Boolean isEvenWeek, String weekPeriod) {
        try {
            StringBuilder scheduleStringBuilder = new StringBuilder();
            scheduleStringBuilder
                    .append("\n")
                    .append("<b>").append(weekPeriod).append("</b>")
                    .append("\n");
            for (String weekday : weekdays) {
                scheduleStringBuilder.append("<b>").append(weekday).append("</b>").append(":\n");
                List<Student> studentList = studentService.findScheduleByDay(weekday, isEvenWeek);
                studentList.sort(Comparator.comparing(Student::getLastName));
                for (Student student : studentList) {
                    scheduleStringBuilder.append(student.getLastName())
                            .append(" ")
                            .append(student.getFirstName())
                            .append("\n");

                }
                scheduleStringBuilder.append("\n");

            }
            return scheduleStringBuilder.toString();
        } catch (ScheduleException exception) {
            return exception.getMessage();
        }
    }

    private String getWeekPeriod(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM", new Locale("uk"));
        String weekStartStr = startDate.format(formatter);
        String weekEndStr = endDate.format(formatter);
        return String.format("тиждень з %s по %s", weekStartStr, weekEndStr);
    }

    private LocalDate getStartOfWeek() {
        LocalDate currentDate = LocalDate.now();
        return formatWeek(currentDate);
    }

    private LocalDate getStartOfNextWeek() {
        LocalDate nextWeekDate = LocalDate.now().plusWeeks(1);
        return formatWeek(nextWeekDate);
    }

    private LocalDate formatWeek(LocalDate date) {
        DayOfWeek currentDayOfWeek = date.getDayOfWeek();
        int daysUntilMonday = currentDayOfWeek.getValue() - DayOfWeek.MONDAY.getValue();
        return date.minusDays(daysUntilMonday);
    }

    private LocalDate getEndOfWeek(LocalDate startOfWeek) {
        return startOfWeek.plusDays(4);
    }

    private String formatDayName(LocalDate date) {
        Locale ukrainianLocale = new Locale("uk", "UA");
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, ukrainianLocale);
        return dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
    }

    private boolean isEvenWeek() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Kiev"));
        return today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()) % 2 == 0;
    }
}
