package com.mini2550.CourtCaseManagementSystem.Repositories;

import com.mini2550.CourtCaseManagementSystem.Models.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}