package com.mini2550.CourtCaseManagementSystem.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mini2550.CourtCaseManagementSystem.Enums.CaseStatus;
import com.mini2550.CourtCaseManagementSystem.Enums.CaseType;
import com.mini2550.CourtCaseManagementSystem.Enums.Status;
import jakarta.persistence.*;
import java.time.LocalDate;
    @Table(name = "CourtCase")
    @Entity
    public class Case {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

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

        public Status getDeleteStatus() {
            return deleteStatus;
        }

        public void setDeleteStatus(Status deleteStatus) {
            this.deleteStatus = deleteStatus;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
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


        public User getJudge() {
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
    }
