package com.dnk.service.impl;

import com.dnk.entity.AppUser;
import com.dnk.entity.Lesson;
import com.dnk.entity.Student;
import com.dnk.exception.ScheduleException;
import com.dnk.service.CommandService;
import com.dnk.service.LessonService;
import com.dnk.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@AllArgsConstructor
@Service
public class CommandServiceImpl implements CommandService {
    private final LessonService lessonService;
    private final StudentService studentService;

    private String buildScheduleResponse(List<Lesson> lessons, Student student, String dateInfo) {
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


    private String getWeekPeriod(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM", new Locale("uk"));

        String weekStartStr = startDate.format(formatter);
        String weekEndStr = endDate.format(formatter);

        return String.format("тиждень з %s по %s", weekStartStr, weekEndStr);
    }

    private LocalDate formatWeek(LocalDate date) {
        DayOfWeek currentDayOfWeek = date.getDayOfWeek();
        int daysUntilMonday = currentDayOfWeek.getValue() - DayOfWeek.MONDAY.getValue();
        return date.minusDays(daysUntilMonday);
    }


    private LocalDate getStartOfWeek() {
        LocalDate currentDate = LocalDate.now();
        return formatWeek(currentDate);
    }

    private LocalDate getStartOfNextWeek() {
        LocalDate nextWeekDate = LocalDate.now().plusWeeks(1);
        return formatWeek(nextWeekDate);
    }

    private LocalDate getEndOfWeek(LocalDate startOfWeek) {
        return startOfWeek.plusDays(4);
    }


    private String formatDayName(LocalDate date) {
        Locale ukrainianLocale = new Locale("uk", "UA");
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, ukrainianLocale);
        return dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
    }

    private String getTomorrowDay() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return formatDayName(tomorrow);
    }

    private String getCurrentDay() {
        LocalDate today = LocalDate.now();
        return formatDayName(today);
    }

    public List<String> getDaysOfWeek() {
        List<String> daysOfWeek = new ArrayList<>();

        Locale ukrainianLocale = new Locale("uk", "UA");

        for (DayOfWeek day : DayOfWeek.values()) {
            String dayName = day.getDisplayName(TextStyle.FULL, ukrainianLocale);
            dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
            dayName = dayName.replace("'", "");
            daysOfWeek.add(dayName);
        }

        return daysOfWeek;
    }

    private boolean isEvenWeek() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Kiev"));
         return today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()) % 2 == 0;
    }

    public String help() {
        return "Cписок доступних команд:\n" +
                "/MyScheduleToday    - виводить твій розклад на сьогодні;\n" +
                "/MyScheduleThisWeek - виводить твій розклад на цей тиждень;\n" +
                "/MyScheduleTomorrow - виводить твій розклад на завтра;\n" + //TODO student's schedule for tomorrow
                "/MyScheduleNextWeek - виводить твій розклад на наступний тиждень;\n" + //TODO student's schedule for next week
                "/ScheduleThisWeek   - виводить загальний розклад на цей тиждень;\n" +  //TODO Schedule for this week
                "/ScheduleNextWeek   - виводить загальний розклад на наступний тиждень;"; //TODO Schedule for next week
    }

    @Override
    public String showStudentScheduleToday(AppUser appUser) {
        return showStudentScheduleForDay(appUser, getCurrentDay());
    }

    @Override
    public String showStudentScheduleTomorrow(AppUser appUser) {
        return showStudentScheduleForDay(appUser, getTomorrowDay());
    }

    private String showStudentScheduleForDay(AppUser appUser, String dayName) {
        try {
            Student student = studentService.findByAppUser(appUser);
            Long studentId = student.getId();
            LocalDate currentDate = LocalDate.now();
            String dateInfo = currentDate.format(DateTimeFormatter.ofPattern("dd MMMM", new Locale("uk")));

            List<Lesson> todayLessons = lessonService
                    .findByDayNameAndStudentAndEvenWeek(studentId, dayName, isEvenWeek());

            return buildScheduleResponse(todayLessons, student, dateInfo);
        } catch (ScheduleException exception) {
            return exception.getMessage();
        }
    }

    @Override
    public String showStudentScheduleThisWeek(AppUser appUser) {
        boolean isEvenCurrentWeek = isEvenWeek();
        return showStudentScheduleForWeek(appUser, isEvenCurrentWeek);
    }

    @Override
    public String showStudentScheduleNextWeek(AppUser appUser) {
        boolean isEvenNextWeek = !isEvenWeek();
        return showStudentScheduleForWeek(appUser, isEvenNextWeek);
    }

    private String showStudentScheduleForWeek(AppUser appUser, boolean isNextWeek) {
        try {
            Student student = studentService.findByAppUser(appUser);
            Long studentId = student.getId();
            List<String> daysOfWeek = getDaysOfWeek();
            LocalDate startDate = isNextWeek ? getStartOfNextWeek() : getStartOfWeek();
            LocalDate endDate = getEndOfWeek(startDate);

            List<Lesson> lessonList = lessonService
                    .findByWeekdaysAndStudentAndEvenWeek(studentId, daysOfWeek, isNextWeek);

            String weekPeriod = getWeekPeriod(startDate, endDate);

            return buildScheduleResponse(lessonList, student, weekPeriod);
        } catch (ScheduleException exception) {
            return exception.getMessage();
        }
    }



    @Override
    public String showScheduleThisWeek() {
        return "showScheduleThisWeek";
    }

    @Override
    public String showScheduleNextWeek() {
        return "showScheduleNextWeek";
    }
}
