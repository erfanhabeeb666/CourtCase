package com.mini2550.CourtCaseManagementSystem.Utils;

import com.mini2550.CourtCaseManagementSystem.Dtos.UploadDocumentRequest;
import com.mini2550.CourtCaseManagementSystem.Enums.UserType;
import com.mini2550.CourtCaseManagementSystem.Models.Case;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Repositories.CaseRepository;
import org.springframework.stereotype.Service;

@Service
public class FileUploadUtil {

    private final CaseRepository caseRepository;
    private final DocumentService documentService;

    public FileUploadUtil(CaseRepository caseRepository, DocumentService documentService) {
        this.caseRepository = caseRepository;
        this.documentService = documentService;
    }

    public void uploadDocument(UploadDocumentRequest request, User user) {
        Case courtCase = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (user.getUserType() == UserType.LAWYER) {
            if (!courtCase.getClientLawyer().getId().equals(user.getId())) {
                throw new RuntimeException("Not authorized to upload");
            }
        } else if (user.getUserType() == UserType.JUDGE) {
            if (courtCase.getJudge() == null || !courtCase.getJudge().getId().equals(user.getId())) {
                throw new RuntimeException("Not authorized to upload");
            }
        } else {
            throw new RuntimeException("Not authorized to upload");
        }


        // Save file logic here (e.g., store in filesystem or DB)
        documentService.saveDocument(courtCase, request.getFile(), request.getNote(),user);
    }
}
