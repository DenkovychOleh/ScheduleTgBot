package com.dnk.service;

import com.dnk.config.BotConfig;
import com.dnk.entity.AppUser;
import com.dnk.exception.ScheduleException;
import com.dnk.service.jpa.AppUserService;
import com.dnk.service.node.CommandService;
import lombok.extern.log4j.Log4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.dnk.service.node.enums.ServiceCommands.*;


@Log4j
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private final BotConfig config;
    private final CommandService commandService;
    private final AppUserService appUserService;

    public TelegramBotService(BotConfig config, CommandService commandService, AppUserService appUserService) {
        this.config = config;
        this.commandService = commandService;
        this.appUserService = appUserService;

        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/myscheduletoday", "виводить інформацію, чи обов'язкова присутність на парах та розклад на сьогодні"));
        botCommandList.add(new BotCommand("/myscheduletomorrow", "виводить інформацію, чи обов'язкова присутність на парах та розклад на завтра"));
        botCommandList.add(new BotCommand("/myschedulethisweek", "виводить список днів та розклад цього тижня, коли обов'язкова присутність на парах"));
        botCommandList.add(new BotCommand("/myschedulenextweek", "виводить список днів та розклад наступного тижня, коли обов'язкова присутність на парах"));
        botCommandList.add(new BotCommand("/scheduletoday", "виводить розклад на сьогодні"));
        botCommandList.add(new BotCommand("/scheduletomorrow", "виводить розклад на завтра"));
        botCommandList.add(new BotCommand("/schedulethisweek", "виводить розклад цього тижня"));
        botCommandList.add(new BotCommand("/schedulenextweek", "виводить розклад наступного тижня"));
        botCommandList.add(new BotCommand("/studentsthisweek", "виводить розклад зі списком студентів, в яких обов'язкова присутність на парах"));
        botCommandList.add(new BotCommand("/studentsnextweek", "виводить розклад зі списком студентів, в яких обов'язкова присутність на парах цього тижня"));
        botCommandList.add(new BotCommand("/seton", "вмикає сповіщення кожного дня об 20:00, щодо розкладу пар на завтра"));
        botCommandList.add(new BotCommand("/setoff", "вимикає сповіщення кожного дня об 20:00, щодо розкладу пар на завтра"));
        botCommandList.add(new BotCommand("/help", "виводить список корисних команд"));
        try {
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list" + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            processTextMessage(update);
        }
    }

    private void sendMassage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        message.setParseMode("HTML");
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    public void processTextMessage(Update update) {
        AppUser appUser = findOrSaveAppUser(update);
        String text = update.getMessage().getText();
        String output = processServiceCommand(appUser, text);
        Long chatId = update.getMessage().getChatId();
        sendMassage(chatId, output);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if (cmd.startsWith(SEND.toString())) {
            return sendCommand(appUser, cmd);
        } else if (SET_NOTIFICATION_ON.toString().equals(cmd)) {
            return commandService.setNotificationOn(appUser);
        } else if (SET_NOTIFICATION_OFF.toString().equals(cmd)) {
            return commandService.setNotificationOff(appUser);
        } else if (SCHEDULE_TODAY.toString().equals(cmd)) {
            return commandService.showScheduleToday(appUser);
        } else if (MY_SCHEDULE_TODAY.toString().equals(cmd)) {
            return commandService.showMyScheduleToday(appUser);
        } else if (SCHEDULE_TOMORROW.toString().equals(cmd)) {
            return commandService.showScheduleTomorrow(appUser);
        } else if (MY_SCHEDULE_TOMORROW.toString().equals(cmd)) {
            return commandService.showMyScheduleTomorrow(appUser);
        } else if (SCHEDULE_THIS_WEEK.toString().equals(cmd)) {
            return commandService.showScheduleThisWeek(appUser);
        } else if (MY_SCHEDULE_THIS_WEEK.toString().equals(cmd)) {
            return commandService.showMyScheduleThisWeek(appUser);
        } else if (SCHEDULE_NEXT_WEEK.toString().equals(cmd)) {
            return commandService.showScheduleNextWeek(appUser);
        } else if (MY_SCHEDULE_NEXT_WEEK.toString().equals(cmd)) {
            return commandService.showMyScheduleNextWeek(appUser);
        } else if (STUDENTS_THIS_WEEK.toString().equals(cmd)) {
            return commandService.showStudentsThisWeek();
        } else if (STUDENTS_NEXT_WEEK.toString().equals(cmd)) {
            return commandService.showStudentsNextWeek();
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
                    .role(AppUser.Roles.USER)
                    .notificationStatus(AppUser.NotificationStatus.ON)
                    .build();
            return appUserService.save(transientAppUser);
        }
    }

    private String sendCommand(AppUser appUser, String cmd) {
        if (appUser.getRole().equals(AppUser.Roles.ADMIN)) {
            String substring ="Важлива інформація:" + cmd.substring(5);
            List<AppUser> appUsers = appUserService.findAll();
            for (AppUser user : appUsers) {
                sendMassage(user.getTelegramUserId(), substring);
            }
            return "Всіх користувачів повідомлено";
        } else {
            return "Команда /send вам недоступна.";
        }
    }


    @Scheduled(cron = "${cron.scheduler.tomorrow}")
    private void sendScheduleTomorrow() {
        sendScheduleForNotificationStatus(commandService::showMyScheduleTomorrow);
    }

    @Scheduled(cron = "${cron.scheduler.week}")
    private void sendScheduleNextWeek() {
        sendScheduleForNotificationStatus(commandService::showMyScheduleNextWeek);
    }

    private void sendScheduleForNotificationStatus(Function<AppUser, String> messageSupplier) {
        try {
            List<AppUser> allByNotificationStatus = appUserService.findAllByNotificationStatus();
            for (AppUser user : allByNotificationStatus) {
                    String message = messageSupplier.apply(user);
                    sendMassage(user.getTelegramUserId(), message);
            }
        } catch (ScheduleException exception) {
            log.error(exception.getMessage());
        }
    }

}
