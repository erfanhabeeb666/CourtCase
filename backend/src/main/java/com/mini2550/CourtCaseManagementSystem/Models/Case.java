package com.mini2550.CourtCaseManagementSystem.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mini2550.CourtCaseManagementSystem.Enums.CaseStatus;
import com.mini2550.CourtCaseManagementSystem.Enums.CaseType;
import com.mini2550.CourtCaseManagementSystem.Enums.Status;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
    @Table(name = "court_case")
    @Entity
    public class Case {
        @Id
        @GeneratedValue(generator = "case-id-generator")
        @GenericGenerator(
                name = "case-id-generator",
                strategy = "com.mini2550.CourtCaseManagementSystem.Utils.CaseIdGenerator"
        )
        @Column(name = "id", unique = true, nullable = false)
        private String id;   // must be String now


        private String title;

        @Enumerated(EnumType.STRING)
        private CaseType Type;

        private String description;

        @ManyToOne
        private Client client;

        @ManyToOne
        @JsonIgnoreProperties({"cases", "assignedCases"})
        private Lawyer clientLawyer;

        @ManyToOne
        private Lawyer opposingLawyer;
        @ManyToOne
        private Judge judge;

        private CaseStatus status;

        private LocalDate nextHearingDate;
        private Status deleteStatus;

        // Verdict information
        private String verdict;
        private LocalDate verdictDate;

        // Hearings associated with this case
        @OneToMany(mappedBy = "courtCase", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonIgnoreProperties({"courtCase"})
        private List<Hearing> hearings = new ArrayList<>();

        public Status getDeleteStatus() {
            return deleteStatus;
        }

        public void setDeleteStatus(Status deleteStatus) {
            this.deleteStatus = deleteStatus;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Client getClient() {
            return client;
        }

        public void setClient(Client client) {
            this.client = client;
        }


        public Judge getJudge() {
            return judge;
        }

        public void setJudge(Judge judge) {
            this.judge = judge;
        }

        public CaseStatus getStatus() {
            return status;
        }

        public void setStatus(CaseStatus status) {
            this.status = status;
        }

        public LocalDate getNextHearingDate() {
            return nextHearingDate;
        }

        public void setNextHearingDate(LocalDate nextHearingDate) {
            this.nextHearingDate = nextHearingDate;
        }

        public CaseType getType() {
            return Type;
        }

        public void setType(CaseType type) {
            Type = type;
        }
        // getters/setters


        public Lawyer getClientLawyer() {
            return clientLawyer;
        }

        public void setClientLawyer(Lawyer clientLawyer) {
            this.clientLawyer = clientLawyer;
        }

        public Lawyer getOpposingLawyer() {
            return opposingLawyer;
        }

        public void setOpposingLawyer(Lawyer opposingLawyer) {
            this.opposingLawyer = opposingLawyer;
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

        public List<Hearing> getHearings() {
            return hearings;
        }

        public void setHearings(List<Hearing> hearings) {
            this.hearings = hearings;
        }
    }

