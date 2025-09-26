package com.mini2550.CourtCaseManagementSystem.Repositories;

import com.mini2550.CourtCaseManagementSystem.Enums.CaseStatus;
import com.mini2550.CourtCaseManagementSystem.Models.Case;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseRepository extends JpaRepository<Case, String> {
    List<Case> findByClient(User client);
    List<Case> findByJudge_Id(Long judgeId);
    List<Case> findByClientLawyer_IdOrOpposingLawyer_Id(Long clientLawyerId, Long opposingLawyerId);
    List<Case> findByStatus(CaseStatus status);
}
