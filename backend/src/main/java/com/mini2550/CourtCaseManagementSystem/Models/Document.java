package com.mini2550.CourtCaseManagementSystem.Models;

import jakarta.persistence.*;

@Entity
    public class Document {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String fileName;
        private String filePath;

        @ManyToOne
        private Case caseRef;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public Case getCaseRef() {
            return caseRef;
        }

        public void setCaseRef(Case caseRef) {
            this.caseRef = caseRef;
        }
    }