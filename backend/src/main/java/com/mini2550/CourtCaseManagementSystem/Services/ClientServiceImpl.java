package com.mini2550.CourtCaseManagementSystem.Services;
 
 import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
 import com.mini2550.CourtCaseManagementSystem.Dtos.CaseFileRequestDto;
 import com.mini2550.CourtCaseManagementSystem.Dtos.DocumentDto;
 import com.mini2550.CourtCaseManagementSystem.Dtos.UserSummaryDto;
 import com.mini2550.CourtCaseManagementSystem.Dtos.UploadDocumentRequest;
 import com.mini2550.CourtCaseManagementSystem.Enums.CaseStatus;
 import com.mini2550.CourtCaseManagementSystem.Enums.Status;
 import com.mini2550.CourtCaseManagementSystem.Models.Case;
 import com.mini2550.CourtCaseManagementSystem.Models.Client;
 import com.mini2550.CourtCaseManagementSystem.Models.Lawyer;
 import com.mini2550.CourtCaseManagementSystem.Models.User;
 import com.mini2550.CourtCaseManagementSystem.Repositories.CaseRepository;
 import com.mini2550.CourtCaseManagementSystem.Repositories.ClientRepository;
 import com.mini2550.CourtCaseManagementSystem.Repositories.LawyerRepository;
 import com.mini2550.CourtCaseManagementSystem.Repositories.DocumentRepository;
 import com.mini2550.CourtCaseManagementSystem.Repositories.UserRepository;
 import com.mini2550.CourtCaseManagementSystem.Security.JwtService;
 import com.mini2550.CourtCaseManagementSystem.Security.JwtUtils;
 import com.mini2550.CourtCaseManagementSystem.Utils.FileUploadUtil;
 import jakarta.servlet.http.HttpServletRequest;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 import java.util.Optional;
 import java.util.stream.Collectors;
 
 @Service
 public class ClientServiceImpl implements ClientService {
     private final CaseRepository caseRepository;
     private final UserRepository userRepository;
     private final JwtService jwtService;
     private final HttpServletRequest request;
     private final JwtUtils jwtUtils;
     private final LawyerRepository lawyerRepository;
     private final FileUploadUtil fileUploadUtil;
     private final DocumentRepository documentRepository;
     private final ClientRepository clientRepository;
 
     public ClientServiceImpl(CaseRepository caseRepository,
                             UserRepository userRepository,
                             JwtService jwtService,
                             HttpServletRequest request,
                             JwtUtils jwtUtils,
                             ClientRepository clientRepository,
                             LawyerRepository lawyerRepository,
                             FileUploadUtil fileUploadUtil,
                             DocumentRepository documentRepository) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.request = request;
        this.jwtUtils = jwtUtils;
        this.clientRepository = clientRepository;
        this.lawyerRepository = lawyerRepository;
        this.fileUploadUtil = fileUploadUtil;
        this.documentRepository = documentRepository;
    }
     @Override
     public void fileCase(CaseFileRequestDto dto) {
         Long clientId = Long.valueOf(jwtService.extractId(jwtUtils.getJwtFromRequest(request)));
         Client client = clientRepository.findById(clientId).orElseThrow();
         Case c = new Case();
         c.setTitle(dto.getTitle());
         c.setType(dto.getType());
         c.setDescription(dto.getDescription());
         c.setClient(client);
         c.setStatus(CaseStatus.NEW);
         c.setDeleteStatus(Status.ACTIVE);
         if(!dto.getClientLawyerId().isEmpty()){
             Optional<Lawyer> optionalUser = lawyerRepository.findById(Long.valueOf(dto.getClientLawyerId()));
             if(optionalUser.isPresent()){
                 c.setClientLawyer(optionalUser.get());
             }else{
                 throw new RuntimeException("the specieified client lwayer id is not present in the db");
             }
         }
         caseRepository.save(c);
     }
 
     @Override
     public List<CaseDto> getMyCases() {
         Long clientId = Long.valueOf(jwtService.extractId(jwtUtils.getJwtFromRequest(request)));
         User client = userRepository.findById(clientId).orElseThrow();
 
         return caseRepository.findByClient(client).stream().map(c -> {
             CaseDto dto = new CaseDto();
             dto.setId(c.getId());
             dto.setTitle(c.getTitle());
             dto.setType(c.getType().name());
             dto.setStatus(c.getStatus());
             dto.setClientName(c.getClient().getName());
             dto.setClientLawyerName(c.getClientLawyer() != null ? c.getClientLawyer().getName() : null);
             dto.setOpposingLawyerName(c.getOpposingLawyer() != null ? c.getOpposingLawyer().getName() : null);
             dto.setJudgeName(c.getJudge() != null ? c.getJudge().getName() : null);
             dto.setNextHearingDate(c.getNextHearingDate());
             dto.setVerdict(c.getVerdict());
             dto.setVerdictDate(c.getVerdictDate());
             return dto;
         }).collect(Collectors.toList());
     }
 
     @Override
     public List<UserSummaryDto> listLawyers() {
         return lawyerRepository.findAll().stream().map(lawyer -> {
             UserSummaryDto dto = new UserSummaryDto();
             dto.setId(lawyer.getId());
             dto.setName(lawyer.getName());
             dto.setEmail(lawyer.getEmail());
             dto.setUserType(lawyer.getUserType() != null ? lawyer.getUserType().name() : "LAWYER");
             dto.setStatus(lawyer.getStatus());
             return dto;
         }).collect(Collectors.toList());
     }
 
 
     @Override
     public void uploadDocument(UploadDocumentRequest uploadRequest) {
         Long userId = Long.valueOf(jwtService.extractId(jwtUtils.getJwtFromRequest(request)));
         User user = userRepository.findById(userId).orElseThrow();
         fileUploadUtil.uploadDocument(uploadRequest, user);
     }

     @Override
     public List<DocumentDto> getDocuments(Long caseId) {
         Long clientId = Long.valueOf(jwtService.extractId(jwtUtils.getJwtFromRequest(request)));
         User client = userRepository.findById(clientId).orElseThrow();
         Case c = caseRepository.findById(caseId).orElseThrow(() -> new RuntimeException("Case not found"));
         if (c.getClient() == null || !c.getClient().getId().equals(client.getId())) {
             throw new RuntimeException("Not authorized for this case");
         }
         return documentRepository.findByCourtCaseOrderByUploadedAtDesc(c)
                 .stream().map(this::toDocumentDto).collect(Collectors.toList());
     }

     private DocumentDto toDocumentDto(com.mini2550.CourtCaseManagementSystem.Models.Document d) {
         DocumentDto dto = new DocumentDto();
         dto.setId(d.getId());
         dto.setFileName(d.getFileName());
         dto.setFilePath(d.getFilePath());
         dto.setUploaderId(d.getUploaderId());
         dto.setNote(d.getNote());
         dto.setUploadedAt(d.getUploadedAt());
         return dto;
     }
}
