package com.dnk.service.node.impl;

import com.dnk.entity.AppUser;
import com.dnk.entity.Lesson;
import com.dnk.entity.ScheduleDay;
import com.dnk.entity.Student;
import com.dnk.exception.ScheduleException;
import com.dnk.service.jpa.AppUserService;
import com.dnk.service.jpa.LessonService;
import com.dnk.service.jpa.ScheduleDayService;
import com.dnk.service.jpa.StudentService;
import com.dnk.service.node.CommandService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;
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

@Log4j
@AllArgsConstructor
@Service
public class CommandServiceImpl implements CommandService {

    private final LessonService lessonService;

    private final StudentService studentService;

    private final ScheduleDayService scheduleDayService;

    private final AppUserService appUserService;


    @Override
    public String showScheduleToday(AppUser appUser) {
        LocalDate today = LocalDate.now();
        return getStudentSchedule(appUser, today, false);
    }

    @Override
    public String showMyScheduleToday(AppUser appUser) {
        LocalDate today = LocalDate.now();
        return getStudentSchedule(appUser, today, true);
    }

    @Override
    public String showScheduleTomorrow(AppUser appUser) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return getStudentSchedule(appUser, tomorrow, false);
    }

    @Override
    public String showMyScheduleTomorrow(AppUser appUser) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return getStudentSchedule(appUser, tomorrow, true);
    }

    @Override
    public String showScheduleThisWeek(AppUser appUser) {
        return showScheduleForWeek(appUser, false, false);
    }

    @Override
    public String showMyScheduleThisWeek(AppUser appUser) {
        return showScheduleForWeek(appUser, false, true);
    }

    @Override
    public String showScheduleNextWeek(AppUser appUser) {
        return showScheduleForWeek(appUser, true, false);
    }

    @Override
    public String showMyScheduleNextWeek(AppUser appUser) {
        return showScheduleForWeek(appUser, true, true);
    }

    @Override
    public String showStudentsThisWeek() {
        return showScheduleForWeek(false);
    }

    @Override
    public String showStudentsNextWeek() {
        return showScheduleForWeek(true);
    }

    @Override
    public String setNotificationOn(AppUser appUser) {
        return setNotification(appUser, AppUser.NotificationStatus.ON);
    }

    @Override
    public String setNotificationOff(AppUser appUser) {
        return setNotification(appUser, AppUser.NotificationStatus.OFF);
    }

    public String setNotification(AppUser appUser, AppUser.NotificationStatus notificationStatus) {
        try {
            AppUser user = appUserService.findByTelegramUserId(appUser.getTelegramUserId());
            if (user.getNotificationStatus().equals(notificationStatus)) {
                return "Ваш статус відповідає обраному";
            } else {
                user.setNotificationStatus(notificationStatus);
                appUserService.save(user);
                return "Налаштування сповіщень змінено.";
            }
        } catch (ScheduleException exception) {
            return exception.getMessage();
        }
    }


    public String help() {
        return "Cписок доступних команд можна побачити в меню керування.\n" +
                "\n @oleh_denkovych - телеграм для зворотнього зв'язку.";
    }
    private String getStudentSchedule(AppUser appUser, LocalDate date, boolean isDetailed) {
        try {
            Student student = studentService.findByAppUser(appUser);
            String dayName = formatDayName(date);
            Long studentId = student.getId();
            Boolean isEvenWeek = isEvenWeek();
            String dateInfo = date.format(DateTimeFormatter.ofPattern("dd MMMM", new Locale("uk")));

            List<Lesson> todayLessons;

            if (isDetailed) {
                todayLessons = lessonService.findByDayNameAndStudentAndEvenWeek(studentId, dayName, isEvenWeek);
            } else {
                ScheduleDay scheduleDay = scheduleDayService.findByDayNameAndIsEvenWeek(dayName, isEvenWeek);
                todayLessons = lessonService.findByDayNameAndEvenWeek(scheduleDay);
            }

            return buildStudentScheduleResponse(todayLessons, student, dateInfo);
        } catch (ScheduleException exception) {
            log.error(exception.getMessage());
            return exception.getMessage();
        }
    }

    private String showScheduleForWeek(boolean isNextWeek) {
        try {
            List<String> weekdays = scheduleDayService.findDistinctDayNames();
            LocalDate startDate = isNextWeek ? getStartOfNextWeek() : getStartOfWeek();
            LocalDate endDate = getEndOfWeek(startDate);
            String weekPeriod = getWeekPeriod(startDate, endDate);
            Boolean isEvenWeek = isNextWeek != isEvenWeek();
            return buildScheduleResponses(weekdays, isEvenWeek, weekPeriod);
        } catch (ScheduleException exception) {
            log.error(exception.getMessage());
            return exception.getMessage();
        }
    }


    private String showScheduleForWeek(AppUser appUser, boolean isNextWeek, boolean isDetailed) {
        try {
            Student student = studentService.findByAppUser(appUser);
            Long studentId = student.getId();
            List<String> daysOfWeek = scheduleDayService.findDistinctDayNames();
            LocalDate startDate = isNextWeek ? getStartOfNextWeek() : getStartOfWeek();
            LocalDate endDate = getEndOfWeek(startDate);
            String weekPeriod = getWeekPeriod(startDate, endDate);
            Boolean isEvenWeek = isNextWeek != isEvenWeek();
            List<Lesson> lessonList;
            if (isDetailed) {
                lessonList = lessonService
                        .findByWeekdaysAndStudentAndEvenWeek(studentId, daysOfWeek, isEvenWeek);
            } else {
                List<ScheduleDay> byIsEvenWeek = scheduleDayService.findByIsEvenWeek(isEvenWeek);
                lessonList= lessonService.findByScheduleDay(byIsEvenWeek);
            }
            return buildStudentScheduleResponse(lessonList, student, weekPeriod);
        } catch (ScheduleException exception) {
            log.error(exception.getMessage());
            return exception.getMessage();
        }
    }

    private String buildStudentScheduleResponse(List<Lesson> lessons, Student student, String dateInfo) {
        StringBuilder scheduleStringBuilder = new StringBuilder();
        scheduleStringBuilder
                .append(student.getFirstName()).append(" ")
                .append(student.getLastName())
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
            log.error(exception.getMessage());
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
