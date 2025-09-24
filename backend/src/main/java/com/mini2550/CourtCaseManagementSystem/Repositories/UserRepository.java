package com.mini2550.CourtCaseManagementSystem.Repositories;


import com.mini2550.CourtCaseManagementSystem.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import com.mini2550.CourtCaseManagementSystem.Enums.UserType;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByUserType(UserType userType);
}
