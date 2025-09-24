package com.mini2550.CourtCaseManagementSystem.Controllers;

import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.CaseFileRequestDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.UserSummaryDto;
import com.mini2550.CourtCaseManagementSystem.Services.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client")
@PreAuthorize("hasAuthority('CLIENT')")
public class ClientController {

    private final ClientService clientService;
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/file-case")
    public ResponseEntity<String> fileCase(@RequestBody CaseFileRequestDto dto) {
        clientService.fileCase(dto);
        return ResponseEntity.ok("Case filed successfully");
    }

    @GetMapping("/my-cases")
    public ResponseEntity<List<CaseDto>> getMyCases() {
        return ResponseEntity.ok(clientService.getMyCases());
    }

    @GetMapping("/lawyers")
    public ResponseEntity<List<UserSummaryDto>> listLawyers() {
        return ResponseEntity.ok(clientService.listLawyers());
    }


}