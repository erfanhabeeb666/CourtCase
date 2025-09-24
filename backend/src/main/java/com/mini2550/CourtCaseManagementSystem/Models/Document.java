package com.mini2550.CourtCaseManagementSystem.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;       // Original file name
    private String filePath;       // Path on filesystem
    private String note;           // Optional note
    private Long uploaderId;       // ID of uploader (lawyer/judge)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @ManyToOne
    private Case courtCase;       // Link to the case

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public Case getCourtCase() { return courtCase; }
    public void setCourtCase(Case courtCase) { this.courtCase = courtCase; }
}
