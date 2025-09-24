package com.mini2550.CourtCaseManagementSystem.Dtos;

import org.springframework.web.multipart.MultipartFile;

public class UploadDocumentRequest {
    private Long caseId;
    private MultipartFile file; // file uploaded
    private String note;        // optional note for context

    // Getters & Setters
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
