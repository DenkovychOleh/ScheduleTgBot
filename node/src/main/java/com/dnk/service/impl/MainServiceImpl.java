package com.dnk.service.impl;

import com.dnk.dao.AppUserDAO;
import com.dnk.dao.RawDataDAO;
import com.dnk.entity.*;
import com.dnk.service.MainService;
import com.dnk.service.ProducerService;
import com.dnk.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Optional;

import static com.dnk.service.enums.ServiceCommands.*;

@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final ScheduleService scheduleService;
    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO, ScheduleService scheduleService) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.scheduleService = scheduleService;
    }

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

    private String showStudentScheduleThisWeek(AppUser appUser) {
        Optional<Student> student = scheduleService.findByAppUser(appUser);
        if (student.isPresent()) {
            Optional<List<Lesson>> optionalLessonList = scheduleService.getLessonsForStudentById(student.get().getId());
            if (optionalLessonList.isPresent()) {
                List<Lesson> lessonList = optionalLessonList.get();
                if (lessonList.isEmpty()) {
                    return student.get().getFistName() + " для вас пар не знайдено";
                } else {
                    StringBuilder scheduleStringBuilder = new StringBuilder();
                    scheduleStringBuilder.append(student.get().getFistName()).append(", ваш розклад на цей тиждень:\n");

                    for (Lesson lesson : lessonList) {
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
            } else {
                return student.get().getFistName() + "цього тижня для вас пар не знайдено";

            }
        } else {
            return "Ваш телеграм не привязаний до картки студента, зворотній звʼязок можна отримати - @oleh_denkovych";
        }
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
        Optional<AppUser> optionalAppUser = appUserDAO.findByTelegramUserId(telegramUser.getId());
        if (!optionalAppUser.isPresent()) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .fistName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return optionalAppUser.get();
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
