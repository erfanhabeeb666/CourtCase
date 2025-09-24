package com.mini2550.CourtCaseManagementSystem.Dtos;

import java.time.LocalDate;

public class AssignCaseRequest {

    private Long opposingLawyerId;
    private Long judgeId;
    private LocalDate nextHearingDate;

    // Getters and Setters
    public Long getOpposingLawyerId() {
        return opposingLawyerId;
    }

    public void setOpposingLawyerId(Long opposingLawyerId) {
        this.opposingLawyerId = opposingLawyerId;
    }

    public Long getJudgeId() {
        return judgeId;
    }

    public void setJudgeId(Long judgeId) {
        this.judgeId = judgeId;
    }

    public LocalDate getNextHearingDate() {
        return nextHearingDate;
    }

    public void setNextHearingDate(LocalDate nextHearingDate) {
        this.nextHearingDate = nextHearingDate;
    }
}
