package com.mini2550.CourtCaseManagementSystem.Controllers;

import com.mini2550.CourtCaseManagementSystem.Dtos.UserDto;
import com.mini2550.CourtCaseManagementSystem.Services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
