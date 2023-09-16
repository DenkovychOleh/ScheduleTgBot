package ua.dnk.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.dnk.service.impl.UpdateProducerImpl;
import ua.dnk.utils.MessageUtils;

import static ua.dnk.model.RabbitQueue.TEXT_MESSAGE_UPDATED;

@Log4j
@Component
public class UpdateController {
    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducerImpl updateProducer;


    public UpdateController(MessageUtils messageUtils, UpdateProducerImpl updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        if (!update.hasMessage()) {
            log.error("Received update is null");
            return;
        }
        if (!update.getMessage().hasText()) {
            log.error("Unsupported message type is received: " + update);
            setUnsupportedMessageTypeView(update);
        } else {
            processTextMessage(update);
        }
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATED, update);
    }

    private void setUnsupportedMessageTypeView(Update update) {
        SendMessage sendMessage = messageUtils.generateSandMessageWithText(update,
                "Unsupported message type");
        setView(sendMessage);
    }

    private void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }

}
