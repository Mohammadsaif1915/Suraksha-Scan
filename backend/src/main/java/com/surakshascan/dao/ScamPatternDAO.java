package com.surakshascan.dao;

import com.surakshascan.model.ScamPattern;
import com.surakshascan.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScamPatternDAO {
    // Only returns ACTIVE patterns — scanner must ignore disabled rules
    public List<ScamPattern> findByCategory(String category) {
        List<ScamPattern> patterns = new ArrayList<>();
        String sql = "SELECT * FROM scam_patterns WHERE category = ? AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ScamPattern p = new ScamPattern();
                    p.setPatternId(rs.getInt("pattern_id"));
                    p.setCategory(rs.getString("category"));
                    p.setPatternText(rs.getString("pattern_text"));
                    p.setPatternType(rs.getString("pattern_type"));
                    p.setRiskWeight(rs.getInt("risk_weight"));
                    p.setDescription(rs.getString("description"));
                    p.setActive(true);
                    patterns.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patterns;
    }
}
