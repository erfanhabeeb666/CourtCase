package com.mini2550.CourtCaseManagementSystem.Services;

import com.mini2550.CourtCaseManagementSystem.Dtos.HearingDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.DocumentDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.UploadDocumentRequest;
import com.mini2550.CourtCaseManagementSystem.Models.Case;
import com.mini2550.CourtCaseManagementSystem.Models.Hearing;
import com.mini2550.CourtCaseManagementSystem.Models.Document;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Repositories.CaseRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.HearingRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.UserRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.DocumentRepository;
import com.mini2550.CourtCaseManagementSystem.Security.JwtService;
import com.mini2550.CourtCaseManagementSystem.Security.JwtUtils;
import com.mini2550.CourtCaseManagementSystem.Utils.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LawyerService {
    private final FileUploadUtil fileUploadUtil;
    private final HttpServletRequest servletRequest;
    private final UserRepository userRepository;
    private final CaseRepository caseRepository;
    private final HearingRepository hearingRepository;
    private final JwtUtils jwtUtils;
    private final JwtService jwtService;
    private final DocumentRepository documentRepository;

    public LawyerService(FileUploadUtil fileUploadUtil,
                         HttpServletRequest servletRequest,
                         UserRepository userRepository,
                         CaseRepository caseRepository,
                         HearingRepository hearingRepository,
                         DocumentRepository documentRepository,
                         JwtUtils jwtUtils,
                         JwtService jwtService) {
        this.fileUploadUtil = fileUploadUtil;
        this.servletRequest = servletRequest;
        this.userRepository = userRepository;
        this.caseRepository = caseRepository;
        this.hearingRepository = hearingRepository;
        this.jwtUtils = jwtUtils;
        this.jwtService = jwtService;
        this.documentRepository = documentRepository;
    }

    public void uploadDocument(UploadDocumentRequest request) {
        Long userId = Long.valueOf(jwtService.extractId(jwtUtils.getJwtFromRequest(servletRequest)));
        User user = userRepository.getById(userId);
        fileUploadUtil.uploadDocument(request, user);
    }

    public List<com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto> listMyCases() {
        User lawyer = getCurrentUser();
        List<Case> cases = caseRepository.findByClientLawyer_IdOrOpposingLawyer_Id(lawyer.getId(), lawyer.getId());
        return cases.stream().map(this::toCaseDto).collect(Collectors.toList());
    }

    public com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto getCase(Long caseId) {
        User lawyer = getCurrentUser();
        Case c = caseRepository.findById(caseId).orElseThrow(() -> new RuntimeException("Case not found"));
        if (!isLawyerOnCase(lawyer, c)) throw new RuntimeException("Not authorized for this case");
        return toCaseDto(c);
    }

    public List<HearingDto> getHearings(Long caseId) {
        User lawyer = getCurrentUser();
        Case c = caseRepository.findById(caseId).orElseThrow(() -> new RuntimeException("Case not found"));
        if (!isLawyerOnCase(lawyer, c)) throw new RuntimeException("Not authorized for this case");
        return hearingRepository.findByCourtCaseOrderByHearingDateAsc(c)
                .stream().map(this::toHearingDto).collect(Collectors.toList());
    }

    public List<DocumentDto> getDocuments(Long caseId) {
        User lawyer = getCurrentUser();
        Case c = caseRepository.findById(caseId).orElseThrow(() -> new RuntimeException("Case not found"));
        if (!isLawyerOnCase(lawyer, c)) throw new RuntimeException("Not authorized for this case");
        return documentRepository.findByCourtCaseOrderByUploadedAtDesc(c)
                .stream().map(this::toDocumentDto).collect(Collectors.toList());
    }

    private boolean isLawyerOnCase(User lawyer, Case c) {
        return (c.getClientLawyer() != null && c.getClientLawyer().getId().equals(lawyer.getId()))
                || (c.getOpposingLawyer() != null && c.getOpposingLawyer().getId().equals(lawyer.getId()));
    }

    private User getCurrentUser() {
        Long userId = Long.valueOf(jwtService.extractId(jwtUtils.getJwtFromRequest(servletRequest)));
        return userRepository.findById(userId).orElseThrow();
    }

    private com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto toCaseDto(Case c) {
        com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto dto = new com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setType(c.getType() != null ? c.getType().name() : null);
        dto.setStatus(c.getStatus());
        dto.setNextHearingDate(c.getNextHearingDate());
        if (c.getClient() != null) dto.setClientName(c.getClient().getName());
        if (c.getClientLawyer() != null) {
            dto.setClientLawyerId(String.valueOf(c.getClientLawyer().getId()));
            dto.setClientLawyerName(c.getClientLawyer().getName());
        }
        if (c.getOpposingLawyer() != null) {
            dto.setOpposingLawyerId(String.valueOf(c.getOpposingLawyer().getId()));
            dto.setOpposingLawyerName(c.getOpposingLawyer().getName());
        }
        if (c.getJudge() != null) dto.setJudgeName(c.getJudge().getName());
        dto.setVerdict(c.getVerdict());
        dto.setVerdictDate(c.getVerdictDate());
        return dto;
    }

    private HearingDto toHearingDto(Hearing h) {
        HearingDto dto = new HearingDto();
        dto.setId(h.getId());
        dto.setHearingDate(h.getHearingDate());
        dto.setJudgeSummary(h.getJudgeSummary());
        dto.setCreatedAt(h.getCreatedAt());
        dto.setStatus(h.getStatus());
        return dto;
    }

    private DocumentDto toDocumentDto(Document d) {
        DocumentDto dto = new DocumentDto();
        dto.setId(d.getId());
        dto.setFileName(d.getFileName());
        dto.setFilePath(d.getFilePath());
        dto.setUploaderId(d.getUploaderId());
        dto.setNote(d.getNote());
        dto.setUploadedAt(d.getUploadedAt());
        return dto;
    }
}
