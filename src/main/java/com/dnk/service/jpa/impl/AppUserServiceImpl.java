package com.dnk.service.jpa.impl;

import com.dnk.dao.AppUserDAO;
import com.dnk.entity.AppUser;
import com.dnk.exception.ScheduleException;
import com.dnk.service.jpa.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserDAO appUserDAO;

    @Override
    public AppUser findByTelegramUserId(Long telegramUserId) {
        return appUserDAO.findByTelegramUserId(telegramUserId)
                .orElseThrow(() -> new ScheduleException("Помилка знаходження користувача за даним Telegram, щоб привʼязати аккаунта напишить - @oleh_denkovych"));
    }

    @Override
    public AppUser save(AppUser appUser) {
        return appUserDAO.save(appUser);
    }

    @Override
    public List<AppUser> findAll() {
        return appUserDAO.findAll();
    }

    @Override
    public List<AppUser> findAllByNotificationStatus() {
        return appUserDAO.findAllByNotificationStatus()
                .filter(strings -> !strings.isEmpty())
                .orElseThrow(() -> new ScheduleException("Не знайдено користувачів, у яких увімкнені сповіщення"));
    }

    @Override
    public List<AppUser> findAppUsersWithoutStudents() {
        return appUserDAO.findAppUsersWithoutStudents()
                .filter(strings -> !strings.isEmpty())
                .orElseThrow(() -> new ScheduleException("Нових користувачів не знайдено"));
    }

    @Override
    public boolean existsByTelegramUserId(Long id) {
        return appUserDAO.existsByTelegramUserId(id);
    }
}
