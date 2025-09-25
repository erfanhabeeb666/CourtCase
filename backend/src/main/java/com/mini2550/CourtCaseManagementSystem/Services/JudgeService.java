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
import com.mini2550.CourtCaseManagementSystem.Dtos.HearingWithCaseDto;
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
import com.mini2550.CourtCaseManagementSystem.Enums.HearingStatus;
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

        // If judge provided next hearing date, COMPLETE the current hearing and CREATE a new next hearing.
        if (request.getNextHearingDate() != null) {
            if (courtCase.getStatus() == CaseStatus.CLOSED || courtCase.getVerdict() != null) {
                throw new RuntimeException("Cannot schedule next hearing: case is closed or already has a verdict.");
            }
            // Prevent duplicate hearing on the same date for this case
            if (hearingRepository.existsByCourtCaseAndHearingDate(courtCase, request.getNextHearingDate())) {
                throw new RuntimeException("A hearing is already scheduled for this date.");
            }
            hearing.setJudgeSummary(request.getJudgeSummary());
            // Mark current hearing as COMPLETED (preserving log)
            hearing.setStatus(HearingStatus.COMPLETED);
            hearingRepository.save(hearing);

            // Create the next SCHEDULED hearing on the provided date
            Hearing next = new Hearing();
            next.setCourtCase(courtCase);
            next.setHearingDate(request.getNextHearingDate());
            next.setStatus(HearingStatus.SCHEDULED);
            hearingRepository.save(next);

            // Keep the case in sync with the next hearing date
            courtCase.setNextHearingDate(request.getNextHearingDate());
            caseRepository.save(courtCase);

            // Ensure only ONE future hearing exists for this case: remove any other future hearings except the one we just created
            LocalDate today = LocalDate.now();
            List<Hearing> futureHearings = hearingRepository
                    .findByCourtCaseAndHearingDateAfterOrderByHearingDateAsc(courtCase, today);
            List<Hearing> toDelete = futureHearings.stream()
                    .filter(h2 -> !h2.getId().equals(next.getId()))
                    .collect(Collectors.toList());
            if (!toDelete.isEmpty()) {
                hearingRepository.deleteAll(toDelete);
            }
        }
        return toHearingDto(hearing);
    }

    public List<DocumentDto> getDocuments(Long caseId) {
        User judge = getCurrentUser();
        Case courtCase = getAuthorizedCase(caseId, judge);
        return documentRepository.findByCourtCase(courtCase).stream().map(this::toDocumentDto).collect(Collectors.toList());
    }

    public List<com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto> listMyCases() {
        User judge = getCurrentUser();
        List<Case> cases = caseRepository.findByJudge_Id(judge.getId());
        return cases.stream().map(this::toCaseDto).collect(Collectors.toList());
    }

    public List<HearingWithCaseDto> listTodaysHearings() {
        User judge = getCurrentUser();
        LocalDate today = LocalDate.now();
        List<Hearing> hearings = hearingRepository.findByHearingDateAndCourtCase_Judge_Id(today, judge.getId());
        // Exclude hearings whose cases are already closed or have a verdict
        return hearings.stream()
                .filter(h -> {
                    Case c = h.getCourtCase();
                    // case must be open and without verdict
                    if (c.getStatus() == CaseStatus.CLOSED || c.getVerdict() != null) return false;
                    // only show SCHEDULED hearings in today's list. Treat null (legacy rows) as SCHEDULED.
                    if (h.getStatus() != null && h.getStatus() != HearingStatus.SCHEDULED) return false;
                    // if a new next hearing has been scheduled for a future date, hide today's hearing
                    return c.getNextHearingDate() == null || !c.getNextHearingDate().isAfter(today);
                })
                .map(h -> {
            HearingWithCaseDto d = new HearingWithCaseDto();
            d.setHearing(toHearingDto(h));
            d.setCourtCase(toCaseDto(h.getCourtCase()));
            return d;
        }).collect(Collectors.toList());
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
