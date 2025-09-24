package com.mini2550.CourtCaseManagementSystem.Repositories;

import com.mini2550.CourtCaseManagementSystem.Models.Case;
import com.mini2550.CourtCaseManagementSystem.Models.Hearing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface HearingRepository extends JpaRepository<Hearing,Long> {
    List<Hearing> findByCourtCaseOrderByHearingDateAsc(Case courtCase);
    boolean existsByCourtCaseAndHearingDate(Case courtCase, LocalDate hearingDate);
}
