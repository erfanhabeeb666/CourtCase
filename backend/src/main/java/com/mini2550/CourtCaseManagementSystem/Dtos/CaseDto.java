package com.mini2550.CourtCaseManagementSystem.Dtos;

import com.mini2550.CourtCaseManagementSystem.Enums.CaseStatus;

import java.time.LocalDate;

public class CaseDto {
    private long id;
    private String title;
    private String type;
    private String clientName;
    private String clientLawyerId;
    private String opposingLawyerId;
    private String clientLawyerName;
    private String opposingLawyerName;
    private String judgeName;
    private CaseStatus status;
    private LocalDate nextHearingDate;
    private String verdict;
    private LocalDate verdictDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientLawyerId() {
        return clientLawyerId;
    }

    public void setClientLawyerId(String clientLawyerId) {
        this.clientLawyerId = clientLawyerId;
    }

    public String getOpposingLawyerId() {
        return opposingLawyerId;
    }

    public void setOpposingLawyerId(String opposingLawyerId) {
        this.opposingLawyerId = opposingLawyerId;
    }

    public String getJudgeName() {
        return judgeName;
    }

    public void setJudgeName(String judgeName) {
        this.judgeName = judgeName;
    }

    public LocalDate getNextHearingDate() {
        return nextHearingDate;
    }

    public void setNextHearingDate(LocalDate nextHearingDate) {
        this.nextHearingDate = nextHearingDate;
    }

    public String getClientLawyerName() {
        return clientLawyerName;
    }

    public void setClientLawyerName(String clientLawyerName) {
        this.clientLawyerName = clientLawyerName;
    }

    public String getOpposingLawyerName() {
        return opposingLawyerName;
    }

    public void setOpposingLawyerName(String opposingLawyerName) {
        this.opposingLawyerName = opposingLawyerName;
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

