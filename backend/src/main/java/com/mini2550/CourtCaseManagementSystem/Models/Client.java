package com.mini2550.CourtCaseManagementSystem.Models;

import jakarta.persistence.*;

import java.util.List;

@DiscriminatorValue("CLIENT")
@Entity
public class Client extends User {

    public Client() {
        super();
    }
}
