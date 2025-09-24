package com.mini2550.CourtCaseManagementSystem.Dtos;

import java.time.LocalDate;

public class HearingUpdateRequest {
    private Long hearingId;
    private LocalDate hearingDate; // optional; if null, do not update
    private String judgeSummary;   // optional; if null, do not update
    private LocalDate nextHearingDate; // optional; if provided, schedule next hearing
    private String verdict;        // optional; if provided, close the case with this verdict
    private LocalDate verdictDate; // optional; if null while verdict provided, default to today

    public Long getHearingId() {
        return hearingId;
    }

    public void setHearingId(Long hearingId) {
        this.hearingId = hearingId;
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

    public LocalDate getNextHearingDate() {
        return nextHearingDate;
    }

    public void setNextHearingDate(LocalDate nextHearingDate) {
        this.nextHearingDate = nextHearingDate;
    }

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
