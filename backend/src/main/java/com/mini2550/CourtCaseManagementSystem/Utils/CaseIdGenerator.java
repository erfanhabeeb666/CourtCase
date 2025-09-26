package com.mini2550.CourtCaseManagementSystem.Utils;

import com.mini2550.CourtCaseManagementSystem.Enums.CaseType;
import com.mini2550.CourtCaseManagementSystem.Models.Case;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CaseIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        try {
            Case courtCase = (Case) object;

            // prefix based on case type
            String prefix = courtCase.getType() != null ? courtCase.getType().name() : "CASE";

            final int[] nextVal = {1};

            // Hibernate 6 way → use doWork to access JDBC
            session.doWork(connection -> {
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM court_case")) {
                    if (rs.next()) {
                        nextVal[0] = rs.getInt(1) + 1;
                    }
                }
            });

            return prefix + "-" + String.format("%05d", nextVal[0]); // e.g. CRIMINAL-00001
        } catch (Exception e) {
            throw new RuntimeException("Error generating Case ID", e);
        }
    }
}
