package com.dnk.service.impl;

import com.dnk.dao.AppUserDAO;
import com.dnk.entity.AppUser;
import com.dnk.exception.ScheduleException;
import com.dnk.service.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AppUserServiceImpl implements AppUserService {
    private final AppUserDAO appUserDAO;

    @Override
    public AppUser findByTelegramUserId(Long telegramUserId) {
        return appUserDAO.findByTelegramUserId(telegramUserId).orElseThrow(() -> new ScheduleException("Помилка знаходження користувача за даним Telegram, щоб привʼязати аккаунта напишить - @oleh_denkovych"));
    }
}
