package com.mini2550.CourtCaseManagementSystem.Dtos;

import com.mini2550.CourtCaseManagementSystem.Enums.HearingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class HearingDto {
    private Long id;
    private LocalDate hearingDate;
    private String judgeSummary;
    private LocalDateTime createdAt;
    private HearingStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getHearingDate() {
        return hearingDate;
    }

    public void setHearingDate(LocalDate hearingDate) {
        this.hearingDate = hearingDate;
    }

    public String getJudgeSummary() {
        return judgeSummary;
    }

    public void setJudgeSummary(String judgeSummary) {
        this.judgeSummary = judgeSummary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public HearingStatus getStatus() {
        return status;
    }

    public void setStatus(HearingStatus status) {
        this.status = status;
    }
}
