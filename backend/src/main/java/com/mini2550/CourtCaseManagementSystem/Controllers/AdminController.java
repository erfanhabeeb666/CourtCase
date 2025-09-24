package com.mini2550.CourtCaseManagementSystem.Controllers;

import com.mini2550.CourtCaseManagementSystem.Dtos.AssignCaseRequest;
import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.UserDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.UserSummaryDto;
import com.mini2550.CourtCaseManagementSystem.Enums.Status;
import com.mini2550.CourtCaseManagementSystem.Enums.UserType;
import com.mini2550.CourtCaseManagementSystem.Services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    // List all cases for admin (to populate dropdowns in UI)
    @GetMapping("/cases")
    public ResponseEntity<List<CaseDto>> listCases() {
        return ResponseEntity.ok(adminService.listCases());
    }

    // User management
    @GetMapping("/users")
    public ResponseEntity<java.util.List<UserSummaryDto>> listUsers(@RequestParam(value = "role", required = false) UserType role) {
        return ResponseEntity.ok(adminService.listUsers(role));
    }

    // Convenience endpoints to list judges and lawyers
    @GetMapping("/judges")
    public ResponseEntity<List<UserSummaryDto>> listJudges() {
        return ResponseEntity.ok(adminService.listUsers(UserType.JUDGE));
    }

    @GetMapping("/lawyers")
    public ResponseEntity<List<UserSummaryDto>> listLawyers() {
        return ResponseEntity.ok(adminService.listUsers(UserType.LAWYER));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserSummaryDto> updateUserStatus(@PathVariable Long userId, @RequestParam("status") Status status) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, status));
    }
}
