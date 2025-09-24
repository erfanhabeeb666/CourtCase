package com.mini2550.CourtCaseManagementSystem.Services;

import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.CaseFileRequestDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.UserSummaryDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClientService {
    void fileCase(CaseFileRequestDto dto);
    List<CaseDto> getMyCases();
    List<UserSummaryDto> listLawyers();
}