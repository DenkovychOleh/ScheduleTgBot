package com.dnk.dao;

import com.dnk.entity.AppUser;
import com.dnk.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentDAO extends JpaRepository<Student, Long> {
    Optional<Student> findByAppUser(AppUser appUser);
}
