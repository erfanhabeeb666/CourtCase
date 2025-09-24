package com.mini2550.CourtCaseManagementSystem.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mini2550.CourtCaseManagementSystem.Enums.HearingStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hearings")
public class Hearing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "case_id")
    @JsonIgnoreProperties({"hearings"})
    private Case courtCase;

    private LocalDate hearingDate;

    @Column(length = 4000)
    private String judgeSummary;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private HearingStatus status = HearingStatus.SCHEDULED;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Case getCourtCase() {
        return courtCase;
    }

    public void setCourtCase(Case courtCase) {
        this.courtCase = courtCase;
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
