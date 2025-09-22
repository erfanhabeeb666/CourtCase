package com.mini2550.CourtCaseManagementSystem.Services;

import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.CaseFileRequestDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.DocumentDto;
import com.mini2550.CourtCaseManagementSystem.Enums.CaseStatus;
import com.mini2550.CourtCaseManagementSystem.Models.Case;
import com.mini2550.CourtCaseManagementSystem.Models.Document;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Repositories.CaseRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.DocumentRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.UserRepository;
import com.mini2550.CourtCaseManagementSystem.Security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    private final CaseRepository caseRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public ClientServiceImpl(CaseRepository caseRepository,
                             DocumentRepository documentRepository,
                             UserRepository userRepository,
                             JwtService jwtService) {
        this.caseRepository = caseRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void fileCase(CaseFileRequestDto dto, String token) {
        // ✅ Get user by ID from JWT
        Long clientId = Long.valueOf(jwtService.extractId(token));
        User client = userRepository.findById(clientId).orElseThrow();

        Case c = new Case();
        c.setTitle(dto.getTitle());
        c.setType(dto.getType());
        c.setDescription(dto.getDescription());
        c.setClient(client);
        c.setStatus(CaseStatus.NEW);

        caseRepository.save(c);
    }

    @Override
    public List<CaseDto> getMyCases(String token) {
        Long clientId = Long.valueOf(jwtService.extractId(token));
        User client = userRepository.findById(clientId).orElseThrow();

        return caseRepository.findByClient(client).stream().map(c -> {
            CaseDto dto = new CaseDto();
            dto.setId(c.getId());
            dto.setTitle(c.getTitle());
            dto.setType(c.getType());
            dto.setStatus(c.getStatus());
            dto.setClientName(c.getClient().getName());
            dto.setLawyerName(c.getLawyer() != null ? c.getLawyer().getName() : null);
            dto.setJudgeName(c.getJudge() != null ? c.getJudge().getName() : null);
            dto.setNextHearingDate(c.getNextHearingDate());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public DocumentDto uploadDocument(Long caseId, MultipartFile file) {
        try {
            String uploadDir = "uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String path = uploadDir + file.getOriginalFilename();
            file.transferTo(new File(path));

            Document doc = new Document();
            doc.setFileName(file.getOriginalFilename());
            doc.setFilePath(path);
            doc.setCaseRef(caseRepository.findById(caseId).orElseThrow()); // ✅ use relation

            Document saved = documentRepository.save(doc);

            DocumentDto dto = new DocumentDto();
            dto.setId(saved.getId());
            dto.setFileName(saved.getFileName());
            dto.setDownloadUrl("/client/documents/" + saved.getId());
            return dto;

        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }
    }
}

