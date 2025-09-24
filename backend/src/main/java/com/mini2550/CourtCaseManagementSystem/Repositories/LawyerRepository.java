package com.mini2550.CourtCaseManagementSystem.Repositories;

import com.mini2550.CourtCaseManagementSystem.Models.Lawyer;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LawyerRepository extends JpaRepository<Lawyer,Long> {
}
