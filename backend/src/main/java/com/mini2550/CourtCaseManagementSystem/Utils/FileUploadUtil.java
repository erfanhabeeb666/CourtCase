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
            boolean isClientLawyer = courtCase.getClientLawyer() != null && courtCase.getClientLawyer().getId().equals(user.getId());
            boolean isOpposingLawyer = courtCase.getOpposingLawyer() != null && courtCase.getOpposingLawyer().getId().equals(user.getId());
            if (!isClientLawyer && !isOpposingLawyer) throw new RuntimeException("Not authorized to upload");
        } else if (user.getUserType() == UserType.JUDGE) {
            if (courtCase.getJudge() == null || !courtCase.getJudge().getId().equals(user.getId())) {
                throw new RuntimeException("Not authorized to upload");
            }
        } else if (user.getUserType() == UserType.CLIENT) {
            if (courtCase.getClient() == null || !courtCase.getClient().getId().equals(user.getId())) {
                throw new RuntimeException("Not authorized to upload");
            }
        } else {
            throw new RuntimeException("Not authorized to upload");
        }

        // Save file(s)
        if (request.getFiles() != null && request.getFiles().length > 0) {
            for (var f : request.getFiles()) {
                if (f != null && !f.isEmpty()) {
                    documentService.saveDocument(courtCase, f, request.getNote(), user);
                }
            }
        } else if (request.getFile() != null && !request.getFile().isEmpty()) {
            documentService.saveDocument(courtCase, request.getFile(), request.getNote(), user);
        } else {
            throw new RuntimeException("No file provided");
        }
    }
}
