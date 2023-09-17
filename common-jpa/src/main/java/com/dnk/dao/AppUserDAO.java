package com.dnk.dao;

import com.dnk.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByTelegramUserId(Long id);
}
