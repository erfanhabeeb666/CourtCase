package com.mini2550.CourtCaseManagementSystem.Models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
@DiscriminatorValue("ADMIN")
@Entity
public class Admin extends User {


    Admin() {
        super();
    }
}
