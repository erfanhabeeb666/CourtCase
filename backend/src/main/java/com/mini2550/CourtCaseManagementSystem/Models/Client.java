package com.mini2550.CourtCaseManagementSystem.Models;

import jakarta.persistence.*;

@DiscriminatorValue("CLIENT")
@Entity
public class Client extends User {

    public Client() {
        super();
    }
}
