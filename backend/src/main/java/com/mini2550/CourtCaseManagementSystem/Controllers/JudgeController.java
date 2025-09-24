package com.mini2550.CourtCaseManagementSystem.Controllers;

import com.mini2550.CourtCaseManagementSystem.Dtos.UploadDocumentRequest;
import com.mini2550.CourtCaseManagementSystem.Models.Judge;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Services.JudgeService;
import com.mini2550.CourtCaseManagementSystem.Utils.FileUploadUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/judge")
public class JudgeController {

    private final JudgeService judgeService;
    public JudgeController(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    @PostMapping("/cases/upload-document")
    public void uploadDocument(@ModelAttribute UploadDocumentRequest request) {
        judgeService.uploadDocument(request);
    }
    @PutMapping("/cases/update-hearing")
    public void updateHearing(@RequestBody HearingUpdateRequest request) {
        caseService.updateHearing(request, judge);
    }
}
