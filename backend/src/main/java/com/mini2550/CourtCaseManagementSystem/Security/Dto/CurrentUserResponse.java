package com.mini2550.CourtCaseManagementSystem.Security.Dto;

public class CurrentUserResponse {
    private Long id;
    private String name;
    private String email;
    private String userType; // ADMIN, JUDGE, LAWYER, CLIENT

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}
