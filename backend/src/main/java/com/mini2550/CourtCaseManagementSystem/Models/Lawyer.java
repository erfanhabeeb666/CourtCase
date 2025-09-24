package com.mini2550.CourtCaseManagementSystem.Models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
@DiscriminatorValue("LAWYER")
@Entity
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
