package com.mini2550.CourtCaseManagementSystem.Models;

import com.mini2550.CourtCaseManagementSystem.Enums.CaseStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
    @Table(name = "CourtCase")
    @Entity
    public class Case {
        @Id
        @GeneratedValue
        private Long id;

        private String title;
        private String Type;

        private String description;

        @OneToOne
        private User client;

        @ManyToOne
        private User clientLawyer;
        @ManyToOne
        private User opposingLawyer;
        @ManyToOne
        private User judge;

        private CaseStatus status;

        private LocalDate nextHearingDate;

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

        public User getClient() {
            return client;
        }

        public void setClient(User client) {
            this.client = client;
        }


        public User getJudge() {
            return judge;
        }

        public void setJudge(User judge) {
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

        public String getType() {
            return Type;
        }

        public void setType(String type) {
            Type = type;
        }
        // getters/setters


        public User getClientLawyer() {
            return clientLawyer;
        }

        public void setClientLawyer(User clientLawyer) {
            this.clientLawyer = clientLawyer;
        }

        public User getOpposingLawyer() {
            return opposingLawyer;
        }

        public void setOpposingLawyer(User opposingLawyer) {
            this.opposingLawyer = opposingLawyer;
        }
    }
