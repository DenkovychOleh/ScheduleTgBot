package com.dnk.service;

import com.dnk.entity.AppUser;
import com.dnk.entity.Student;

public interface StudentService {
    Student findByAppUser(AppUser appUser);
}
