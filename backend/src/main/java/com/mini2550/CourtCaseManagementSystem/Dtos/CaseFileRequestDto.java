package com.mini2550.CourtCaseManagementSystem.Dtos;

import com.mini2550.CourtCaseManagementSystem.Enums.CaseType;

public class CaseFileRequestDto {
    private String title;
    private CaseType type;
    private String description;
    private String clientLawyerId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CaseType getType() {
        return type;
    }

    public void setType(CaseType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClientLawyerId() {
        return clientLawyerId;
    }

    public void setClientLawyerId(String clientLawyerId) {
        this.clientLawyerId = clientLawyerId;
    }
}
