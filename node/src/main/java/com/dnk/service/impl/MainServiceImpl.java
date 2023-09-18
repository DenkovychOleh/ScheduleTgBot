package com.dnk.service.impl;

import com.dnk.dao.AppUserDAO;
import com.dnk.dao.RawDataDAO;
import com.dnk.entity.AppUser;
import com.dnk.entity.Lesson;
import com.dnk.entity.RawData;
import com.dnk.entity.Student;
import com.dnk.exception.ScheduleException;
import com.dnk.service.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.dnk.service.enums.ServiceCommands.*;

@AllArgsConstructor
@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserService appUserService;
    private final LessonService lessonService;
    private final StudentService studentService;
    private final AppUserDAO appUserDAO;



    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        String text = update.getMessage().getText();
        String output = processServiceCommand(appUser, text);
        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("HTML");
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if (MY_SCHEDULE_THIS_WEEK.toString().equals(cmd)) {
            return showStudentScheduleThisWeek(appUser);
        } else if (MY_SCHEDULE_TODAY.toString().equals(cmd)) {
            return showStudentScheduleToday(appUser);
        } else if (MY_SCHEDULE_NEXT_WEEK.toString().equals(cmd)) {
            return "мій розклад на наступний тиждень";
        } else if (SCHEDULE_THIS_WEEK.toString().equals(cmd)) {
            return "загальний розклад на цей тиждень";
        } else if (SCHEDULE_NEXT_WEEK.toString().equals(cmd)) {
            return "загальний розклад на наступний тиждень";
        } else if (HELP.toString().equals(cmd)) {
            return help();
        } else {
            return "Невідома команда, спробуй /help";
        }
    }

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



    public String showStudentScheduleToday(AppUser appUser) {
        boolean currentWeek = determineCurrentWeek();
        try {
            Student student = studentService.findByAppUser(appUser);
            Long studentId = student.getId();
            String dayName = getCurrentDay();
            LocalDate currentDate = LocalDate.now();

            List<Lesson> todayLessons = lessonService
                    .findByDayNameAndStudentAndEvenWeek(dayName,studentId, currentWeek);

            String dateInfo = currentDate.format(DateTimeFormatter.ofPattern("dd MMMM", new Locale("uk")));

            return buildScheduleResponse(todayLessons, student, dateInfo);
        } catch (ScheduleException exception) {
            return "Error: " + exception.getMessage();
        }
    }

    public String showStudentScheduleThisWeek(AppUser appUser) {
        boolean isEvenWeek = determineCurrentWeek();
        try {
            Student student = studentService.findByAppUser(appUser);
            Long studentId = student.getId();
            List<String> daysOfWeek = getDaysOfWeek();
            LocalDate startDate = getStartOfWeek();
            LocalDate endDate = getEndOfWeek(startDate);

            List<Lesson> lessonList = lessonService
                    .findByWeekdaysAndStudentAndEvenWeek(studentId, isEvenWeek, daysOfWeek);

            String weekPeriod = getWeekPeriod(startDate, endDate);

            return buildScheduleResponse(lessonList, student, weekPeriod);
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
        DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();

        int daysUntilMonday = currentDayOfWeek.getValue() - DayOfWeek.MONDAY.getValue();

        return currentDate.minusDays(daysUntilMonday);
    }

    private LocalDate getEndOfWeek(LocalDate startOfWeek) {
        return startOfWeek.plusDays(4);
    }


    private String getCurrentDay() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Kiev"));
        Locale ukrainianLocale = new Locale("uk", "UA");
        String dayName = today.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, ukrainianLocale);
        dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
        return dayName;
    }

    public List<String> getDaysOfWeek() {
        List<String> daysOfWeek = new ArrayList<>();

        Locale ukrainianLocale = new Locale("uk", "UA");

        for (DayOfWeek day : DayOfWeek.values()) {
            String dayName = day.getDisplayName(TextStyle.FULL, ukrainianLocale);
            dayName = dayName.replace("ʼ", "");
            dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
            daysOfWeek.add(dayName);
        }

        return daysOfWeek;
    }

    private boolean determineCurrentWeek() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Kiev"));
         return today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()) % 2 == 0;
    }

    private String help() {
        return "Cписок доступних команд:\n" +
                "/MyScheduleThisWeek - виводить твій розклад на цей тиждень;\n" +
                "/MyScheduleToday - виводить твій розклад на цей тиждень;\n" +
                "/MyScheduleNextWeek - виводить твій розклад на наступний тиждень;\n" +
                "/ScheduleThisWeek   - виводить загальний розклад на цей тиждень;\n" +
                "/ScheduleNextWeek   - виводить загальний розклад на наступний тиждень;";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        Long telegramUserId = telegramUser.getId();
        appUserService.findByTelegramUserId(telegramUserId);
        try {
            return appUserService.findByTelegramUserId(telegramUserId);
        } catch (ScheduleException ex) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUserId)
                    .username(telegramUser.getUserName())
                    .fistName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .build();
            return appUserDAO.save(transientAppUser);
        }
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
