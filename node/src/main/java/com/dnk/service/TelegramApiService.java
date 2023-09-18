package com.dnk.service;

import com.dnk.entity.AppUser;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.dnk.service.enums.ServiceCommands.*;
import static com.dnk.service.enums.ServiceCommands.HELP;

public interface TelegramApiService {
    void processTextMessage(Update update);
    void sendAnswer(String output, Long chatId);
}
