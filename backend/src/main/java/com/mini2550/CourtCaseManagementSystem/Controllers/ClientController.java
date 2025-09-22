package com.mini2550.CourtCaseManagementSystem.Controllers;

import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.CaseFileRequestDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.ClientDto;
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
    public ResponseEntity<String> fileCase(@RequestBody CaseFileRequestDto dto) {
        clientService.fileCase(dto);
        return ResponseEntity.ok("Case filed successfully");
    }

    // ✅ View my cases
    @GetMapping("/my-cases")
    public ResponseEntity<List<CaseDto>> getMyCases() {
        return ResponseEntity.ok(clientService.getMyCases());
    }


}