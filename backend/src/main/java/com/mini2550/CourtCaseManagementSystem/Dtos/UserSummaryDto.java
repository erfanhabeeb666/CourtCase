package com.mini2550.CourtCaseManagementSystem.Dtos;

import com.mini2550.CourtCaseManagementSystem.Enums.Status;

public class UserSummaryDto {
    private Long id;
    private String name;
    private String email;
    private String userType;
    private Status status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
