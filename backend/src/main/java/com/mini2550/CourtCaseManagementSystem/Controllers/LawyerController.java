package com.mini2550.CourtCaseManagementSystem.Controllers;

import com.mini2550.CourtCaseManagementSystem.Dtos.UploadDocumentRequest;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Services.LawyerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
