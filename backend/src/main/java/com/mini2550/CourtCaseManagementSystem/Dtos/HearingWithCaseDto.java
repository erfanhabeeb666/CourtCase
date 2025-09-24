package com.mini2550.CourtCaseManagementSystem.Dtos;

public class HearingWithCaseDto {
    private HearingDto hearing;
    private CaseDto courtCase;

    public HearingWithCaseDto() {}

    public HearingWithCaseDto(HearingDto hearing, CaseDto courtCase) {
        this.hearing = hearing;
        this.courtCase = courtCase;
    }

    public HearingDto getHearing() {
        return hearing;
    }

    public void setHearing(HearingDto hearing) {
        this.hearing = hearing;
    }

    public CaseDto getCourtCase() {
        return courtCase;
    }

    public void setCourtCase(CaseDto courtCase) {
        this.courtCase = courtCase;
    }
}
