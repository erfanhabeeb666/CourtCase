package com.mini2550.CourtCaseManagementSystem.Dtos;

import java.time.LocalDate;

public class HearingCreateRequest {
    private LocalDate hearingDate;
    private String judgeSummary;

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
}
