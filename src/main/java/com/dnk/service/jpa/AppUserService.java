package com.dnk.service.jpa;

import com.dnk.entity.AppUser;

import java.util.List;

public interface AppUserService {
    AppUser findByTelegramUserId(Long telegramUserId);
    AppUser save(AppUser appUser);
    List<AppUser> findAll();
    List<AppUser> findAllByNotificationStatus();
    List<AppUser> findAppUsersWithoutStudents();
    boolean existsByTelegramUserId(Long id);
}
