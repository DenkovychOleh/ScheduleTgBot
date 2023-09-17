package com.dnk.dao;

import com.dnk.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {
    AppUser findByTelegramUserId(Long id);
}
