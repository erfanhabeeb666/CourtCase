package com.mini2550.CourtCaseManagementSystem.Controllers;

import com.mini2550.CourtCaseManagementSystem.Enums.UserType;
import com.mini2550.CourtCaseManagementSystem.Models.Case;
import com.mini2550.CourtCaseManagementSystem.Models.Document;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Repositories.DocumentRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.CaseRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.UserRepository;
import com.mini2550.CourtCaseManagementSystem.Security.JwtService;
import com.mini2550.CourtCaseManagementSystem.Security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final JwtUtils jwtUtils;
    private final HttpServletRequest request;

    public DocumentController(DocumentRepository documentRepository, CaseRepository caseRepository, UserRepository userRepository, JwtService jwtService, JwtUtils jwtUtils, HttpServletRequest request) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.jwtUtils = jwtUtils;
        this.request = request;
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long documentId) {
        Document doc = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found"));
        Case courtCase = caseRepository.findById(doc.getCourtCase().getId()).orElseThrow(() -> new RuntimeException("Case not found"));

        // Authorization: judge assigned to case, either lawyer on case, or the client
        User current = getCurrentUser();
        boolean allowed = false;
        if (current.getUserType() == UserType.JUDGE) {
            allowed = (courtCase.getJudge() != null && courtCase.getJudge().getId().equals(current.getId()));
        } else if (current.getUserType() == UserType.LAWYER) {
            boolean isClientLawyer = (courtCase.getClientLawyer() != null && courtCase.getClientLawyer().getId().equals(current.getId()));
            boolean isOpposingLawyer = (courtCase.getOpposingLawyer() != null && courtCase.getOpposingLawyer().getId().equals(current.getId()));
            allowed = isClientLawyer || isOpposingLawyer;
        } else if (current.getUserType() == UserType.CLIENT) {
            allowed = (courtCase.getClient() != null && courtCase.getClient().getId().equals(current.getId()));
        }
        if (!allowed) throw new RuntimeException("Not authorized to download this document");

        Path path = Path.of(doc.getFilePath());
        if (!Files.exists(path)) throw new RuntimeException("File not found on server");
        FileSystemResource resource = new FileSystemResource(path);

        String fileName = doc.getFileName() != null ? doc.getFileName() : path.getFileName().toString();
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        try {
            String probe = Files.probeContentType(path);
            if (probe != null) contentType = probe;
        } catch (Exception ignored) {}

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    private User getCurrentUser() {
        Long userId = Long.valueOf(jwtService.extractId(jwtUtils.getJwtFromRequest(request)));
        return userRepository.findById(userId).orElseThrow();
    }
}
