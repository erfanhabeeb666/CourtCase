package com.mini2550.CourtCaseManagementSystem.Dtos;

import java.time.LocalDate;

public class NextHearingRequest {
    private LocalDate nextHearingDate;
    private String judgeSummary; // optional

    public LocalDate getNextHearingDate() {
        return nextHearingDate;
    }

    public void setNextHearingDate(LocalDate nextHearingDate) {
        this.nextHearingDate = nextHearingDate;
    }

    public String getJudgeSummary() {
        return judgeSummary;
    }

    public void setJudgeSummary(String judgeSummary) {
        this.judgeSummary = judgeSummary;
    }
}
