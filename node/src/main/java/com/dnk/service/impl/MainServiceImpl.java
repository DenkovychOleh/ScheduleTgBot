package com.dnk.service.impl;

import com.dnk.dao.AppUserDAO;
import com.dnk.dao.RawDataDAO;
import com.dnk.entity.*;
import com.dnk.exception.ScheduleException;
import com.dnk.service.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dnk.service.enums.ServiceCommands.*;

@AllArgsConstructor
@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserService appUserService;
    private final LessonService lessonService;
    private final ScheduleDayService scheduleDayService;
    private final ScheduleService scheduleService;
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
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if (MY_SCHEDULE_THIS_WEEK.toString().equals(cmd)) {
            return showStudentScheduleThisWeek(appUser);
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
    private List<ScheduleDay> getStudentScheduleDays(Long studentId, boolean isEvenWeek) {
        List<Schedule> studentSchedules = scheduleService.getSchedulesForStudentById(studentId);
        // Фільтруємо розклад для парного або непарного тижня
        List<Schedule> filteredSchedules = studentSchedules.stream()
                .filter(schedule -> schedule.getScheduleDay().getIsEvenWeek() == isEvenWeek)
                .collect(Collectors.toList());

        // Отримуємо дні розкладу
        List<ScheduleDay> scheduleDays = new ArrayList<>();
        for (Schedule schedule : filteredSchedules) {
            scheduleDays.addAll(scheduleDayService.getScheduleDaysByScheduleId(schedule.getId()));
        }

        return scheduleDays;
    }

    private List<Lesson> getStudentLessonsForWeek(Long studentId, List<ScheduleDay> scheduleDays) {
        List<Lesson> studentLessons = new ArrayList<>();

        for (ScheduleDay scheduleDay : scheduleDays) {
            List<Lesson> lessons = lessonService.getLessonsByScheduleDayId(scheduleDay.getId());
            studentLessons.addAll(lessons);
        }

        return studentLessons;
    }

    private List<Lesson> filterLessonsForToday(List<Lesson> lessons) {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Kiev"));
        Locale ukrainianLocale = new Locale("uk", "UA");
        String dayName = today.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, ukrainianLocale);

        return lessons.stream()
                .filter(lesson -> lesson.getScheduleDay().getDayName().equalsIgnoreCase(dayName))
                .collect(Collectors.toList());
    }

    private String buildScheduleResponse(List<Lesson> lessons, Student student) {
        StringBuilder scheduleStringBuilder = new StringBuilder();

        scheduleStringBuilder.append(student.getFirstName()).append(", ваш розклад на цей тиждень:\n");

        for (Lesson lesson : lessons) {
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

    public String showStudentScheduleThisWeek(AppUser appUser) {
        boolean isEvenWeek = determineCurrentWeek();
        Student student = studentService.findByAppUser(appUser);
        Long studentId = student.getId();
        List<ScheduleDay> scheduleDays = getStudentScheduleDays(studentId, isEvenWeek);
        List<Lesson> studentLessons = getStudentLessonsForWeek(studentId, scheduleDays);
        List<Lesson> todayLessons = filterLessonsForToday(studentLessons);

        if (todayLessons.isEmpty()) {
            return "Для вас сьогодні пари не знайдено.";
        } else {
            return buildScheduleResponse(todayLessons, student);
        }
    }

    private boolean determineCurrentWeek() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Kiev"));
         return today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()) % 2 == 0;
    }

    private String help() {
        return "Cписок доступних команд:\n" +
                "/MyScheduleThisWeek - виводить твій розклад на цей тиждень;\n" +
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
