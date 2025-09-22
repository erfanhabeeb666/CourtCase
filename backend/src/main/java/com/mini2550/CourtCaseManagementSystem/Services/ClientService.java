package com.mini2550.CourtCaseManagementSystem.Services;

import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.CaseFileRequestDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.DocumentDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClientService {
    void fileCase(CaseFileRequestDto dto, String clientEmail);
    List<CaseDto> getMyCases(String clientEmail);
    DocumentDto uploadDocument(Long caseId, MultipartFile file);
}