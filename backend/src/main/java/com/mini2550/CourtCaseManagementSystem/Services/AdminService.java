package com.mini2550.CourtCaseManagementSystem.Services;

import com.mini2550.CourtCaseManagementSystem.Dtos.UserDto;
import com.mini2550.CourtCaseManagementSystem.Enums.UserType;
import com.mini2550.CourtCaseManagementSystem.Models.Judge;
import com.mini2550.CourtCaseManagementSystem.Repositories.JudgeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final PasswordEncoder passwordEncoder;
    private final JudgeRepository judgeRepository;

    public AdminService(PasswordEncoder passwordEncoder, JudgeRepository judgeRepository) {
        this.passwordEncoder = passwordEncoder;
        this.judgeRepository = judgeRepository;
    }

    public void addJudge(UserDto user) {
        Judge judge = new Judge();
        judge.setName(user.getName());
        judge.setEmail(user.getEmail());
        judge.setPassword(passwordEncoder.encode(user.getPassword()));
        judge.setUserType(UserType.JUDGE);
        judgeRepository.save(judge);
    }
}
