package com.surakshascan.dao;

import com.surakshascan.model.ScamPattern;
import com.surakshascan.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminPatternDAO {

    public List<ScamPattern> findAll() {
        List<ScamPattern> list = new ArrayList<>();
        String sql = "SELECT * FROM scam_patterns ORDER BY category, pattern_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(extract(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public ScamPattern findById(int id) {
        String sql = "SELECT * FROM scam_patterns WHERE pattern_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return extract(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int create(Connection conn, ScamPattern p) throws SQLException {
        String sql = "INSERT INTO scam_patterns (category, pattern_text, pattern_type, risk_weight, description, is_active) VALUES (?,?,?,?,?,TRUE)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getCategory());
            stmt.setString(2, p.getPatternText());
            stmt.setString(3, p.getPatternType());
            stmt.setInt(4, p.getRiskWeight());
            stmt.setString(5, p.getDescription());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void update(Connection conn, ScamPattern p) throws SQLException {
        String sql = "UPDATE scam_patterns SET category=?, pattern_text=?, pattern_type=?, risk_weight=?, description=?, is_active=? WHERE pattern_id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getCategory());
            stmt.setString(2, p.getPatternText());
            stmt.setString(3, p.getPatternType());
            stmt.setInt(4, p.getRiskWeight());
            stmt.setString(5, p.getDescription());
            stmt.setBoolean(6, p.isActive());
            stmt.setInt(7, p.getPatternId());
            stmt.executeUpdate();
        }
    }

    public void setActive(Connection conn, int id, boolean active) throws SQLException {
        String sql = "UPDATE scam_patterns SET is_active=? WHERE pattern_id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    private ScamPattern extract(ResultSet rs) throws SQLException {
        ScamPattern p = new ScamPattern();
        p.setPatternId(rs.getInt("pattern_id"));
        p.setCategory(rs.getString("category"));
        p.setPatternText(rs.getString("pattern_text"));
        p.setPatternType(rs.getString("pattern_type"));
        p.setRiskWeight(rs.getInt("risk_weight"));
        p.setDescription(rs.getString("description"));
        try { p.setActive(rs.getBoolean("is_active")); } catch (SQLException ignored) { p.setActive(true); }
        return p;
    }
}

/* Also update ScamPatternDAO.findByCategory to only use active rules */
