package com.mini2550.CourtCaseManagementSystem.Controllers;

import com.mini2550.CourtCaseManagementSystem.Dtos.HearingDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.UploadDocumentRequest;
import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Services.LawyerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lawyer")
@PreAuthorize("hasAuthority('LAWYER')")
public class LawyerController {

    private final LawyerService lawyerService;

    public LawyerController(LawyerService lawyerService) {
        this.lawyerService = lawyerService;
    }

    @PostMapping("/cases/upload-document")
    public void uploadDocument(@ModelAttribute UploadDocumentRequest request, @AuthenticationPrincipal User user) {
        lawyerService.uploadDocument(request);
    }

    @GetMapping("/my-cases")
    public ResponseEntity<List<CaseDto>> myCases() {
        return ResponseEntity.ok(lawyerService.listMyCases());
    }

    @GetMapping("/cases/{caseId}")
    public ResponseEntity<CaseDto> getCase(@PathVariable Long caseId) {
        return ResponseEntity.ok(lawyerService.getCase(caseId));
    }

    @GetMapping("/cases/{caseId}/hearings")
    public ResponseEntity<List<HearingDto>> getHearings(@PathVariable Long caseId) {
        return ResponseEntity.ok(lawyerService.getHearings(caseId));
    }
}
