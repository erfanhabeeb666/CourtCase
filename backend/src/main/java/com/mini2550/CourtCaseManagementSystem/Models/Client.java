package com.mini2550.CourtCaseManagementSystem.Models;

import jakarta.persistence.*;

@DiscriminatorValue("CLIENT")
@Entity
public class Client extends User {
    private String legalIdentity;
    public Client() {
        super();
    }

    public String getLegalIdentity() {
        return legalIdentity;
    }

    public void setLegalIdentity(String legalIdentity) {
        this.legalIdentity = legalIdentity;
    }
}
