package com.mini2550.CourtCaseManagementSystem.Dtos;

import java.time.LocalDate;

public class VerdictUpdateRequest {
    private String verdict;
    private LocalDate verdictDate; // optional; if null, default to today

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public LocalDate getVerdictDate() {
        return verdictDate;
    }

    public void setVerdictDate(LocalDate verdictDate) {
        this.verdictDate = verdictDate;
    }
}
