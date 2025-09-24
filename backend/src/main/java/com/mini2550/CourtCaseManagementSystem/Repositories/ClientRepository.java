package com.mini2550.CourtCaseManagementSystem.Repositories;

import com.mini2550.CourtCaseManagementSystem.Models.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client,Long> {

}
