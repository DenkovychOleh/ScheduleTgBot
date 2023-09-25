package com.dnk.dao;

import com.dnk.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByTelegramUserId(Long id);

    @Override
    List<AppUser> findAll();

    @Query("SELECT u FROM AppUser u WHERE u.notificationStatus = 'ON'")
    Optional<List<AppUser>> findAllByNotificationStatus();
}
