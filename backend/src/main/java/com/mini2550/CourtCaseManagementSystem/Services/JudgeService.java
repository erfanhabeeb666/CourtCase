package com.mini2550.CourtCaseManagementSystem.Services;


import com.mini2550.CourtCaseManagementSystem.Dtos.UploadDocumentRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mini2550.CourtCaseManagementSystem.Dtos.DocumentDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.HearingDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.HearingUpdateRequest;
import com.mini2550.CourtCaseManagementSystem.Dtos.NextHearingRequest;
import com.mini2550.CourtCaseManagementSystem.Enums.CaseStatus;
import com.mini2550.CourtCaseManagementSystem.Models.Case;
import com.mini2550.CourtCaseManagementSystem.Models.Document;
import com.mini2550.CourtCaseManagementSystem.Models.Hearing;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Repositories.CaseRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.DocumentRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.HearingRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.UserRepository;
import com.mini2550.CourtCaseManagementSystem.Dtos.VerdictUpdateRequest;
import com.mini2550.CourtCaseManagementSystem.Security.JwtService;
import com.mini2550.CourtCaseManagementSystem.Security.JwtUtils;
import com.mini2550.CourtCaseManagementSystem.Utils.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
@Service
public class JudgeService {
    private final FileUploadUtil fileUploadUtil;
    private final HttpServletRequest servletRequest;
    private final JwtService jwtService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final CaseRepository caseRepository;
    private final HearingRepository hearingRepository;
    private final DocumentRepository documentRepository;

    public JudgeService(FileUploadUtil fileUploadUtil, HttpServletRequest servletRequest, JwtService jwtService, JwtUtils jwtUtils, UserRepository userRepository, CaseRepository caseRepository, HearingRepository hearingRepository, DocumentRepository documentRepository) {
        this.fileUploadUtil = fileUploadUtil;
        this.servletRequest = servletRequest;
        this.jwtService = jwtService;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.caseRepository = caseRepository;
        this.hearingRepository = hearingRepository;
        this.documentRepository = documentRepository;
    }

    public void uploadDocument(UploadDocumentRequest request) {
        Long userId = Long.valueOf(jwtService.extractId(jwtUtils.getJwtFromRequest(servletRequest)));
        User user = userRepository.findById(userId).orElseThrow();
        fileUploadUtil.uploadDocument(request,user);
    }

    // Removed generic addHearing API in favor of scheduling only the next hearing

    /**
     * Schedule the next hearing with only a next hearing date (and optional summary).
     * This should be used instead of a general-purpose add hearing API.
     */
    public HearingDto scheduleNextHearing(Long caseId, NextHearingRequest request) {
        User judge = getCurrentUser();
        Case courtCase = getAuthorizedCase(caseId, judge);
        if (courtCase.getStatus() == CaseStatus.CLOSED || courtCase.getVerdict() != null) {
            throw new RuntimeException("Cannot schedule next hearing: case is closed or already has a verdict.");
        }
        if (request.getNextHearingDate() == null) {
            throw new RuntimeException("Validation failed: nextHearingDate is required.");
        }
        // prevent duplicate hearing on the same date for this case
        if (hearingRepository.existsByCourtCaseAndHearingDate(courtCase, request.getNextHearingDate())) {
            throw new RuntimeException("A hearing is already scheduled for this date.");
        }

        Hearing hearing = new Hearing();
        hearing.setCourtCase(courtCase);
        hearing.setHearingDate(request.getNextHearingDate());
        hearing.setJudgeSummary(request.getJudgeSummary());
        hearingRepository.save(hearing);

        courtCase.setNextHearingDate(request.getNextHearingDate());
        caseRepository.save(courtCase);

        return toHearingDto(hearing);
    }

    public List<HearingDto> getHearings(Long caseId) {
        User judge = getCurrentUser();
        Case courtCase = getAuthorizedCase(caseId, judge);
        return hearingRepository.findByCourtCaseOrderByHearingDateAsc(courtCase)
                .stream().map(this::toHearingDto).collect(Collectors.toList());
    }

    public HearingDto updateHearing(Long caseId, Long hearingId, HearingUpdateRequest request) {
        User judge = getCurrentUser();
        Case courtCase = getAuthorizedCase(caseId, judge);
        Hearing hearing = hearingRepository.findById(hearingId).orElseThrow(() -> new RuntimeException("Hearing not found"));
        if (!hearing.getCourtCase().getId().equals(courtCase.getId())) {
            throw new RuntimeException("Hearing does not belong to the given case");
        }

        if (request.getHearingDate() != null) hearing.setHearingDate(request.getHearingDate());
        if (request.getJudgeSummary() != null) hearing.setJudgeSummary(request.getJudgeSummary());
        hearingRepository.save(hearing);

        // If verdict provided, close the case and clear next hearing
        if (request.getVerdict() != null && !request.getVerdict().isBlank()) {
            courtCase.setVerdict(request.getVerdict());
            courtCase.setVerdictDate(request.getVerdictDate() != null ? request.getVerdictDate() : LocalDate.now());
            courtCase.setStatus(CaseStatus.CLOSED);
            courtCase.setNextHearingDate(null);
            caseRepository.save(courtCase);
            return toHearingDto(hearing);
        }

        // If judge provided next hearing date, create it only if case is not closed and no verdict exists
        if (request.getNextHearingDate() != null) {
            if (courtCase.getStatus() == CaseStatus.CLOSED || courtCase.getVerdict() != null) {
                throw new RuntimeException("Cannot schedule next hearing: case is closed or already has a verdict.");
            }
            if (hearingRepository.existsByCourtCaseAndHearingDate(courtCase, request.getNextHearingDate())) {
                throw new RuntimeException("A hearing is already scheduled for this date.");
            }
            Hearing next = new Hearing();
            next.setCourtCase(courtCase);
            next.setHearingDate(request.getNextHearingDate());
            hearingRepository.save(next);
            courtCase.setNextHearingDate(request.getNextHearingDate());
            caseRepository.save(courtCase);
        }
        return toHearingDto(hearing);
    }

    public com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto updateVerdict(Long caseId, VerdictUpdateRequest request) {
        User judge = getCurrentUser();
        Case courtCase = getAuthorizedCase(caseId, judge);

        courtCase.setVerdict(request.getVerdict());
        courtCase.setVerdictDate(request.getVerdictDate() != null ? request.getVerdictDate() : LocalDate.now());
        courtCase.setStatus(CaseStatus.CLOSED);
        courtCase.setNextHearingDate(null);
        Case saved = caseRepository.save(courtCase);
        return toCaseDto(saved);
    }

    public List<DocumentDto> getDocuments(Long caseId) {
        User judge = getCurrentUser();
        Case courtCase = getAuthorizedCase(caseId, judge);
        return documentRepository.findByCourtCase(courtCase).stream().map(this::toDocumentDto).collect(Collectors.toList());
    }

    private User getCurrentUser() {
        Long userId = Long.valueOf(jwtService.extractId(jwtUtils.getJwtFromRequest(servletRequest)));
        return userRepository.findById(userId).orElseThrow();
    }

    private Case getAuthorizedCase(Long caseId, User judge) {
        Case courtCase = caseRepository.findById(caseId).orElseThrow(() -> new RuntimeException("Case not found"));
        if (courtCase.getJudge() == null || !courtCase.getJudge().getId().equals(judge.getId())) {
            throw new RuntimeException("Not authorized for this case");
        }
        return courtCase;
    }

    private HearingDto toHearingDto(Hearing h) {
        HearingDto dto = new HearingDto();
        dto.setId(h.getId());
        dto.setHearingDate(h.getHearingDate());
        dto.setJudgeSummary(h.getJudgeSummary());
        dto.setCreatedAt(h.getCreatedAt());
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
}
