package com.mini2550.CourtCaseManagementSystem.Utils;
import com.mini2550.CourtCaseManagementSystem.Models.Case;
import com.mini2550.CourtCaseManagementSystem.Models.Document;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Repositories.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@Service
public class DocumentService {
    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public void saveDocument(Case courtCase, MultipartFile file, String note, User user) {
        try {
            Path caseDir = Paths.get("uploads/cases/" + courtCase.getId());
            if (!Files.exists(caseDir)) {
                Files.createDirectories(caseDir);
            }

            Path filePath = caseDir.resolve(file.getOriginalFilename());
            file.transferTo(filePath.toFile());

            // Optionally: save file metadata in DB (filename, uploader, timestamp, note)
            Document doc = new Document();
            doc.setCourtCase(courtCase);
            doc.setFilePath(filePath.toString());
            doc.setUploaderId(user.getId());
            doc.setNote(note);
            documentRepository.save(doc);

        } catch (Exception e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }
    }

}
