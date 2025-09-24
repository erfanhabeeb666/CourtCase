package com.mini2550.CourtCaseManagementSystem.Dtos;


public class UserDto {

    private String name;
    private String email;
    private String password;
    private String legalIdentity;

    // Default constructor
    public UserDto() {
    }

    // Constructor with fields
    public UserDto(String name, String email, String password,String legalIdentity) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.legalIdentity=legalIdentity;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLegalIdentity() {
        return legalIdentity;
    }

    public void setLegalIdentity(String legalIdentity) {
        this.legalIdentity = legalIdentity;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
