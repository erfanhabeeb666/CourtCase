package com.mini2550.CourtCaseManagementSystem.Dtos;

import org.springframework.web.multipart.MultipartFile;

public class UploadDocumentRequest {
    private String caseId;
    private MultipartFile file; // file uploaded
    private String note;        // optional note for context
    // Support multiple files as well; either 'file' or 'files' may be used by clients
    private MultipartFile[] files;

    // Getters & Setters
    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public MultipartFile[] getFiles() { return files; }
    public void setFiles(MultipartFile[] files) { this.files = files; }
}
