package com.dnk.service.impl;

import com.dnk.dao.AppUserDAO;
import com.dnk.dao.RawDataDAO;
import com.dnk.entity.AppUser;
import com.dnk.entity.RawData;
import com.dnk.exception.ScheduleException;
import com.dnk.service.AppUserService;
import com.dnk.service.CommandService;
import com.dnk.service.ProducerService;
import com.dnk.service.TelegramApiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.dnk.service.enums.ServiceCommands.*;

@AllArgsConstructor
@Service
public class TelegramApiServiceImpl implements TelegramApiService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final AppUserService appUserService;
    private final CommandService commandService;

    public void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("HTML");
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    public void processTextMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        String text = update.getMessage().getText();
        String output = processServiceCommand(appUser, text);
        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if (MY_SCHEDULE_TODAY.toString().equals(cmd)) {
            return commandService.showStudentScheduleToday(appUser);
        } else if (MY_SCHEDULE_TOMORROW.toString().equals(cmd)) {
            return commandService.showStudentScheduleTomorrow(appUser);
        } else if (MY_SCHEDULE_THIS_WEEK.toString().equals(cmd)) {
            return commandService.showStudentScheduleThisWeek(appUser);
        } else if (MY_SCHEDULE_NEXT_WEEK.toString().equals(cmd)) {
            return commandService.showStudentScheduleNextWeek(appUser);
        } else if (SCHEDULE_THIS_WEEK.toString().equals(cmd)) {
            return commandService.showScheduleThisWeek();
        } else if (SCHEDULE_NEXT_WEEK.toString().equals(cmd)) {
            return commandService.showScheduleNextWeek();
        } else if (HELP.toString().equals(cmd)) {
            return commandService.help();
        } else {
            return "Невідома команда, спробуй /help";
        }
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        Long telegramUserId = telegramUser.getId();
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
