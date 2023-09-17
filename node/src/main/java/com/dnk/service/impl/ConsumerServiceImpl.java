package com.dnk.service.impl;

import com.dnk.service.ConsumerService;
import com.dnk.service.MainService;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.dnk.model.RabbitQueue.TEXT_MESSAGE_UPDATED;

@Log4j
@Service
public class ConsumerServiceImpl implements ConsumerService {
    private final MainService mainService;

    public ConsumerServiceImpl(MainService mainService) {
        this.mainService = mainService;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATED)
    public void consumeTextMessageUpdates(Update update) {
        log.debug("NODE : Text message is received");
        mainService.processTextMessage(update);
    }
}
