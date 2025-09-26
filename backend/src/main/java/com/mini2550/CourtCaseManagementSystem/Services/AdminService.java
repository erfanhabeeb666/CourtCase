package com.mini2550.CourtCaseManagementSystem.Services;

import com.mini2550.CourtCaseManagementSystem.Dtos.AssignCaseRequest;
import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.UserDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.UserSummaryDto;
import com.mini2550.CourtCaseManagementSystem.Enums.CaseStatus;
import com.mini2550.CourtCaseManagementSystem.Enums.Status;
import com.mini2550.CourtCaseManagementSystem.Enums.UserType;
import com.mini2550.CourtCaseManagementSystem.Models.Case;
import com.mini2550.CourtCaseManagementSystem.Models.Judge;
import com.mini2550.CourtCaseManagementSystem.Models.Lawyer;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Models.Hearing;
import com.mini2550.CourtCaseManagementSystem.Enums.HearingStatus;
import com.mini2550.CourtCaseManagementSystem.Repositories.CaseRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.JudgeRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.LawyerRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.HearingRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final PasswordEncoder passwordEncoder;
    private final JudgeRepository judgeRepository;
    private final CaseRepository caseRepository;
    private final LawyerRepository lawyerRepository;
    private final HearingRepository hearingRepository;
    private final UserRepository userRepository;

    public AdminService(PasswordEncoder passwordEncoder, JudgeRepository judgeRepository, CaseRepository caseRepository, LawyerRepository lawyerRepository, HearingRepository hearingRepository, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.judgeRepository = judgeRepository;
        this.caseRepository = caseRepository;
        this.lawyerRepository = lawyerRepository;
        this.hearingRepository = hearingRepository;
        this.userRepository = userRepository;
    }
    public CaseDto convertToDto(Case courtCase) {
        if (courtCase == null) return null;

        CaseDto dto = new CaseDto();
        dto.setId(courtCase.getId());
        dto.setTitle(courtCase.getTitle());
        dto.setType(courtCase.getType() != null ? courtCase.getType().name() : null);
        dto.setStatus(courtCase.getStatus());
        dto.setNextHearingDate(courtCase.getNextHearingDate());
        dto.setVerdict(courtCase.getVerdict());
        dto.setVerdictDate(courtCase.getVerdictDate());

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
        try {
            Judge judge = new Judge();
            judge.setName(user.getName());
            judge.setEmail(user.getEmail());
            judge.setPassword(passwordEncoder.encode(user.getPassword()));
            judge.setUserType(UserType.JUDGE);
            judge.setStatus(Status.ACTIVE);
            judgeRepository.save(judge);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while creating judge.", e);
        }
    }

    @Transactional
    public CaseDto assignCaseByAdmin(String caseId, AssignCaseRequest request) {
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

        Case savedCase = caseRepository.save(courtCase);

        // Automatically create a Hearing record if a next hearing date is provided
        if (request.getNextHearingDate() != null) {
            // prevent duplicate hearing on the same date for this case
            if (!hearingRepository.existsByCourtCaseAndHearingDate(savedCase, request.getNextHearingDate())) {
                Hearing hearing = new Hearing();
                hearing.setCourtCase(savedCase);
                hearing.setHearingDate(request.getNextHearingDate());
                hearing.setStatus(HearingStatus.SCHEDULED);
                hearingRepository.save(hearing);
            }
        }

        return convertToDto(savedCase);
    }

    // User management
    public List<UserSummaryDto> listUsers(UserType role) {
        List<User> users = (role != null) ? userRepository.findByUserType(role) : userRepository.findAll();
        return users.stream().map(this::toUserSummaryDto).collect(Collectors.toList());
    }

    public UserSummaryDto updateUserStatus(Long userId, Status status) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        userRepository.save(user);
        return toUserSummaryDto(user);
    }

    private UserSummaryDto toUserSummaryDto(User u) {
        UserSummaryDto dto = new UserSummaryDto();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        dto.setUserType(u.getUserType() != null ? u.getUserType().name() : null);
        dto.setStatus(u.getStatus());
        return dto;
    }

    // Cases listing for admin (restricted to NEW by requirement)
    public List<CaseDto> listCases() {
        List<Case> cases = caseRepository.findByStatus(CaseStatus.NEW);
        return cases.stream().map(this::convertToDto).collect(Collectors.toList());
    }

}
