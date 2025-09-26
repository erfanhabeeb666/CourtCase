package com.mini2550.CourtCaseManagementSystem.Controllers;

import com.mini2550.CourtCaseManagementSystem.Dtos.UploadDocumentRequest;
import com.mini2550.CourtCaseManagementSystem.Dtos.HearingDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.DocumentDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.HearingUpdateRequest;
import com.mini2550.CourtCaseManagementSystem.Dtos.HearingWithCaseDto;
import com.mini2550.CourtCaseManagementSystem.Services.JudgeService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/judge")
@PreAuthorize("hasAuthority('JUDGE')")
public class JudgeController {

    private final JudgeService judgeService;
    public JudgeController(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    @PostMapping("/cases/upload-document")
    public void uploadDocument(@ModelAttribute UploadDocumentRequest request) {
        judgeService.uploadDocument(request);
    }

    @GetMapping("/cases/{caseId}/hearings")
    public ResponseEntity<List<HearingDto>> getHearings(@PathVariable String caseId) {
        return ResponseEntity.ok(judgeService.getHearings(caseId));
    }

    // Unified update hearing API: update hearing details, schedule next, or submit verdict
    @PutMapping("/cases/{caseId}/hearings/{hearingId}")
    public ResponseEntity<HearingDto> updateHearing(
            @PathVariable String caseId,
            @PathVariable Long hearingId,
            @RequestBody HearingUpdateRequest request) {
        return ResponseEntity.ok(judgeService.updateHearing(caseId, hearingId, request));
    }

    @GetMapping("/cases/{caseId}/documents")
    public ResponseEntity<List<DocumentDto>> getDocuments(@PathVariable String caseId) {
        return ResponseEntity.ok(judgeService.getDocuments(caseId));
    }

    @GetMapping("/hearings/today")
    public ResponseEntity<List<HearingWithCaseDto>> listTodaysHearings() {
        return ResponseEntity.ok(judgeService.listTodaysHearings());
    }

    @GetMapping("/my-cases")
    public ResponseEntity<List<com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto>> myCases() {
        return ResponseEntity.ok(judgeService.listMyCases());
    }
}
