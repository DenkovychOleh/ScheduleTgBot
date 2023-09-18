package com.dnk.service;

import com.dnk.entity.AppUser;

public interface AppUserService {
    AppUser findByTelegramUserId(Long telegramUserId);
}
