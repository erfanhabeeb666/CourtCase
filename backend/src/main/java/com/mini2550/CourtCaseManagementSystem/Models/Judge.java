package com.mini2550.CourtCaseManagementSystem.Models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
@DiscriminatorValue("JUDGE")
@Entity
public class Judge extends User {
    public Judge(){
        super();
    }
}
