package com.mini2550.CourtCaseManagementSystem.Controllers;

import com.mini2550.CourtCaseManagementSystem.Dtos.AssignCaseRequest;
import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.UserDto;
import com.mini2550.CourtCaseManagementSystem.Services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/add-judge")
    public ResponseEntity<String> addJudge(@RequestBody UserDto user){
        adminService.addJudge(user);
        return ResponseEntity.ok("judge created succesfully");
    }
    @PutMapping("/cases/{caseId}/assign")
    public CaseDto assignCase(@PathVariable Long caseId, @RequestBody AssignCaseRequest request) {
        return adminService.assignCaseByAdmin(caseId, request);
    }
}
