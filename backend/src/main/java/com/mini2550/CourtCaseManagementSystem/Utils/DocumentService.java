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
            // Base directory rooted at the application working directory to avoid container temp dirs
            String base = System.getProperty("user.dir");
            Path caseDir = Paths.get(base, "uploads", "cases", String.valueOf(courtCase.getId()));

            // Ensure directory exists
            Files.createDirectories(caseDir);

            // Sanitize filename (remove any path separators)
            String originalName = file.getOriginalFilename();
            String safeName = (originalName == null ? "upload" : originalName).replace("\\", "_").replace("/", "_");

            Path filePath = caseDir.resolve(safeName);
            // Ensure parent directories for safety
            Files.createDirectories(filePath.getParent());

            // Copy stream (more robust than transferTo across temp/filesystems)
            try (var in = file.getInputStream()) {
                Files.copy(in, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // Save metadata
            Document doc = new Document();
            doc.setCourtCase(courtCase);
            doc.setFileName(safeName);
            doc.setFilePath(filePath.toString());
            doc.setUploaderId(user.getId());
            doc.setNote(note);
            documentRepository.save(doc);

        } catch (Exception e) {
            throw new RuntimeException("Failed to save file to target path. Reason: " + e.getMessage());
        }
    }

}
