package com.mini2550.CourtCaseManagementSystem.Models;

public class Lawyer extends User {
    public String legalIdentity;
    public Lawyer(String legalIdentity) {
        super();
        this.legalIdentity=legalIdentity;
    }

    public String getLegalIdentity() {
        return legalIdentity;
    }

    public void setLegalIdentity(String legalIdentity) {
        this.legalIdentity = legalIdentity;
    }
}
