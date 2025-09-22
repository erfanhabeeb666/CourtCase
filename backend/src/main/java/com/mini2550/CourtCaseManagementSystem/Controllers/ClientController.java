package com.mini2550.CourtCaseManagementSystem.Controllers;

import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.CaseFileRequestDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.DocumentDto;
import com.mini2550.CourtCaseManagementSystem.Services.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/client")
@PreAuthorize("hasAuthority('CLIENT')")
public class ClientController {

    private final ClientService clientService;
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // ✅ File a new case
    @PostMapping("/file-case")
    public ResponseEntity<String> fileCase(@RequestBody CaseFileRequestDto dto,
                                           Principal principal) {
        clientService.fileCase(dto, principal.getName());
        return ResponseEntity.ok("Case filed successfully");
    }

    // ✅ View my cases
    @GetMapping("/my-cases")
    public ResponseEntity<List<CaseDto>> getMyCases(Principal principal) {
        return ResponseEntity.ok(clientService.getMyCases(principal.getName()));
    }

    // ✅ Upload a document for a case
    @PostMapping("/{caseId}/upload")
    public ResponseEntity<DocumentDto> uploadDocument(@PathVariable Long caseId,
                                                      @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(clientService.uploadDocument(caseId, file));
    }
}