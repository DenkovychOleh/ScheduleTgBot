package com.dnk.service.impl;

import com.dnk.dao.AppUserDAO;
import com.dnk.dao.RawDataDAO;
import com.dnk.entity.AppUser;
import com.dnk.entity.RawData;
import com.dnk.service.MainService;
import com.dnk.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.dnk.service.enums.ServiceCommands.*;

@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        AppUser appUser = findOrSaveAppUser(update);

        String text = update.getMessage().getText();
        String output = "";

        output = processServiceCommand(appUser, text);

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
            return "мій розклад на цей тиждень";
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

    private String help() {
        return "Cписок доступних команд:\n" +
                "/MyScheduleThisWeek - виводить твій розклад на цей тиждень;\n" +
                "/MyScheduleNextWeek - виводить твій розклад на наступний тиждень;\n" +
                "/ScheduleThisWeek   - виводить загальний розклад на цей тиждень;\n" +
                "/ScheduleNextWeek   - виводить загальний розклад на наступний тиждень;";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDAO.findByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .fistName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
