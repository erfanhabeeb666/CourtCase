package com.mini2550.CourtCaseManagementSystem.Services;

import com.mini2550.CourtCaseManagementSystem.Dtos.AssignCaseRequest;
import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.UserDto;
import com.mini2550.CourtCaseManagementSystem.Enums.CaseStatus;
import com.mini2550.CourtCaseManagementSystem.Enums.Status;
import com.mini2550.CourtCaseManagementSystem.Enums.UserType;
import com.mini2550.CourtCaseManagementSystem.Models.Case;
import com.mini2550.CourtCaseManagementSystem.Models.Judge;
import com.mini2550.CourtCaseManagementSystem.Models.Lawyer;
import com.mini2550.CourtCaseManagementSystem.Repositories.CaseRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.JudgeRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.LawyerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final PasswordEncoder passwordEncoder;
    private final JudgeRepository judgeRepository;
    private final CaseRepository caseRepository;
    private final LawyerRepository lawyerRepository;

    public AdminService(PasswordEncoder passwordEncoder, JudgeRepository judgeRepository, CaseRepository caseRepository, LawyerRepository lawyerRepository) {
        this.passwordEncoder = passwordEncoder;
        this.judgeRepository = judgeRepository;
        this.caseRepository = caseRepository;
        this.lawyerRepository = lawyerRepository;
    }
    public CaseDto convertToDto(Case courtCase) {
        if (courtCase == null) return null;

        CaseDto dto = new CaseDto();
        dto.setId(courtCase.getId());
        dto.setTitle(courtCase.getTitle());
        dto.setType(courtCase.getType() != null ? courtCase.getType().name() : null);
        dto.setStatus(courtCase.getStatus());
        dto.setNextHearingDate(courtCase.getNextHearingDate());

        // Client info
        if (courtCase.getClient() != null) {
            dto.setClientName(courtCase.getClient().getName());
        }

        // Client lawyer info
        if (courtCase.getClientLawyer() != null) {
            dto.setClientLawyerId(String.valueOf(courtCase.getClientLawyer().getId()));
            dto.setClientLawyerName(courtCase.getClientLawyer().getName());
        }

        // Opposing lawyer info
        if (courtCase.getOpposingLawyer() != null) {
            dto.setOpposingLawyerId(String.valueOf(courtCase.getOpposingLawyer().getId()));
            dto.setOpposingLawyerName(courtCase.getOpposingLawyer().getName());
        }

        // Judge info
        if (courtCase.getJudge() != null) {
            dto.setJudgeName(courtCase.getJudge().getName());
        }

        return dto;
    }


    public void addJudge(UserDto user) {
        Judge judge = new Judge();
        judge.setName(user.getName());
        judge.setEmail(user.getEmail());
        judge.setPassword(passwordEncoder.encode(user.getPassword()));
        judge.setUserType(UserType.JUDGE);
        judge.setStatus(Status.ACTIVE);
        judgeRepository.save(judge);
    }

    public CaseDto assignCaseByAdmin(Long caseId, AssignCaseRequest request) {
        Case courtCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        Lawyer opposingLawyer = lawyerRepository.findById(request.getOpposingLawyerId())
                .orElseThrow(() -> new RuntimeException("Opposing lawyer not found"));

        Judge judge = judgeRepository.findById(request.getJudgeId())
                .orElseThrow(() -> new RuntimeException("Judge not found"));

        courtCase.setOpposingLawyer(opposingLawyer);
        courtCase.setJudge(judge);
        courtCase.setNextHearingDate(request.getNextHearingDate());
        courtCase.setStatus(CaseStatus.ASSIGNED);

        return convertToDto(caseRepository.save(courtCase));
    }

}
