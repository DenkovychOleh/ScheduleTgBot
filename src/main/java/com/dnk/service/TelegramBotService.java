package com.dnk.service;

import com.dnk.config.BotConfig;
import com.dnk.entity.AppUser;
import com.dnk.entity.Student;
import com.dnk.exception.ScheduleException;
import com.dnk.service.jpa.AppUserService;
import com.dnk.service.jpa.StudentService;
import com.dnk.service.node.CommandService;
import com.dnk.service.node.impl.CallbackService;
import lombok.extern.log4j.Log4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.dnk.service.node.enums.ServiceCommands.*;


@Log4j
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private final BotConfig config;
    private final CommandService commandService;
    private final AppUserService appUserService;
    private final StudentService studentService;
    private final CallbackService callbackService;
    private AppUser tempAppUser;

    public TelegramBotService(BotConfig config, CommandService commandService, AppUserService appUserService, StudentService studentService, CallbackService callbackService) {
        this.config = config;
        this.commandService = commandService;
        this.appUserService = appUserService;
        this.studentService = studentService;
        this.callbackService = callbackService;

        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/myschedulethisweek", "розклад цього тижня, коли обов'язкова присутність на парах"));
        botCommandList.add(new BotCommand("/myschedulenextweek", "розклад наступного тижня, коли обов'язкова присутність на парах"));
        botCommandList.add(new BotCommand("/scheduletoday", "розклад на сьогодні"));
        botCommandList.add(new BotCommand("/scheduletomorrow", " розклад на завтра"));
        botCommandList.add(new BotCommand("/schedulethisweek", "розклад цього тижня"));
        botCommandList.add(new BotCommand("/schedulenextweek", "розклад наступного тижня"));
        botCommandList.add(new BotCommand("/studentsthisweek", "список студентів, в яких обов'язкова присутність на парах цього тижня"));
        botCommandList.add(new BotCommand("/studentsnextweek", "список студентів, в яких обов'язкова присутність на парах наступного тижня"));
        botCommandList.add(new BotCommand("/seton", "увімкнути щоденні сповіщення про розклад пар на завтра"));
        botCommandList.add(new BotCommand("/setoff", "вимкнути щоденні сповіщення про розклад пар на завтра"));
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
        } else if (update.hasCallbackQuery()) {
            processCallbackQuery(update);
        }
    }

    private void processCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        searchCallbackCommand(callbackData, messageId, chatId);
    }

    private void searchCallbackCommand(String callbackData, long messageId, long chatId) {
        if (callbackData.equals("YES_BUTTON")) {
            EditMessageText message = new EditMessageText();
            message.setChatId(chatId);
            message.setText("Виберіть користувача:");
            message.setMessageId((int) messageId);
            message.setReplyMarkup(callbackService.createKeyboardForAppUsers());
            executeMessage(message);
        } else if (callbackData.equals("NO_BUTTON")) {
            EditMessageText message = new EditMessageText();
            message.setChatId(chatId);
            message.setText("Бажаю гарного дня!");
            message.setMessageId((int) messageId);
            executeMessage(message);
        } else if (callbackService.isAppUserExistFromCallback(callbackData)) {
            tempAppUser = callbackService.getAppUser(callbackData);
            EditMessageText message = new EditMessageText();
            message.setChatId(chatId);
            message.setText("Оберіть студента до якого бажаєте привязати даного користувача:");
            message.setMessageId((int) messageId);
            message.setReplyMarkup(callbackService.createKeyboardForStudents());
            executeMessage(message);
        } else if (callbackService.isStudentExistFromCallback(callbackData)) {
            Student student = callbackService.getStudent(callbackData);
            student.setAppUser(tempAppUser);
            studentService.save(student);
            EditMessageText message = new EditMessageText();
            message.setChatId(chatId);
            message.setText("Користувач успішно був привязаний до студента. Бажаєте продовжити?");
            message.setMessageId((int) messageId);
            message.setReplyMarkup(callbackService.createKeyboardYesOrNo());
            executeMessage(message);
        }
    }
    private void sendMassage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        message.setParseMode("HTML");
        if (textToSend.equals("Виберіть користувача:")) {
            message.setReplyMarkup(callbackService.createKeyboardForAppUsers());
        }
        executeMessage(message);
    }

    private void executeMessage(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
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
        Long chatId = update.getMessage().getChatId();
        String output = processServiceCommand(appUser, text);
        sendMassage(chatId, output);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if (cmd.startsWith(SEND.toString()) && appUser.getRole().equals(AppUser.Roles.ADMIN)) {
            return sendCommand(cmd);
        } else if (SET_STUDENT.toString().equals(cmd) && appUser.getRole().equals(AppUser.Roles.ADMIN)) {
            return "Виберіть користувача:";
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
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .role(AppUser.Roles.USER)
                    .notificationStatus(AppUser.NotificationStatus.ON)
                    .build();
            return appUserService.save(transientAppUser);
        }
    }

    private String sendCommand(String cmd) {
            String substring ="Важлива інформація:" + cmd.substring(5);
            List<AppUser> appUsers = appUserService.findAll();
            for (AppUser user : appUsers) {
                sendMassage(user.getTelegramUserId(), substring);
            }
            return "Всіх користувачів повідомлено";
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
